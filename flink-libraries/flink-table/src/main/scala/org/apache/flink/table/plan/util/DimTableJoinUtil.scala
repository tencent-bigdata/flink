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

import java.util.Collections
import java.util.concurrent.TimeUnit

import org.apache.calcite.plan.RelOptUtil
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.core.{JoinInfo, JoinRelType}
import org.apache.calcite.rex._
import org.apache.flink.table.expressions.Expression
import org.apache.flink.table.plan.nodes.datastream.StreamTableSourceScan
import org.apache.flink.table.sources.DimTableSource
import org.apache.flink.streaming.api.datastream.{AsyncDataStream, DataStream, SingleOutputStreamOperator}
import org.apache.flink.table.plan.nodes.datastream.DataStreamCalc
import org.apache.flink.table.plan.schema.RowSchema
import org.apache.flink.table.runtime.types.CRow
import org.apache.flink.table.validate.FunctionCatalog
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object DimTableJoinUtil {

  lazy val LOG: Logger = LoggerFactory.getLogger(getClass)

  val supportedJoinTypes = Array(JoinRelType.INNER, JoinRelType.LEFT)

  def join(leftDataStream: DataStream[CRow],
      rightNode: RelNode,
      joinInfo: JoinInfo,
      joinType: JoinRelType,
      leftSchema: RowSchema,
      rightSchema: RowSchema): Option[SingleOutputStreamOperator[CRow]] = {
    // TODO non-equi join
    if (supportedJoinTypes.contains(joinType) && joinInfo.isEqui) {
      rightNode match {
        case calc: DataStreamCalc =>
          calc.getInput match {
            case scan: StreamTableSourceScan =>
              return join0(leftSchema, rightSchema, leftDataStream, scan, joinInfo, joinType,
                Some(calc.getProgram))
            case _ =>
          }

        case scan: StreamTableSourceScan =>
          return join0(leftSchema, rightSchema, leftDataStream, scan, joinInfo, joinType)

        case _ =>
          LOG.warn(
            s"""
               |Unknown rightNode encountered:
               | ${RelOptUtil.toString(rightNode)}
             """.stripMargin)
      }
    }

    None
  }

  private def join0(
      leftSchema: RowSchema,
      rightSchema: RowSchema,
      leftDataStream: DataStream[CRow],
      scan: StreamTableSourceScan,
      joinInfo: JoinInfo,
      joinType: JoinRelType,
      program: Option[RexProgram] = None): Option[SingleOutputStreamOperator[CRow]] = {
    scan.tableSource match {
      case dimTableSource: DimTableSource =>
        val filters = program match {
          case Some(p) => translateFilter(p, scan.getCluster.getRexBuilder)
          case None => Collections.emptyList[Expression]()
        }

        val leftKeys = joinInfo.leftKeys.toIntArray
        val leftFieldTypes = leftSchema.fieldTypeInfos.toArray
        val rightKeys = joinInfo.rightKeys.toIntArray
        val rightFieldTypes = rightSchema.fieldTypeInfos.toArray
        val rightFieldNames = rightSchema.fieldNames.toArray

        Some(AsyncDataStream.orderedWait(
            leftDataStream.keyBy(leftKeys.map(index => leftSchema.fieldNames(index)):_*),
            new DimTableJoinAsyncFunction(
              dimTableSource.typeName, dimTableSource.params,
              dimTableSource.fieldNames, dimTableSource.fieldTypes,
              leftKeys, leftFieldTypes, rightKeys, rightFieldTypes, rightFieldNames,
              joinType, filters),
            60,
            TimeUnit.SECONDS)
          // TODO
          .name("DimTableJoinOperator")
        )

      case _ => None
    }
  }

  def translateFilter(program: RexProgram, rexBuilder: RexBuilder): java.util.List[Expression] = {
    val (predicates, unconverted) = RexProgramExtractor.extractConjunctiveConditions(
      program,
      rexBuilder,
      FunctionCatalog.withBuiltIns)
    if (unconverted.nonEmpty) {
      sys.error(
        s"""
           |Unconverted filters encountered: {$unconverted}
           |Input RexProgram: {$program}
         """.stripMargin)
    }
    predicates.toList.asJava
  }

}
