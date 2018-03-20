/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.plan.util

import java.util.ServiceLoader
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import org.apache.calcite.rel.core.JoinRelType
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.util.{ExecutorThreadFactory, Hardware}
import org.apache.flink.streaming.api.functions.async.{ResultFuture, RichAsyncFunction}
import org.apache.flink.table.expressions.{EqualTo, Expression, Literal, ResolvedFieldReference}
import org.apache.flink.table.runtime.types.CRow
import org.apache.flink.table.util.DimTableSourceProvider
import org.apache.flink.types.Row
import org.apache.flink.util.ExecutorUtils
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

class DimTableJoinAsyncFunction (
    tableType: String,
    tableParams: java.util.Map[String, java.io.Serializable],
    tableFieldNames: Array[String],
    tableFieldTypes: Array[TypeInformation[_]],
    leftKeys: Array[Int],
    leftFieldTypes: Array[TypeInformation[_]],
    rightKeys: Array[Int],
    rightFieldTypes: Array[TypeInformation[_]],
    rightFieldNames: Array[String],
    joinType: JoinRelType,
    filters: java.util.List[Expression]) extends RichAsyncFunction[CRow, CRow] {

  lazy val LOG: Logger = LoggerFactory.getLogger(getClass)

  val leftKeyTypes: Array[TypeInformation[_]] = leftKeys.map(index => leftFieldTypes(index))
  val rightKeyTypes: Array[TypeInformation[_]] = rightKeys.map(index => rightFieldTypes(index))
  val rightKeyNames: Array[String] = rightKeys.map(index => rightFieldNames(index))

  var tableSource: DimTableSourceProvider = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    ThreadPool.acquireExecutor()
    val serviceLoader = ServiceLoader.load(classOf[DimTableSourceProvider])
    tableSource =
      serviceLoader.asScala.filter(_.typeName().equalsIgnoreCase(tableType)).toList match {
        case Nil =>
          sys.error(s"#DimTableSourceProvider impl for type $tableType not found.")
        case source :: Nil =>
          val nonNullParams = if (tableParams == null) {
            new java.util.HashMap[String, java.io.Serializable]()
          } else {
            new java.util.HashMap[String, java.io.Serializable](tableParams)
          }
          nonNullParams.put("_fieldNames", tableFieldNames)
          nonNullParams.put("_fieldTypes", tableFieldTypes)
          source.init(nonNullParams)
          source
        case sources =>
          sys.error(s"Multiple #DimTableSourceProvider impls found for type $tableType " +
            s"${sources.map(_.getClass.getName).mkString("[", ", ", "]")}")
      }
  }

  override def close(): Unit = {
    ThreadPool.freeExecutor()
    super.close()
  }

  override def asyncInvoke(input: CRow, resultFuture: ResultFuture[CRow]): Unit = {
    ThreadPool.submit(new Runnable {
      override def run(): Unit = {
        try {
          val inputRow = input.row
          val leftKeyValues = leftKeys.map(index => inputRow.getField(index))
          val rightOutputIter = rightOutput(leftKeyValues)
          if (rightOutputIter.hasNext) {
            val result = new java.util.ArrayList[CRow]()
            val rightRow = rightOutputIter.next()
            val output = buildOutput(inputRow, rightRow)
            result.add(output)
            while (rightOutputIter.hasNext) {
              val rightRow = rightOutputIter.next()
              val output = buildOutput(inputRow, rightRow)
              result.add(output)
            }
            resultFuture.complete(result)
          } else {
            if (JoinRelType.LEFT.equals(joinType)) {
              val result = new java.util.ArrayList[CRow](1)
              val rightRow = new Row(rightFieldNames.length)
              val output = buildOutput(inputRow, rightRow)
              result.add(output)
              resultFuture.complete(result)
            } else {
              resultFuture.complete(java.util.Collections.emptyList[CRow]())
            }
          }
        } catch {
          case t:Throwable =>
            LOG.error("Error while async invocation.", t)
            resultFuture.completeExceptionally(t)
        }
      }

      def buildOutput(leftRow: Row, rightRow: Row): CRow = {
        val outputRow = new Row(leftRow.getArity + rightFieldNames.length)
        for (i <- 0 until leftRow.getArity) {
          outputRow.setField(i, leftRow.getField(i))
        }
        for (i <- leftRow.getArity until outputRow.getArity) {
          val j = i - leftRow.getArity
          outputRow.setField(i, rightRow.getField(j))
        }
        val output = new CRow()
        output.row = outputRow
        output.change = input.change
        output
      }

      def rightOutput(leftKeyValues: Array[AnyRef]): java.util.Iterator[Row] = {
        val filtersWithJoinCond = new java.util.ArrayList[Expression](filters)
        leftKeyValues.zipWithIndex.foreach { case(leftKeyValue, index) =>
          val attribute = ResolvedFieldReference(rightKeyNames(index), rightKeyTypes(index))
          val literal = new Literal(leftKeyValue, leftKeyTypes(index))
          val equalTo = EqualTo(attribute, literal)
          filtersWithJoinCond.add(equalTo)
        }
        tableSource.scan(rightFieldNames, filtersWithJoinCond)
      }

    })
  }

}

object ThreadPool {

  private var counter: Integer = 0
  private var executorService: ExecutorService = _

  def acquireExecutor(): Unit = this.synchronized {
    if (counter == 0) {
      executorService = Executors.newFixedThreadPool(
        Hardware.getNumberCPUCores, new ExecutorThreadFactory("dimtable-scan"))
    }
    counter += 1
  }

  def freeExecutor(): Unit = this.synchronized {
    counter -= 1
    if (counter == 0) {
      ExecutorUtils.gracefulShutdown(60, TimeUnit.SECONDS, executorService)
    }
  }

  def submit(task: Runnable): Unit = {
    if (executorService == null) {
      throw new IllegalStateException("ExecutorService is null!")
    }
    executorService.submit(task)
  }

}
