package com.github.apuex.lagom.codegen

import java.io._

import com.github.apuex.lagom.codegen.MessageGenerator._
import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.Node

class MessageGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  val aggregates: Map[String, Aggregate] = xml.child.filter(_.label == "aggregate")
    .map(toAggregate)
    .map(x => x.name -> x)
    .toMap

  val valueObjects: Map[String, ValueObject] = xml.child.filter(_.label == "value-object")
    .map(toValueObject)
    .map(x => x.name -> x)
    .toMap

  def generate(): Unit = {
    aggregates.map(_._2)
      .map(messagesForAggregate)
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def toAggregate(node: Node): Aggregate = {
    Aggregate(
      node.\@("name"),
      if ("true" == node.\@("root")) true else false,
      node.child.filter(_.label == "field").map(toField),
      getPrimaryKey(node),
      getForeignKeys(node)
    )
  }

  def toValueObject(node: Node): ValueObject = {
    ValueObject(
      node.\@("name"),
      node.child.filter(_.label == "field").map(toField),
      getPrimaryKey(node),
      getForeignKeys(node)
    )
  }
}

object MessageGenerator {
  def apply(fileName: String): MessageGenerator = new MessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MessageGenerator = new MessageGenerator(modelLoader)


  def messagesForAggregate(entity: Aggregate): Seq[String] = {
    Seq(generateValueObject(entity)) ++ generateCommands(entity) ++ generateEvents(entity)
  }

  def generateCommands(entity: Aggregate): Seq[String] = Seq()

  def generateEvents(entity: Aggregate): Seq[String] = Seq()

  def generateValueObject(entity: Aggregate): String = {
    s"""
       |message ${cToPascal(entity.name)}Vo {
       |  ${indent(generateFields(entity.fields), 2)}
       |}
     """.stripMargin
  }

  def toField(node: Node): Field = {
    val name = node.\@("name")
    val _type = node.\@("type")
    val length = if ("" == node.\@("length")) 0 else node.\@("length").toInt
    val required = if ("true" == node.\@("required")) true else false
    val keyType = node.\@("keyType")
    val valueType = node.\@("valueType")
    val aggregate = if ("true" == node.\@("aggregate")) true else false
    val comment = node.\@("comment")
    Field(name, _type, length, required, keyType, valueType, aggregate, comment)
  }

  def generateField(field: Field, no: Int): String = {
    import field._
    val protobufType = if ("array" == _type) s"repeated ${toProtobufType(valueType)}"
    else if ("map" == _type) s"map <${toProtobufType(keyType)}, ${toProtobufType(valueType)}>"
    else toProtobufType(_type)

    s"""
       |${protobufType} ${cToCamel(name)} = ${no} // ${comment}
     """.stripMargin
  }

  def generateFields(fields: Seq[Field]): String = {
    var no = 0
    fields
      .map(x => {
        no += 1
        generateField(x, no)
      })
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def getPrimaryKey(node: Node): PrimaryKey = {

  }

  def getForeignKeys(node: Node): Seq[ForeignKey] = {

  }

  def save(fileName: String, content: String, dir: String): Unit = {
    new File(dir).mkdirs()
    val pw = new PrintWriter(new File(dir, fileName), "utf-8")
    pw.println(content)
    pw.close()
  }
}
