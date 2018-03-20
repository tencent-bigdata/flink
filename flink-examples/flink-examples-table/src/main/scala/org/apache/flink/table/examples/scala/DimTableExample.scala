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

package org.apache.flink.table.examples.scala

import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.scala._
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.examples.scala.ExampleTableSourceProvider._
import org.apache.flink.table.expressions.Expression
import org.apache.flink.table.sources.DimTableSource
import org.apache.flink.table.util.DimTableSourceProvider
import org.apache.flink.types.Row

object DimTableExample {

  // *************************************************************************
  //     PROGRAM
  // *************************************************************************

  def main(args: Array[String]): Unit = {

    // set up execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tEnv = TableEnvironment.getTableEnvironment(env)

    val order: DataStream[Order] = env.fromCollection(Seq(
      Order(1L, "beer", 3),
      Order(1L, "diaper", 4),
      Order(3L, "rubber", 2)))
    tEnv.registerDataStream("OrderA", order, 'user, 'product, 'amount)

    val params = new java.util.HashMap[String, java.io.Serializable]()
    params.put("hello", "world!")
    val dimTableSource = new DimTableSource(
      tableType, params, fieldNames, fieldTypes)
    tEnv.registerDimTableSource("Product", dimTableSource)

    // val result = tEnv.sqlQuery("SELECT * FROM Order")
    val result = tEnv.sqlQuery(
      "SELECT o.product, o.amount, p.id, p.quantity FROM OrderA AS o, Product AS p " +
        "WHERE o.product = p.name AND o.amount = p.quantity AND o.amount < 4 AND p.quantity > 0")

    // println(RelOptUtil.toString(result.getRelNode))
    result.toAppendStream[Row].print()

    env.execute()
  }

  // *************************************************************************
  //     USER DATA TYPES
  // *************************************************************************

  case class Order(user: Long, product: String, amount: Int)

}

object ExampleTableSourceProvider {

  val tableType: String = "example"

  val fieldNames: Array[String] = Array("id", "name", "quantity")
  val fieldTypes: Array[TypeInformation[_]] =
    Array(BasicTypeInfo.LONG_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO)

  val fieldIndexMap: Map[String, Int] = {
    var m = Map[String, Int]()
    fieldNames.zipWithIndex.foreach { case (fieldName, index) =>
      m += (fieldName -> index)
    }
    m
  }

  val records: Array[Array[Any]] = Array(
    Array(66L, "beer",   20),
    Array(67L, "diaper", 10)
  )

}

class ExampleTableSourceProvider extends DimTableSourceProvider {

  override def typeName(): String = tableType

  override def init(params: java.util.Map[String, java.io.Serializable]): Unit = {
    println(s"params: $params")
  }

  override def scan(requiredColumns: Array[String], filters: java.util.List[Expression]): java.util.Iterator[Row] = {
    new java.util.Iterator[Row] {
      var i = 0
      override def hasNext: Boolean = i < records.length
      override def next(): Row = {
        val row = new Row(requiredColumns.length)
        requiredColumns.zipWithIndex.foreach { case (fieldName, index) =>
          row.setField(index, records(i)(fieldIndexMap(fieldName)))
        }
        i += 1
        row
      }
    }
  }

}
