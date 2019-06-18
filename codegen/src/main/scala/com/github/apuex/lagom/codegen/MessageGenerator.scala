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

  def generate(): Unit = {
    xml.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        if ("" == aggregatesTo) messagesForAggregate(toAggregate(x, xml))
        else generateValueObject(toValueObject(x, aggregatesTo, xml))
      })
  }
}

object MessageGenerator {
  def apply(fileName: String): MessageGenerator = new MessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MessageGenerator = new MessageGenerator(modelLoader)

  def messagesForAggregate(entity: Aggregate, primaryKey: PrimaryKey): Seq[String] = {
    Seq(
      s"""
         |
       """.stripMargin.trim
    )
  }

  def messagesForAggregate(entity: Aggregate): Seq[String] = {
    entity.aggregates.map(messagesForAggregate(_, entity.primaryKey)).flatMap(x => x) ++
      Seq(generateValueObject(entity)) ++
      generateCommands(entity) ++
      generateEvents(entity)
  }

  def generateCommands(entity: Aggregate): Seq[String] = Seq()

  def generateEvents(entity: Aggregate): Seq[String] = Seq()

  def generateValueObject(entity: Aggregate, primaryKey: PrimaryKey): String = {
    s"""
       |message ${cToPascal(entity.name)}Vo {
       |  ${indent(generateFields(entity.fields), 2)}
       |}
     """.stripMargin
  }

  def generateValueObject(entity: Aggregate): String = {
    s"""
       |message ${cToPascal(entity.name)}Vo {
       |  ${indent(generateFields(entity.fields), 2)}
       |}
     """.stripMargin
  }

  def generateValueObject(entity: ValueObject): String = {
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

  def getFields(node: Node, root: Node): Seq[Field] = {
    val foreignKeys = getForeignKeys(node)
    val referenced = foreignKeys
      .flatMap(x => x.fields.map(f => getReferencedColumn(f.name, foreignKeys, root)))
      .map(_.get)

    val defined = node.child.filter(_.label == "field")
      .map(x => x.\@("type") match {
        case "" =>
          val refKey = x.\@("refKey")
          val refEntity = foreignKeys.filter(_.name == refKey)
            .map(_.refEntity).head
          getReferencedColumn(x.\@("name"), x.\@("refKey"), refEntity, x.\@("refField"), root)
        case _ => Some(toField(x))
      })
      .map(_.get) // throws java.util.NoSuchElementException if the option is empty.

    val all = defined ++ referenced

    all
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
      .map(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def getPrimaryKey(node: Node, root: Node): PrimaryKey = {
    val pks = node.child.filter(_.label == "primaryKey")
    if (pks.isEmpty) {
      val aggregatesTo = node.\@("aggregatesTo")
      getPrimaryKey(root.child.filter(_.label == aggregatesTo).head, root)
    } else {
      val pk = pks.head
      val pkName = pk.\@("name")
      val pkColumnNames = pk.child.filter(_.label == "field")
        .map(_.\@("name"))

      val foreignKeys = getForeignKeys(node)

      val fields = getFields(node, root)
        .map(x => (x.name -> x))
        .toMap

      val pkColumns = pkColumnNames
        .map(x => {
          fields.getOrElse(x, getReferencedColumn(x, foreignKeys, root).get)
        })

      PrimaryKey(pkName, pkColumns)
    }
  }

  def getReferencedColumn(name: String, foreignKeys: Seq[ForeignKey], root: Node): Option[Field] = {
    //println(s"getReferencedColumn: ${name}, ${foreignKeys}")
    val (fkField, refField) = foreignKeys
      .map(k => (k, k.fields.filter(_.name == name)))
      .filter(x => !x._2.isEmpty)
      .map(x => (x._1, x._2.head))
      .map(x => (x, getReferencedColumn(x._2.name, x._1.name, x._1.refEntity, x._2.refField, root)))
      .map(x => (x._1._2, x._2))
      .head

    refField
      .map(x => Field(fkField.name, x._type, x.length, fkField.required, x.keyType, x.valueType, x.aggregate, x.comment))
  }

  def getReferencedColumn(name: String, refKey: String, refEntity: String, refField: String, root: Node): Option[Field] = {
    val node = root.child.filter(x => x.label == "entity" && x.\@("name") == refEntity).head
    Some(getFields(node, root)
      .filter(_.name == refField).head)
  }

  def getForeignKeys(node: Node): Seq[ForeignKey] = {
    node.child.filter(_.label == "foreignKey")
      .map(x => {
        ForeignKey(
          x.\@("name"),
          x.\@("refEntity"),
          x.child.filter(_.label == "field")
            .map(f => ForeignKeyField(f.\@("name"), f.\@("refField"), if ("true" == f.\@("required")) true else false))
        )
      })
  }

  def toAggregate(node: Node, primaryKey: PrimaryKey, root: Node): Aggregate = {
    Aggregate(
      node.\@("name"),
      if ("true" == node.\@("root")) true else false,
      getFields(node, root),
      Seq(),
      primaryKey,
      Seq()
    )
  }

  def toAggregate(node: Node, root: Node): Aggregate = {
    val primaryKey = getPrimaryKey(node, root)
    val fields = getFields(node, root)
    Aggregate(
      node.\@("name"),
      if ("true" == node.\@("root")) true else false,
      getFields(node, root),
      node.child.filter(_.label == "aggregate").map(toAggregate(_, primaryKey, root)),
      primaryKey,
      getForeignKeys(node)
    )
  }

  def toValueObject(node: Node, aggregatesTo: String, root: Node): ValueObject = {
    ValueObject(
      node.\@("name"),
      getFields(node, root),
      getPrimaryKey(node, root),
      getForeignKeys(node)
    )
  }

  def save(fileName: String, content: String, dir: String): Unit = {
    new File(dir).mkdirs()
    val pw = new PrintWriter(new File(dir, fileName), "utf-8")
    pw.println(content)
    pw.close()
  }
}
