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

import org.apache.calcite.rex.RexNode
import org.apache.calcite.sql.fun.SqlStdOperatorTable
import org.apache.calcite.tools.RelBuilder
import org.apache.flink.api.common.typeinfo.BasicTypeInfo._
import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation, Types}
import org.apache.flink.table.expressions.TrimMode.TrimMode
import org.apache.flink.table.functions.sql.ScalarSqlFunctions
import org.apache.flink.table.validate._

import scala.collection.JavaConversions._

case class IsDecimal(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def expectedTypes = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType = BOOLEAN_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"IsDecimal operator requires String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder) = {
    relBuilder.call(ScalarSqlFunctions.IS_DECIMAL, children.map(_.toRexNode))
  }
}

case class IsAlpha(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def expectedTypes = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType = BOOLEAN_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"IsAlpha operator requires String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder) = {
    relBuilder.call(ScalarSqlFunctions.IS_ALPHA, children.map(_.toRexNode))
  }
}

case class _IF(condition: Expression, v1: Expression, v2: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def resultType = STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(BOOLEAN_TYPE_INFO, STRING_TYPE_INFO, STRING_TYPE_INFO)

  override private[flink] def children: Seq[Expression] = Seq(condition, v1, v2)

  override private[flink] def validateInput() = {
   if (condition.resultType == BOOLEAN_TYPE_INFO) {
     ValidationSuccess
   } else {
     ValidationFailure(s"IF operator requires first input type is Boolean ,the second type" +
       s" must equal the third type " +
       s"but the second type  is ${v1.resultType} and the third type is ${v2.resultType}")
   }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder) = {
    relBuilder.call(ScalarSqlFunctions._IF, condition.toRexNode, v1.toRexNode, v2.toRexNode)
  }
}

/**
  * Returns the length of this `str`.
  */
case class CharLength(child: Expression) extends UnaryExpression {
  override private[flink] def resultType: TypeInformation[_] = INT_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"CharLength operator requires String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override def toString: String = s"($child).charLength()"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.CHAR_LENGTH, child.toRexNode)
  }
}

/**
  * Returns str with the first letter of each word in uppercase.
  * All other letters are in lowercase. Words are delimited by white space.
  */
case class InitCap(child: Expression) extends UnaryExpression {
  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"InitCap operator requires String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override def toString: String = s"($child).initCap()"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.INITCAP, child.toRexNode)
  }
}

/**
  * Returns true if `str` matches `pattern`.
  */
case class Like(str: Expression, pattern: Expression) extends BinaryExpression {
  private[flink] def left: Expression = str
  private[flink] def right: Expression = pattern

  override private[flink] def resultType: TypeInformation[_] = BOOLEAN_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (str.resultType == STRING_TYPE_INFO && pattern.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"Like operator requires (String, String) input, " +
        s"but ($str, $pattern) is of type (${str.resultType}, ${pattern.resultType})")
    }
  }

  override def toString: String = s"($str).like($pattern)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.LIKE, children.map(_.toRexNode))
  }
}

/**
  * Returns str with all characters changed to lowercase.
  */
case class Lower(child: Expression) extends UnaryExpression {
  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"Lower operator requires String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override def toString: String = s"($child).lowerCase()"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.LOWER, child.toRexNode)
  }
}

/**
  * Returns true if `str` is similar to `pattern`.
  */
case class Similar(str: Expression, pattern: Expression) extends BinaryExpression {
  private[flink] def left: Expression = str
  private[flink] def right: Expression = pattern

  override private[flink] def resultType: TypeInformation[_] = BOOLEAN_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (str.resultType == STRING_TYPE_INFO && pattern.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"Similar operator requires (String, String) input, " +
        s"but ($str, $pattern) is of type (${str.resultType}, ${pattern.resultType})")
    }
  }

  override def toString: String = s"($str).similarTo($pattern)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.SIMILAR_TO, children.map(_.toRexNode))
  }
}

/**
  * Returns substring of `str` from `begin`(inclusive) for `length`.
  */
