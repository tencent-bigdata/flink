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

package org.apache.flink.table.expressions

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.flink.api.common.typeinfo.{TypeInformation, Types}
import org.apache.flink.api.java.typeutils.RowTypeInfo
import org.apache.flink.table.api.scala._
import org.apache.flink.table.expressions.utils.ExpressionTestBase
import org.apache.flink.types.Row
import org.joda.time.{DateTime, DateTimeZone}
import org.junit.Test

class DateTimeFunctionTest extends ExpressionTestBase {
  private val INSTANT = DateTime.parse("1990-01-02T03:04:05.678Z")
  private val LOCAL_ZONE = DateTimeZone.getDefault
  private val LOCAL_TIME = INSTANT.toDateTime(LOCAL_ZONE)

  @Test
  def testDateFormat(): Unit = {
    val expected = LOCAL_TIME.toString("MM/dd/yyyy HH:mm:ss.SSSSSS")
    testAllApis(
      dateFormat('f0, "%m/%d/%Y %H:%i:%s.%f"),
      "dateFormat(f0, \"%m/%d/%Y %H:%i:%s.%f\")",
      "DATE_FORMAT(f0, '%m/%d/%Y %H:%i:%s.%f')",
      expected)
  }

  @Test
  def testDateFormatNonConstantFormatter(): Unit = {
    val expected = LOCAL_TIME.toString("MM/dd/yyyy")
    testAllApis(
      dateFormat('f0, 'f1),
      "dateFormat(f0, f1)",
      "DATE_FORMAT(f0, f1)",
      expected)
  }

  @Test
  def testDateAdd(): Unit = {
    testAllApis(
      'f2.dateAdd(1),
      "f2.dateAdd(1)",
      "DATE_ADD(f2,1)",
      "2018-08-09")
  }

  @Test
  def testDateSub(): Unit = {
    testAllApis(
      'f2.dateSub(1),
      "f2.dateSub(1)",
      "DATE_SUB(f2,1)",
      "2018-08-07")
  }


  @Test
  def testUnixTimestamp():Unit = {
    testAllApis(
      unix_timestamp("2012-09-27 17:13:49"),
      "unix_timestamp(\"2012-09-27 17:13:49\")",
      "UNIX_TIMESTAMP('2012-09-27 17:13:49')",
      "1348737229"
    )

    testAllApis(
      unix_timestamp('f3),
      "unix_timestamp(f3)",
      "UNIX_TIMESTAMP(f3)",
      "null"
    )

    testAllApis(
      unix_timestamp("2012.09.27 17:13:49", "yyyy.MM.dd HH:mm:ss"),
      "unix_timestamp(\"2012.09.27 17:13:49\", \"yyyy.MM.dd HH:mm:ss\")",
      "UNIX_TIMESTAMP('2012.09.27 17:13:49', 'yyyy.MM.dd HH:mm:ss')",
      "1348737229"
    )

    testAllApis(
      unix_timestamp("2017-09-15 00:00:00"),
      "unix_timestamp(\"2017-09-15 00:00:00\")",
      "UNIX_TIMESTAMP('2017-09-15 00:00:00')",
      "1505404800"
    )
  }

  @Test
  def testFromUnixTime(): Unit = {
    testAllApis(
      from_unixtime('f4),
      "from_unixtime(f4)",
      "FROM_UNIXTIME(f4)",
      "2017-09-15 00:00:00"
    )

    testAllApis(
      from_unixtime('f4, 'f3),
      "from_unixtime(f4, f3)",
      "FROM_UNIXTIME(f4, f3)",
      "null")

    testAllApis(
      from_unixtime('f4, "MMdd-yyyy"),
      "from_unixtime(f4, \"MMdd-yyyy\")",
      "FROM_UNIXTIME(f4, 'MMdd-yyyy')",
      "0915-2017")

    testAllApis(
      from_unixtime('f4, ""),
      "from_unixtime(f4, \"\")",
      "FROM_UNIXTIME(f4, '')",
      "null")
  }

  @Test
  def testNow(): Unit = {
    testAllApis(
      now('f5),
      "now(f5)",
      "NOW(f5)",
      "null"
    )
  }

  override def testData: Any = {
    val testData = new Row(6)
    // SQL expect a timestamp in the local timezone
    testData.setField(0, new Timestamp(LOCAL_ZONE.convertLocalToUTC(INSTANT.getMillis, true)))
    testData.setField(1, "%m/%d/%Y")
    testData.setField(2, "2018-08-08")
    testData.setField(3, null)
    testData.setField(4, 1505404800.toLong)
    testData.setField(5, null)
    testData
  }

  override def typeInfo: TypeInformation[Any] =
    new RowTypeInfo(Types.SQL_TIMESTAMP,
                    Types.STRING,
                    Types.STRING,
                    Types.STRING, Types.LONG, Types.INT).asInstanceOf[TypeInformation[Any]]
}
