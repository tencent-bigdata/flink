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
package org.apache.flink.table.runtime.functions

import java.lang.{Long => JLong}
import java.util.regex.Matcher

import org.apache.flink.table.utils.EncodingUtils

import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils

import collection.JavaConversions._
import com.google.common.base.Splitter
import com.jayway.jsonpath.JsonPath

import scala.annotation.varargs

import java.lang.StringBuilder

import java.math.{BigDecimal => JBigDecimal}
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.{Date, Locale}
import java.util.regex.Pattern
import java.text.SimpleDateFormat


/**
  * Built-in scalar runtime functions.
  *
  * NOTE: Before you add functions here, check if Calcite provides it in
  * [[org.apache.calcite.runtime.SqlFunctions]]. Furthermore, make sure to implement the function
  * efficiently. Sometimes it makes sense to create a
  * [[org.apache.flink.table.codegen.calls.CallGenerator]] instead to avoid massive object
  * creation and reuse instances.
  */
class ScalarFunctions {}

object ScalarFunctions {

  def isDecimal(str: String): Boolean = {
    var data=str
    var result = false
    if (data.startsWith("-")) {
      data = data.substring(0)
    }
    val pattern1 = Pattern.compile("[0-9]+")
    var isNum = pattern1.matcher(data)
    if (isNum.matches) {
      result = true
    }
    val pattern2 = Pattern.compile("^\\\\d+(\\\\.\\\\d+)?$")
    isNum = pattern2.matcher(data)
    if (isNum.matches) {
      result = true
    }
    result
  }

  def isAlpha(str: String): Boolean= {
    val regex = "[a-zA-Z]+"
    val m = Pattern.compile(regex).matcher(str)
    m.matches()
  }

  def _IF(condition: Boolean, v1: String, v2: String): String = {
    if (condition) {
      v1
    }
    else {
      v2
    }
  }

  def power(a: Double, b: JBigDecimal): Double = {
    Math.pow(a, b.doubleValue())
  }

  /**
    * Returns the hyperbolic cosine of a big decimal value.
    */
  def cosh(x: JBigDecimal): Double = {
    Math.cosh(x.doubleValue())
  }
  /**
    * Returns the result of bitwise AND of A and B.
    * If one of parameters is out of range, will throw IllegalArgumentException.
    */
  def bitAnd(a: Int, b: Int): Int = {
    if(a > Int.MaxValue || a < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter a of 'bitAnd(a, b)' must be a int, " +
        s"but it's overflowed, a = $a")
    }

