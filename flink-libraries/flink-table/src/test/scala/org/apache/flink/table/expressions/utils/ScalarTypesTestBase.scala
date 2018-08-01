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

package org.apache.flink.table.expressions.utils

import java.sql.{Date, Time, Timestamp}

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.RowTypeInfo
import org.apache.flink.table.api.Types
import org.apache.flink.types.Row

class ScalarTypesTestBase extends ExpressionTestBase {

  def testData: Row = {

    val testData = new Row(55)

    testData.setField(0, "This is a test String.")
    testData.setField(1, true)
    testData.setField(2, 42.toByte)
    testData.setField(3, 43.toShort)
    testData.setField(4, 44.toLong)
    testData.setField(5, 4.5.toFloat)
    testData.setField(6, 4.6)
    testData.setField(7, 3)
    testData.setField(8, " This is a test String. ")
    testData.setField(9, -42.toByte)
    testData.setField(10, -43.toShort)
    testData.setField(11, -44.toLong)
    testData.setField(12, -4.5.toFloat)
    testData.setField(13, -4.6)
    testData.setField(14, -3)
    testData.setField(15, BigDecimal("-1231.1231231321321321111").bigDecimal)
    testData.setField(16, Date.valueOf("1996-11-10"))
    testData.setField(17, Time.valueOf("06:55:44"))
    testData.setField(18, Timestamp.valueOf("1996-11-10 06:55:44.333"))
    testData.setField(19, 1467012213000L) // +16979 07:23:33.000
    testData.setField(20, 25) // +2-01
    testData.setField(21, null)
    testData.setField(22, BigDecimal("2").bigDecimal)
    testData.setField(23, "%This is a test String.")
    testData.setField(24, "*_This is a test String.")
    testData.setField(25, 0.42.toByte)
    testData.setField(26, 0.toShort)
    testData.setField(27, 0.toLong)
    testData.setField(28, 0.45.toFloat)
    testData.setField(29, 0.46)
    testData.setField(30, 1)
    testData.setField(31, BigDecimal("-0.1231231321321321111").bigDecimal)
    testData.setField(32, -1)
    testData.setField(33, null)
    testData.setField(34, 256)
    testData.setField(35, "aGVsbG8gd29ybGQ=")
    testData.setField(36, 2)
    testData.setField(37, Int.MaxValue + 1)
    testData.setField(38, Int.MinValue - 1)
    testData.setField(39,"2018-08-02 11:41:33")
    testData.setField(40,"2015-08-02 11:41:33")
    testData.setField(41, Timestamp.valueOf("2018-08-02 11:41:33"))
    testData.setField(42,Timestamp.valueOf("2015-08-02 11:41:33"))
    testData.setField(43, null)
    testData.setField(44, "2018-08-08")
    testData.setField(45, "[10, 20, [30, 40]]")
    testData.setField(46,
      """{"aaa":"bbb","ccc":{"ddd":"eee","fff":"ggg","hhh":["h0","h1","h2"]},
        |"iii":"jjj"}""".stripMargin)
    testData.setField(47, "\"{xx]\"")
    testData.setField(48, "k1=v1;k2=v2")
    testData.setField(49, "k1:v1|k2:v2")
    testData.setField(50, "http://facebook.com/path/p1.php?query=1")
    testData.setField(52, 1505404800.toLong)
    testData.setField(53, null)
    testData.setField(53, "abc")
    testData.setField(54, "123")

    testData
  }

  def typeInfo: TypeInformation[Any] = {
    new RowTypeInfo(
      Types.STRING,
      Types.BOOLEAN,
      Types.BYTE,
      Types.SHORT,
      Types.LONG,
      Types.FLOAT,
      Types.DOUBLE,
      Types.INT,
      Types.STRING,
      Types.BYTE,
      Types.SHORT,
      Types.LONG,
      Types.FLOAT,
      Types.DOUBLE,
      Types.INT,
      Types.DECIMAL,
      Types.SQL_DATE,
      Types.SQL_TIME,
      Types.SQL_TIMESTAMP,
      Types.INTERVAL_MILLIS,
      Types.INTERVAL_MONTHS,
      Types.BOOLEAN,
      Types.DECIMAL,
      Types.STRING,
      Types.STRING,
      Types.BYTE,
      Types.SHORT,
      Types.LONG,
      Types.FLOAT,
      Types.DOUBLE,
      Types.INT,
      Types.DECIMAL,
      Types.INT,
      Types.STRING,
      Types.INT,
      Types.STRING,
      Types.INT,
      Types.INT,
      Types.INT,
      Types.STRING,
      Types.STRING,
      Types.SQL_TIMESTAMP,
      Types.SQL_TIMESTAMP,
      Types.STRING,
      Types.STRING,
      Types.STRING,
      Types.STRING,
      Types.STRING,
      Types.STRING,
      Types.STRING,
      Types.STRING,
      Types.LONG,
      Types.INT,
      Types.STRING,
      Types.STRING).asInstanceOf[TypeInformation[Any]]
  }
}
