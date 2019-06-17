package com.github.apuex.lagom.codegen

import java.io._

import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TypeConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.lagom.codegen.MessageGenerator._

import scala.xml.Node

class MessageGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  val entities: Map[String, Node] = xml.child.filter(_.label == "entity")
    .map(x => x.\@("name") -> x)
    .toMap

  def generate(): Unit = {
    entities.map(_._2)
      .map(messagesForEntity)
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

}

object MessageGenerator {
  def apply(fileName: String): MessageGenerator = new MessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MessageGenerator = new MessageGenerator(modelLoader)

  def messagesForEntity(node: Node): Seq[String] = {
    Seq(valueObject(node)) ++ commands(node) ++ events(node)
  }

  case class Field(no: String, name: String, _type: String, length: String, keyType: String, valueType: String, comment: String)

  def commands(node: Node): Seq[String] = Seq()

  def events(node: Node): Seq[String] = Seq()

  def valueObject(node: Node): String = {
    s"""
       |message ${cToPascal(node.\@("name"))}Vo {
       |  ${indent(fields(node), 2)}
       |}
     """.stripMargin
  }

  def toField(node: Node): Field = {
    val no = node.\@("no")
    val name = node.\@("no")
    val _type = node.\@("type")
    val length = node.\@("length")
    val keyType = node.\@("keyType")
    val valueType = node.\@("valueType")
    val comment = node.\@("comment")
    Field(no, name, _type, length, keyType, valueType, comment)
  }

  def field(field: Field): String = {
    import field._
    val protobufType = if ("array" == _type) s"repeated ${toProtobufType(valueType)}"
    else if ("map" == _type) s"map <${toProtobufType(keyType)}, ${toProtobufType(valueType)}>"
    else toProtobufType(_type)

    s"""
       |${protobufType} ${cToCamel(name)} = ${no} // ${comment}
     """.stripMargin
  }

  def fields(node: Node): String = {
    node.child.filter(_.label == "field")
      .map(toField)
      .map(field)
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def save(fileName: String, content: String, dir: String): Unit = {
    new File(dir).mkdirs()
    val pw = new PrintWriter(new File(dir, fileName), "utf-8")
    pw.println(content)
    pw.close()
  }
}