case class Substring(
    str: Expression,
    begin: Expression,
    length: Expression) extends Expression with InputTypeSpec {

  def this(str: Expression, begin: Expression) = this(str, begin, CharLength(str))

  override private[flink] def children: Seq[Expression] = str :: begin :: length :: Nil

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(STRING_TYPE_INFO, INT_TYPE_INFO, INT_TYPE_INFO)

  override def toString: String = s"($str).substring($begin, $length)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.SUBSTRING, children.map(_.toRexNode))
  }
}

/**
  * Trim `trimString` from `str` according to `trimMode`.
  */
case class Trim(
    trimMode: Expression,
    trimString: Expression,
    str: Expression) extends Expression {

  override private[flink] def children: Seq[Expression] = trimMode :: trimString :: str :: Nil

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    trimMode match {
      case SymbolExpression(_: TrimMode) =>
        if (trimString.resultType != STRING_TYPE_INFO) {
          ValidationFailure(s"String expected for trimString, get ${trimString.resultType}")
        } else if (str.resultType != STRING_TYPE_INFO) {
          ValidationFailure(s"String expected for str, get ${str.resultType}")
        } else {
          ValidationSuccess
        }
      case _ => ValidationFailure("TrimMode symbol expected.")
    }
  }

  override def toString: String = s"($str).trim($trimMode, $trimString)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.TRIM, children.map(_.toRexNode))
  }
}

/**
  * Enumeration of trim flags.
  */
object TrimConstants {
  val TRIM_DEFAULT_CHAR = Literal(" ")
}

/**
  * Returns str with all characters changed to uppercase.
  */
case class Upper(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(STRING_TYPE_INFO)

  override def toString: String = s"($child).upperCase()"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.UPPER, child.toRexNode)
  }
}

/**
  * Returns the position of string needle in string haystack.
  */
case class Position(needle: Expression, haystack: Expression)
    extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(needle, haystack)

  override private[flink] def resultType: TypeInformation[_] = INT_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(STRING_TYPE_INFO, STRING_TYPE_INFO)

  override def toString: String = s"($needle).position($haystack)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.POSITION, needle.toRexNode, haystack.toRexNode)
  }
}

/**
  * Replaces a substring of a string with a replacement string.
  * Starting at a position for a given length.
  */
case class Overlay(
    str: Expression,
    replacement: Expression,
    starting: Expression,
    position: Expression)
  extends Expression with InputTypeSpec {

  def this(str: Expression, replacement: Expression, starting: Expression) =
    this(str, replacement, starting, CharLength(replacement))

  override private[flink] def children: Seq[Expression] =
    Seq(str, replacement, starting, position)

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(STRING_TYPE_INFO, STRING_TYPE_INFO, INT_TYPE_INFO, INT_TYPE_INFO)

  override def toString: String = s"($str).overlay($replacement, $starting, $position)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(
      SqlStdOperatorTable.OVERLAY,
      str.toRexNode,
      replacement.toRexNode,
      starting.toRexNode,
      position.toRexNode)
  }
}

/**
  * Returns the string that results from concatenating the arguments.
  * Returns NULL if any argument is NULL.
  */
case class Concat(strings: Seq[Expression]) extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = strings

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    children.map(_ => STRING_TYPE_INFO)

  override def toString: String = s"concat($strings)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.CONCAT, children.map(_.toRexNode))
  }
}

/**
  * Returns the string that results from concatenating the arguments and separator.
  * Returns NULL If the separator is NULL.
  *
  * Note: this user-defined function does not skip empty strings. However, it does skip any NULL
  * values after the separator argument.
  **/
case class ConcatWs(separator: Expression, strings: Seq[Expression])
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(separator) ++ strings

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    children.map(_ => STRING_TYPE_INFO)

  override def toString: String = s"concat_ws($separator, $strings)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.CONCAT_WS, children.map(_.toRexNode))
  }
}

/**
  * Returns the idx segment from the split strings.
  */
