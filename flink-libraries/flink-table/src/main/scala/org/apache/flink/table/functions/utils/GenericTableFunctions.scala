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

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.{ArrayNode, ObjectNode}
import org.apache.flink.table.functions.TableFunction

import scala.annotation.varargs

class GenerateSeries extends TableFunction[Integer] {

  def eval(from: Int, to: Int): Unit = {
    if (from >= to) {
      throw new IllegalArgumentException("For Generate_series(from, to), Parameter 'to' must be " +
        "larger than parameter 'from'")
    }
    val size = to - from
    for(i <- 0 until size) {
      val result = (from + i).asInstanceOf[Integer]
      collect(result)
    }
  }
}

class JsonTuple extends TableFunction[String] {

  @varargs
  def eval(content: String, path: String*): Unit = {
    if (path.size < 1) {
      throw new IllegalArgumentException("Table function : jsonTuple takes at least two " +
        "arguments: the json string and a path expression")
    } else {
      val pathSize = path.size
      val mapper = new ObjectMapper
      val node = mapper.readTree(content)
      for (i <- 0 until pathSize) {
        val jsonNode = node.get(path.apply(i))
        if (jsonNode.isInstanceOf[ArrayNode] || jsonNode.isInstanceOf[ObjectNode]) {
          collect(jsonNode.toString)
        } else {
          collect(jsonNode.asText())
        }
      }
    }
  }
}

class StringSplit extends TableFunction[String] {

  def eval(str: String, delimiter: String): Unit = {
    if (str != null && !str.isEmpty && delimiter != null && !delimiter.isEmpty) {
      val javaSpecial = "[]()|^-+*?{}$\\."
      var delimiterRex = ""
      for (c <- delimiter) {
        if (javaSpecial.contains(c)) {
          delimiterRex = delimiterRex + "\\" + c
        } else {
          delimiterRex = delimiterRex + c
        }
      }
      val afterSplit = str.split(delimiterRex).toList
      for (string <- afterSplit) {
        collect(string)
      }
    } else {
      throw new IllegalArgumentException("The arguments of StringSplit must not be null " +
        "or empty string")
    }
  }
}

object GenericTableFunctions {

  val GENERATE_SERIES = new GenerateSeries
  val JSON_TUPLE = new JsonTuple
  val STRING_SPLIT = new StringSplit

  val instance = Map(
    "GENERATE_SERIES" -> GENERATE_SERIES,
    "JSON_TUPLE" -> JSON_TUPLE,
    "STRING_SPLIT" -> STRING_SPLIT
  )
}
