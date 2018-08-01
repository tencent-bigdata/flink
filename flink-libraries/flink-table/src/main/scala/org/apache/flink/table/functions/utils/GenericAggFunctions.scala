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

package org.apache.flink.table.functions.utils

import java.math.{BigDecimal, BigInteger}
import java.sql.{Time, Timestamp}
import java.util.Date

import org.apache.flink.table.functions.AggregateFunction

import scala.annotation.varargs

class ConcatAggAccumulator {
  var value = ""
}

class FirstOrLastValueAccumulator[T] {
  val record = new java.util.HashMap[Int, T]()
  var count = 0
  var validate = false
}

class ConcatAgg extends AggregateFunction[String, ConcatAggAccumulator] {

  override def createAccumulator(): ConcatAggAccumulator = {
    new ConcatAggAccumulator
  }

  override def getValue(accumulator: ConcatAggAccumulator): String = {
    if (accumulator.value == null || accumulator.value.isEmpty) {
      null
    } else {
      val size = accumulator.value.length
      accumulator.value.substring(0, size - 1)
    }
  }

  @varargs
  def accumulate(aggAccumulator: ConcatAggAccumulator,
                 args: Any*): Unit = {
    if (args.size == 1) {
      val value = args.head
      if (value != null && !value.equals("")) {
        aggAccumulator.value = aggAccumulator.value + value.toString + "\n"
      }
    }

    if (args.size == 2) {
      val delimiter = args.head
      val value = args.apply(1)
      if (!delimiter.isInstanceOf[String]) {
        throw new IllegalArgumentException("The arguments of the ConcatAgg function should match " +
          "these pattern: ConcatAgg(string) or ConcatAgg(delimiter, string) and delimiter must be" +
          " String.")
      }

      if (value != null && !value.equals("")) {
        aggAccumulator.value = aggAccumulator.value + value.toString + delimiter
      }
    }
  }

  def resetAccumulator(aggAccumulator: ConcatAggAccumulator):Unit = {
    aggAccumulator.value = ""
  }

  def merge(aggAccumulator: ConcatAggAccumulator,
            iterable: java.lang.Iterable[ConcatAggAccumulator]): Unit = {
    val iterator = iterable.iterator()
    while (iterator.hasNext) {
      val agg = iterator.next()
      val size = aggAccumulator.value.length
      if (size > 0) {
        aggAccumulator.value = aggAccumulator.value.substring(0, size - 1) + "\n" + agg.value
      } else {
        aggAccumulator.value = agg.value
      }
    }
  }
}

class FirstValue[T] extends AggregateFunction[T, FirstOrLastValueAccumulator[T]] {

  override def createAccumulator(): FirstOrLastValueAccumulator[T] = {
     new FirstOrLastValueAccumulator[T]
  }

  override def getValue(accumulator: FirstOrLastValueAccumulator[T]): T = {
    if (accumulator.validate) {
        accumulator.record.get(0)
    } else {
      null.asInstanceOf[T]
    }
  }

  @varargs
  def accumulate(aggAccumulator: FirstOrLastValueAccumulator[T],
                 value: T, order: Long*): Unit = {
    if (value != null && !value.equals("")) {
      aggAccumulator.record.put(aggAccumulator.count, value)
      aggAccumulator.count += 1
      aggAccumulator.validate = true
    }
  }

  def resetAccumulator(aggAccumulator: FirstOrLastValueAccumulator[T]):Unit = {
    aggAccumulator.record.clear()
    aggAccumulator.count = 0
    aggAccumulator.validate = false
  }
}

class LastValue[T] extends AggregateFunction[T, FirstOrLastValueAccumulator[T]] {

  override def createAccumulator(): FirstOrLastValueAccumulator[T] = {
    new FirstOrLastValueAccumulator[T]
  }

  override def getValue(accumulator: FirstOrLastValueAccumulator[T]): T = {
    if (accumulator.validate) {
      accumulator.record.get(accumulator.count - 1)
    } else {
      null.asInstanceOf[T]
    }
  }

  @varargs
  def accumulate(aggAccumulator: FirstOrLastValueAccumulator[T],
                 value: T, order: Long*): Unit = {
    if (value != null && !value.equals("")) {
      aggAccumulator.record.put(aggAccumulator.count, value)
      aggAccumulator.count += 1
      aggAccumulator.validate = true
    }
  }

  def resetAccumulator(aggAccumulator: FirstOrLastValueAccumulator[T]):Unit = {
    aggAccumulator.record.clear()
    aggAccumulator.count = 0
    aggAccumulator.validate = false
  }
}

//-------------------------- Built-in First_Value -------------------

class IntFirstValue extends FirstValue[Integer]

class ByteFirstValue extends FirstValue[Byte]

class ShortFirstValue extends FirstValue[Short]

class LongFirstValue extends FirstValue[Long]

class FloatFirstValue extends FirstValue[Float]

class DoubleFirstValue extends FirstValue[Double]

class StringFirstValue extends FirstValue[String]

class BooleanFirstValue extends FirstValue[Boolean]

class CharacterFirstValue extends FirstValue[Character]

class DateFirstValue extends FirstValue[Date]

class BigIntegerFirstValue extends FirstValue[BigInteger]

class BigDecimalFirstValue extends FirstValue[BigDecimal]

class TimestampFirstValue extends FirstValue[Timestamp]

class TimeFirstValue extends FirstValue[Time]

//-------------------------- Built-in Last_Value -------------------

class IntLastValue extends LastValue[Integer]

class ByteLastValue extends LastValue[Byte]

class ShortLastValue extends LastValue[Short]

class LongLastValue extends LastValue[Long]

class FloatLastValue extends LastValue[Float]

class DoubleLastValue extends LastValue[Double]

class StringLastValue extends LastValue[String]

class BooleanLastValue extends LastValue[Boolean]

class CharacterLastValue extends LastValue[Character]

class DateLastValue extends LastValue[Date]

class BigIntegerLastValue extends LastValue[BigInteger]

class BigDecimalLastValue extends LastValue[BigDecimal]

class TimestampLastValue extends LastValue[Timestamp]

class TimeLastValue extends LastValue[Time]

object GenericAggFunctions {

  val CONCAT_AGG = new ConcatAgg

  val instance = Map("CONCAT_AGG" -> CONCAT_AGG)
}