case class SplitIndex(text: Expression, split: Expression, idx: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(text, split, idx)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO)

  override def toString: String = s"($text).splitIndex($split,$idx)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.SPLIT_INDEX, children.map(_.toRexNode))
  }
}

/**
  * Returns a reversed string.
  */
case class Reverse(text: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(text)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(BasicTypeInfo.STRING_TYPE_INFO)

  override def toString: String = s"($text).reverse()"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.REVERSE, children.map(_.toRexNode))
  }
}


case class Lpad(text: Expression, len: Expression, pad: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(text, len, pad)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO)

  override def toString: String = s"($text).lpad($len, $pad)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.LPAD, children.map(_.toRexNode))
  }
}

case class Rpad(text: Expression, len: Expression, pad: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(text, len, pad)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO)

  override def toString: String = s"($text).rpad($len, $pad)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.RPAD, children.map(_.toRexNode))
  }
}

/**
  * Returns a string with all substrings that match the regular expression consecutively
  * being replaced.
  */
case class RegexpReplace(str: Expression, regex: Expression, replacement: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(
      BasicTypeInfo.STRING_TYPE_INFO,
      BasicTypeInfo.STRING_TYPE_INFO,
      BasicTypeInfo.STRING_TYPE_INFO)

  override private[flink] def children: Seq[Expression] = Seq(str, regex, replacement)

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.REGEXP_REPLACE, children.map(_.toRexNode))
  }

  override def toString: String = s"($str).regexp_replace($regex, $replacement)"
}

/**
  * Returns a string extracted with a specified regular expression and a regex match group index.
  */
case class RegexpExtract(str: Expression, regex: Expression, extractIndex: Expression)
  extends Expression with InputTypeSpec {
  def this(str: Expression, regex: Expression) = this(str, regex, null)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = {
    if (extractIndex == null) {
      Seq(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO)
    } else {
      Seq(
        BasicTypeInfo.STRING_TYPE_INFO,
        BasicTypeInfo.STRING_TYPE_INFO,
        BasicTypeInfo.INT_TYPE_INFO)
    }
  }

  override private[flink] def children: Seq[Expression] = {
    if (extractIndex == null) {
      Seq(str, regex)
    } else {
      Seq(str, regex, extractIndex)
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.REGEXP_EXTRACT, children.map(_.toRexNode))
  }

  override def toString: String = s"($str).regexp_extract($regex, $extractIndex)"
}

object RegexpExtract {
  def apply(str: Expression, regex: Expression): RegexpExtract = RegexpExtract(str, regex, null)
}

/**
  * Returns the base string decoded with base64.
  * Returns NULL If the input string is NULL.
  */
case class FromBase64(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"FromBase64 operator requires String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.FROM_BASE64, child.toRexNode)
  }

  override def toString: String = s"($child).fromBase64"

}

/**
  * Returns the base64-encoded result of the input string.
  */
case class ToBase64(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"ToBase64 operator requires a String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.TO_BASE64, child.toRexNode)
  }

  override def toString: String = s"($child).toBase64"
}

case class HashCode(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType: TypeInformation[_] = INT_TYPE_INFO

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.HASH_CODE, children.map(_.toRexNode))
  }

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"hash_code operator requires String input," +
        s"but $child is of type ${child.resultType}")
    }
  }

  override def toString: String = s"hash_code($child)"
}

/**
  * Returns a string that removes the left whitespaces from the given string.
  */
case class LTrim(child: Expression) extends UnaryExpression with InputTypeSpec {
  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"LTrim operator requires a String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.LTRIM, child.toRexNode)
  }

  override def toString = s"($child).ltrim"
}

/**
  * Returns a string that removes the right whitespaces from the given string.
  */
case class RTrim(child: Expression) extends UnaryExpression with InputTypeSpec {

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = Seq(STRING_TYPE_INFO)

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def validateInput(): ValidationResult = {
    if (child.resultType == STRING_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"RTrim operator requires a String input, " +
        s"but $child is of type ${child.resultType}")
    }
  }

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.RTRIM, child.toRexNode)
  }

  override def toString = s"($child).rtrim"
}

