package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.Node

object DaoGenerator {
  def apply(fileName: String): DaoGenerator = new DaoGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): DaoGenerator = new DaoGenerator(modelLoader)
}

class DaoGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    generateDaoContent(xml)
      .foreach(x => save(x._1, x._2, daoSrcDir))
  }

  def generateDaoContent(xml: Node): Seq[(String, String)] = {
    xml.child.filter(_.label == "entity")
      .filter(x => ("true" != x.\@("transient")))
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) {
          generateDaoForAggregate(toAggregate(x, xml))
        } else {
          val valueObject = toValueObject(x, aggregatesTo, xml)
          generateDaoForValueObject(valueObject)
        }
      })
  }

  def defEmbeddedAggregateMessage(aggregate: Aggregate): String = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))
    if (nonKeyFieldCount > 1)
      s"""
         |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo
         |
         |def update${cToPascal(aggregate.name)}(evt: Update${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        s"""
           |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo
           |
           |def add${cToPascal(aggregate.name)}(evt: Add${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int
           |
           |def remove${cToPascal(aggregate.name)}(evt: Remove${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int
     """.stripMargin.trim
      else
        s"""
           |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo
           |
           |def change${cToPascal(aggregate.name)}(evt: Change${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int
     """.stripMargin.trim
    } else { // this cannot be happen.
      s"""
         |
     """.stripMargin.trim
    }
  }

  def defEmbeddedAggregateMessages(aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates
      .filter(!_.transient)
      .map(defEmbeddedAggregateMessage(_))
  }

  def generateDaoForAggregate(aggregate: Aggregate): (String, String) = {
    import aggregate._
    val className = s"${cToPascal(aggregate.name)}Dao"
    val fileName = s"${className}.scala"
    val calls = (
      defCrud(name, fields, primaryKey) ++
        defByForeignKeys(name, fields, foreignKeys) ++
        defMessages(aggregate.messages, aggregate.fields, aggregate.primaryKey) ++
        defEmbeddedAggregateMessages(aggregate.aggregates)
      )
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")

    val content =
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |package ${daoSrcPackage}
         |
         |import ${messageSrcPackage}._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.google.protobuf.timestamp.Timestamp
         |import java.sql.Connection
         |import java.util.UUID
         |
         |trait ${className} {
         |  ${indent(calls, 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def generateDaoForValueObject(valueObject: ValueObject): (String, String) = {
    import valueObject._
    val className = s"${cToPascal(valueObject.name)}Dao"
    val fileName = s"${className}.scala"
    val calls = (
      defCrud(name, fields, primaryKey) ++
        defByForeignKeys(name, fields, foreignKeys)
      )
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
    val content =
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |package ${daoSrcPackage}
         |
         |import ${messageSrcPackage}._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.google.protobuf.timestamp.Timestamp
         |import java.sql.Connection
         |
         |trait ${className} {
         |  ${indent(calls, 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def defMessage(message: Message): String = {
    val returnType = if ("" == message.returnType) "Int"
    else {
      val baseName = message.returnType.replace("*", "")
      val multiple = message.returnType.endsWith("*")
      if (multiple) {
        if (isAggregateEntity(baseName)) s"Seq[${cToPascal(baseName)}Vo]" else s"${cToPascal(baseName)}Vo"
      } else {
        cToPascal(toJavaType(baseName))
      }
    }
    s"""
       |def ${cToCamel(message.name)}(evt: ${cToPascal(message.name)}Event)(implicit conn: Connection): ${returnType}
     """.stripMargin.trim
  }

  def defMessages(messages: Seq[Message], parentFields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    val key = primaryKey.fields.map(_.name).toSet
    val derived = parentFields.map(_.name).filter(!key.contains(_)).toSet
    messages
      .filter(x => !x.transient && !x.fields.filter(!_.transient).filter(f => derived.contains(f.name)).isEmpty)
      .map(defMessage(_))
  }

  def defCrud(name: String, fields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    val keyFieldNames = primaryKey.fields.map(_.name).toSet
    val persistFields = fields.filter(!_.transient)
    val nonKeyPersistFields = persistFields.filter(x => !keyFieldNames.contains(x.name))

    val update = if (nonKeyPersistFields.isEmpty) {
      s"""
         |
         """.stripMargin.trim
    } else {
      s"""
         |def update${cToPascal(name)}(evt: Update${cToPascal(name)}Event)(implicit conn: Connection): Int
         """.stripMargin.trim
    }

    val offset = if(journalTable == name) {
      fields.filter("offset" == _.name)
        .map(x => {
          val offsetType = if("long" == x._type) cToPascal(x._type) else x._type.toUpperCase
          s"""
             |def selectCurrentOffset()(implicit conn: Connection): ${offsetType}
           """.stripMargin.trim
        })
        .reduceOption((l, r) => s"${l}\n${r}")
    } else None

    Seq(
      offset.getOrElse(""),
      s"""
         |def create${cToPascal(name)}(evt: Create${cToPascal(name)}Event)(implicit conn: Connection): Int
     """.stripMargin.trim,
      s"""
         |def retrieve${cToPascal(name)}(cmd: Retrieve${cToPascal(name)}Cmd)(implicit conn: Connection): ${cToPascal(name)}Vo
     """.stripMargin.trim,
      update,
      s"""
         |def delete${cToPascal(name)}(evt: Delete${cToPascal(name)}Event)(implicit conn: Connection): Int
     """.stripMargin.trim,
      s"""
         |def query${cToPascal(name)}(cmd: QueryCommand)(implicit conn: Connection): Seq[${cToPascal(name)}Vo]
     """.stripMargin.trim,
      s"""
         |def retrieve${cToPascal(name)}ByRowid(rowid: String)(implicit conn: Connection): ${cToPascal(name)}Vo
     """.stripMargin.trim
    )
  }

  def defByForeignKeys(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)
          .toSet

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        defByForeignKey(name, fkFields)
      }
    )
  }

  def defByForeignKey(name: String, keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def selectBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): Seq[${cToPascal(name)}Vo]
       |
       |def deleteBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): Int
     """.stripMargin.trim
  }
}