    if(b >  Int.MaxValue || b < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter b of 'bitAnd(a, b)' must be a int, " +
        s"but it's overflowed, b = $b")
    }

    a & b
  }

  /**
    * Returns the result of bitwise Not of A.
    * If one of parameters is out of range, will throw IllegalArgumentException.
    */
  def bitNot(a: Int): Int = {
    if(a > Int.MaxValue || a < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter a of 'bitNot(a, b)' must be a int, " +
        s"but it's overflowed, a = $a")
    }

    ~ a
  }

  /**
    * Returns a int results form bit or.
    * If one of parameters is out of range, will throw IllegalArgumentException.
    */
  def bitOr(a: Int, b: Int): Int = {
    if(a > Int.MaxValue || a < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter a of 'bitOr(a, b)' must be a int, " +
        s"but it's overflowed, a = $a")
    }

    if(b >  Int.MaxValue || b < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter b of 'bitOr(a, b)' must be a int, " +
        s"but it's overflowed, b = $b")
    }

    a | b
  }

  /**
    * Returns the result of bitwise XOR of A and B.
    * If one of parameters is out of range, will throw IllegalArgumentException.
    */
  def bitXor(a: Int, b: Int): Int = {
    if(a > Int.MaxValue || a < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter a of 'bitXor(a, b)' must be a int, " +
        s"but it's overflowed, a = $a")
    }

    if(b >  Int.MaxValue || b < Int.MinValue ){
      throw new IllegalArgumentException(s"Parameter b of 'bitXor(a, b)' must be a int, " +
        s"but it's overflowed, b = $b")
    }

    a ^ b
  }

  /**
    * Returns the string that results from concatenating the arguments.
    * Returns NULL if any argument is NULL.
    */
  @varargs
  def concat(args: String*): String = {
    val sb = new StringBuilder
    var i = 0
    while (i < args.length) {
      if (args(i) == null) {
        return null
      }
      sb.append(args(i))
      i += 1
    }
    sb.toString
  }

  /**
    * Returns the string that results from concatenating the arguments and separator.
    * Returns NULL If the separator is NULL.
    *
    * Note: CONCAT_WS() does not skip empty strings. However, it does skip any NULL values after
    * the separator argument.
    *
    **/
  @varargs
  def concat_ws(separator: String, args: String*): String = {
    if (null == separator) {
      return null
    }

    val sb = new StringBuilder

    var i = 0

    var hasValueAppended = false

    while (i < args.length) {
      if (null != args(i)) {
        if (hasValueAppended) {
          sb.append(separator)
        }
        sb.append(args(i))
        hasValueAppended = true
      }
      i = i + 1
    }
    sb.toString
  }

  /**
    * Returns the natural logarithm of "x".
    */
  def log(x: Double): Double = {
    if (x <= 0.0) {
      throw new IllegalArgumentException(s"x of 'log(x)' must be > 0, but x = $x")
    } else {
      Math.log(x)
    }
  }

  /**
    * Calculates the hyperbolic tangent of a big decimal number.
    */
  def tanh(x: JBigDecimal): Double = {
    Math.tanh(x.doubleValue())
  }

  /**
    * Returns the hyperbolic sine of a big decimal value.
    */
  def sinh(x: JBigDecimal): Double = {
    Math.sinh(x.doubleValue())
  }

  /**
    * Returns the logarithm of "x" with base "base".
    */
  def log(base: Double, x: Double): Double = {
    if (x <= 0.0) {
      throw new IllegalArgumentException(s"x of 'log(base, x)' must be > 0, but x = $x")
    }
    if (base <= 1.0) {
      throw new IllegalArgumentException(s"base of 'log(base, x)' must be > 1, but base = $base")
    } else {
      Math.log(x) / Math.log(base)
    }
  }

  def repeat(base: String, cnt: Integer): String = {
    if (cnt < 0) {
      return null
    } else if (cnt == 0) {
      return ""
    }
    var s1 = new StringBuilder()
    for (i <- 1 to cnt) {
      s1.append(base)
    }
    return s1.toString
  }

  def reverse(base: String): String = {
    if (base == null) return null
    var s1 = new StringBuilder()
    for (i <- 1 to base.length) {
      s1.append(base.charAt(base.length - i))
    }
    return s1.toString
  }

  def splitIndex(base: String, sep: String, index: Integer): String = {
    if (base == null || sep == null) return null
    var regex = sep
    if (regex.length == 1 && ".$|()[{^?*+\\".indexOf(regex.charAt(0)) > -1) {
      regex = "\\".concat(regex)
    }
    val result = base.split(regex)
    if (result.length <= index) {
      return null
    } else {
      return result(index)
    }
  }

  /**
    * Returns the logarithm of "x" with base 2.
    */
  def log2(x: Double): Double = {
    if (x <= 0.0) {
      throw new IllegalArgumentException(s"x of 'log2(x)' must be > 0, but x = $x")
    } else {
      Math.log(x) / Math.log(2)
    }
  }

  /**
    * Returns the string str left-padded with the string pad to a length of len characters.
    * If str is longer than len, the return value is shortened to len characters.
    */
  def lpad(base: String, len: Integer, pad: String): String = {
    if (len < 0) {
      return null
    } else if (len == 0) {
      return ""
    }

    val data = new Array[Char](len)
    val baseChars = base.toCharArray
    val padChars = pad.toCharArray

    // The length of the padding needed
    val pos = Math.max(len - base.length, 0)

    // Copy the padding
    var i = 0
    while (i < pos) {
      var j = 0
      while (j < pad.length && j < pos - i) {
        data(i + j) = padChars(j)
        j += 1
      }
      i += pad.length
    }

    // Copy the base
    i = 0
    while (pos + i < len && i < base.length) {
      data(pos + i) = baseChars(i)
      i += 1
    }

    new String(data)
  }

  /**
    * Returns the string str right-padded with the string pad to a length of len characters.
    * If str is longer than len, the return value is shortened to len characters.
    */
  def rpad(base: String, len: Integer, pad: String): String = {
    if (len < 0) {
      return null
    } else if (len == 0) {
      return ""
    }

    val data = new Array[Char](len)
    val baseChars = base.toCharArray
    val padChars = pad.toCharArray

    var pos = 0

    // Copy the base
    while (pos < base.length && pos < len) {
      data(pos) = baseChars(pos)
      pos += 1
    }

    // Copy the padding
    while (pos < len) {
      var i = 0
      while (i < pad.length && i < len - pos) {
        data(pos + i) = padChars(i)
        i += 1
      }
      pos += pad.length
    }

    new String(data)
  }

  /**
    * Returns a string resulting from replacing all substrings
    * that match the regular expression with replacement.
    */
  def regexp_replace(str: String, regex: String, replacement: String): String = {
    if (str == null || regex == null || replacement == null) {
      return null
    }

    str.replaceAll(regex, Matcher.quoteReplacement(replacement))
  }

  /**
    * Returns a string extracted with a specified regular expression and a regex match group index.
    */
  def regexp_extract(str: String, regex: String, extractIndex: Integer): String = {
    if (str == null || regex == null) {
      return null
    }

    val m = Pattern.compile(regex).matcher(str)
    if (m.find) {
      val mr = m.toMatchResult
      return mr.group(extractIndex)
    }

    null
  }

  /**
    * Returns a string extracted with a specified regular expression and
    * a optional regex match group index.
    */
  def regexp_extract(str: String, regex: String): String = {
    regexp_extract(str, regex, 0)
  }

  /**
    * Returns the base string decoded with base64.
    */
  def fromBase64(base64: String): String =
    EncodingUtils.decodeBase64ToString(base64)

  /**
    * Returns the base64-encoded result of the input string.
    */
  def toBase64(string: String): String =
    EncodingUtils.encodeStringToBase64(string)

  /**
    * Returns the hex string of a long argument.
    */
  def hex(string: Long): String = JLong.toHexString(string).toUpperCase()

  /**
    * Returns the hex string of a string argument.
    */
  def hex(string: String): String =
    EncodingUtils.hex(string).toUpperCase()

  /**
    * Returns an UUID string using Java utilities.
    */
  def uuid(): String = java.util.UUID.randomUUID().toString

  /**
    * Returns a string that repeats the base string n times.
    */
  def repeat(base: String, n: Int): String = EncodingUtils.repeat(base, n)

  def hash_code(str: String): Int = {
    str.hashCode
  }

  def json_value(content: String, jsonPath: String): String = {
    try {
      val jsonArray = JsonPath.read[Any](content, jsonPath)
      val result = jsonArray.toString
      result
    } catch {
      case e: Exception => "null"
    }
  }

  def keyvalue(string: String, split1: String, split2: String, key: String): String = {
    if (string == null || string == "") {
      "null"
    } else {
      try {
        val JAVA_REGEX_SPECIALS = "[]()|^-+*?{}$\\."
        var split_r_1 = ""
        var split_r_2 = ""
        for (c <- split1.toList) {
          if(JAVA_REGEX_SPECIALS.contains(c)) {split_r_1 = split_r_1 + "\\" + c}
          else {split_r_1 = split_r_1 + c}
        }
        for (c <- split2.toList) {
          if(JAVA_REGEX_SPECIALS.contains(c)) {split_r_2 = split_r_2 + "\\" + c}
          else {split_r_2 = split_r_2 + c}
        }

        val pattern = s"(.+$split_r_2.+)($split_r_1.+$split_r_2.+)*".r
        val key_values = pattern.findAllIn(string).mkString
        val key_value = key_values.split(split_r_1).toList

        var result = ""
        for (kv <- key_value) {
          if (kv.split(split_r_2).apply(0).equals(key)) {
            if(result != "") {
              result += ","
            }
            result += kv.split(split_r_2).apply(1)
          }
        }
        if(result == "") {
          result = "null"
        }
        result
      } catch {
        case e:Exception => "null"
      }
    }
  }

  @varargs
  def parse_url(urlString: String, partToExtract_key: String*): String = {
    try {
      val url = new java.net.URL(urlString)
      val key = if (partToExtract_key.size == 2) partToExtract_key.apply(1) else ""
      partToExtract_key.head match {
        case "AUTHORITY" => url.getAuthority
        case "FILE" => url.getFile
        case "HOST" => url.getHost
        case "PATH" => url.getPath
        case "REF" => url.getRef
        case "PROTOCOL" => url.getProtocol
        case "USERINFO" => url.getUserInfo
        case "QUERY" =>
          var result = ""
          val query = url.getQuery
          val values: java.util.List[NameValuePair] = URLEncodedUtils.parse(query, Charset.forName
          ("UTF-8"))
          for (name_value: NameValuePair <- values) {
            if (name_value.getName == key) {
              result = name_value.getValue
            } else result = if (key == "") query else "null"
          }
          values.clear()
          if (result == "") "null" else result
      }
    } catch {
      case e: Exception => "null"
    }
  }

  @varargs
  def unix_timestamp(args: String*): Long = {
    try {
      if (args.size > 2) {
        throw new Exception(s"The number of arguments of unix_timestamp($args) is wrong")
      } else {
        if(args.isEmpty) {
          val result = System.currentTimeMillis() / 1000
          result
        } else if(args.size == 1) {
          val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
          val timestamp = format.parse(args.head).getTime / 1000
          timestamp
        } else {
          val format = new SimpleDateFormat(args.apply(1), Locale.US)
          val timestamp = format.parse(args.head).getTime / 1000
          timestamp
        }
      }
    } catch {
      case e: Exception => throw new Exception(s"Exception is $e, cause is ${e.getCause}")
    }
  }

  @varargs
  def from_unixtime(unixTime: Long, formatString: String*): String = {
    try{
      if (formatString.size > 1 || (formatString.size == 1 && formatString.head == "")) {
        "null"
      } else {
        val formatStr = if (formatString.size == 1) formatString.head else "yyyy-MM-dd HH:mm:ss"
        val timestamp = unixTime * 1000
        val format = new SimpleDateFormat(formatStr, Locale.US)
        val date = format.format(new Date(timestamp))
        date
      }
    } catch {
      case e: Exception => "null"
    }
  }

  @varargs
  def now(offset: Int*): Long = {
    try{
      if (offset.size > 1) {
        throw new Exception(s"The number of arguments of now($offset) is wrong")
      } else {
        val nowtime = System.currentTimeMillis() / 1000
        val offSet = if (offset.size == 1) offset.head else 0
        nowtime + offSet
      }
    } catch {
      case e: Exception => throw new Exception(s"Exception is $e, cause is ${e.getCause}")
    }
  }

  @varargs
  def strToMap(args: String*): java.util.Map[String, String] = {
    val args1Size = args.size == 1
    val args3Size = args.size == 3 && args.apply(1) != null && !args.apply(1).isEmpty &&
      args.apply(2) != null && !args.apply(2).isEmpty

    if ((args1Size || args3Size) && (args.head != null && !args.head.isEmpty)) {
      val javaSpecial = "[]()|^-+*?{}$\\."
      val keyValueMap = new java.util.HashMap[String, String]
      var delimiter1 = ""
      var delimiter2 = ""

      try{
        if (args1Size) {
          delimiter1 = ","
          delimiter2 = "="
        } else {
          for (c <- args.apply(1)) {
            if (javaSpecial.contains(c)) {
              delimiter1 = delimiter1 + "\\" + c
            } else {
              delimiter1 = delimiter1 + c
            }
          }

          for (c <- args.apply(2)) {
            if (javaSpecial.contains(c)) {
              delimiter2 = delimiter2 + "\\" + c
            } else {
              delimiter2 =delimiter2 + c
            }
          }
        }

        val pattern = s"(.+$delimiter2.+)($delimiter1.+$delimiter2.+)*".r
        val keyValues = pattern.findAllIn(args.head).mkString
        val keyValue = keyValues.split(delimiter1).toList
        for (kv <- keyValue) {
          keyValueMap.put(kv.split(delimiter2).apply(0), kv.split(delimiter2).apply(1))
        }
        keyValueMap
      } catch {
        case e: Exception => throw new Exception(e)
      }
    } else {
      throw new IllegalArgumentException("The arguments of the strToMap function should match " +
        "these pattern: strToMap(string) or strToMap(string, delimiter1, delimiter2), and all " +
        "arguments must not be null or empty string.")
    }
  }

  def urlEncode(url: String, enc: String): String = {
    try {
      URLEncoder.encode(url, enc)
    } catch {
      case e: Exception => "Exception while encoding " + e.getMessage
    }
  }

  def urlDecode(encodedURL: String, enc: String): String = {
    try {
      var prevUrl = ""
      var decodeUrl = encodedURL
      while (!prevUrl.equals(decodeUrl)) {
        prevUrl = decodeUrl
        decodeUrl = URLDecoder.decode(decodeUrl, enc)
      }
      decodeUrl
    } catch {
      case e:Exception => "Exception while decoding " + e.getMessage
    }
  }
}