/**
  * Returns a string that repeats the base str n times.
  */
case class Repeat(str: Expression, n: Expression) extends Expression with InputTypeSpec {

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(STRING_TYPE_INFO, INT_TYPE_INFO)

  override private[flink] def children: Seq[Expression] = Seq(str, n)

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.REPEAT, str.toRexNode, n.toRexNode)
  }

  override private[flink] def validateInput(): ValidationResult = {
    if (str.resultType == STRING_TYPE_INFO && n.resultType == INT_TYPE_INFO) {
      ValidationSuccess
    } else {
      ValidationFailure(s"Repeat operator requires (String, Int) input, " +
        s"but ($str, $n) is of type (${str.resultType}, ${n.resultType})")
    }
  }

  override def toString: String = s"($str).repeat($n)"
}

/**
  * Returns a new string which replaces all the occurrences of the search target
  * with the replacement string (non-overlapping).
  */
case class Replace(str: Expression,
  search: Expression,
  replacement: Expression) extends Expression with InputTypeSpec {

  def this(str: Expression, begin: Expression) = this(str, begin, CharLength(str))

  override private[flink] def children: Seq[Expression] = str :: search :: replacement :: Nil

  override private[flink] def resultType: TypeInformation[_] = STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    Seq(STRING_TYPE_INFO, STRING_TYPE_INFO, STRING_TYPE_INFO)

  override def toString: String = s"($str).replace($search, $replacement)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(SqlStdOperatorTable.REPLACE, children.map(_.toRexNode))
  }
}

case class JsonValue(content: Expression, jsonPath: Expression) extends Expression with
  InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(content, jsonPath)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = children.map(_
  =>STRING_TYPE_INFO)

  override def toString: String = s"json_value($content, $jsonPath)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.JSON_VALUE, children.map(_.toRexNode))
  }
}

case class KeyValue(string: Expression, split1: Expression, split2: Expression, key: Expression)
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(string, split1, split2, key)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    children.map(_ => STRING_TYPE_INFO)

  override def toString: String = s"keyvalue($string, $split1, $split2, $key)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.KEY_VALUE, children.map(_.toRexNode))
  }
}

case class ParseURL(urlString: Expression, partToExtract_key: Seq[Expression])
  extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(urlString) ++ partToExtract_key

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    children.map(_ => STRING_TYPE_INFO)

  override def toString: String = s"parse_url($urlString, $partToExtract_key)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.PARSE_URL, children.map(_.toRexNode))
  }
}

case class StrToMap(args: Seq[Expression]) extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = args

  override private[flink] def resultType: TypeInformation[_] =
    Types.MAP(STRING_TYPE_INFO, STRING_TYPE_INFO)

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    children.map(_ => STRING_TYPE_INFO)

  override def toString: String = s"StrToMap($args)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.STR_TO_MAP, children.map(_.toRexNode))
  }
}

case class URLEncode(url: Expression, enc: Expression) extends Expression with InputTypeSpec {

  override private[flink] def children: Seq[Expression] = Seq(url, enc)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] =
    children.map(_ => STRING_TYPE_INFO)

  override def toString: String = s"URLEncode($url, $enc)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = {
    relBuilder.call(ScalarSqlFunctions.URL_ENCODE, children.map(_.toRexNode))
  }
}

case class URLDecode(encodedUrl: Expression, enc: Expression) extends Expression with
  InputTypeSpec {

  override private[flink] def children:Seq[Expression] = Seq(encodedUrl, enc)

  override private[flink] def resultType: TypeInformation[_] = BasicTypeInfo.STRING_TYPE_INFO

  override private[flink] def expectedTypes: Seq[TypeInformation[_]] = children.map(_ =>
    STRING_TYPE_INFO)

  override def toString: String = s"URLDecode($encodedUrl, $enc)"

  override private[flink] def toRexNode(implicit relBuilder: RelBuilder): RexNode = relBuilder
    .call(ScalarSqlFunctions.URL_DECODE, children.map(_.toRexNode))
}
