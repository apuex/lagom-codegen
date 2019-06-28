package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters.toJavaType

import scala.xml.Node


object CqrsServiceGenerator {
  def apply(fileName: String): CqrsServiceGenerator = new CqrsServiceGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): CqrsServiceGenerator = new CqrsServiceGenerator(modelLoader)
}

class CqrsServiceGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    save(
      s"${cToPascal(s"${modelName}_${service}_${impl}")}.scala",
      generateServiceImpl(),
      implSrcDir
    )
  }

  def generateServiceImpl(): String = {
    val constructorParams = (xml.child.filter(_.label == "entity").map(_.\@("name"))
      .map(x => s"${cToCamel(x)}Dao: ${cToPascal(x)}Dao") :+ "db: Database")
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${crudImplSrcPackage}
       |
       |import akka._
       |import akka.stream.scaladsl._
       |import ${messageSrcPackage}._
       |import ${messageSrcPackage}.dao._
       |import com.github.apuex.springbootsolution.runtime._
       |import com.google.protobuf.timestamp.Timestamp
       |import com.lightbend.lagom.scaladsl.api._
       |import play.api.db.Database
       |import play.api.libs.json.Json
       |
       |import scala.concurrent.Future
       |
       |class ${cToPascal(modelName)}ServiceImpl (${indent(constructorParams, 2)})
       |  extends ${cToPascal(modelName)}Service {
       |
       |  ${indent(calls(), 2)}
       |
       |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
       |    ServiceCall { is =>
       |      Future.successful(is.map(x => x))
       |    }
       |  }
       |}
     """.stripMargin.trim
  }

  def calls(): String = calls(xml)
    .reduceOption((l, r) => s"${l}\n\n${r}")
    .getOrElse("")

  def calls(root: Node): Seq[String] = {
    root.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) generateCallsForAggregate(toAggregate(x, root))
        else {
          val valueObject = toValueObject(x, aggregatesTo, root)
          generateCallsForValueObject(valueObject)
        }
      })
  }

  def defCallsForEmbeddedAggregateMessage(name: String, aggregate: Aggregate): String = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))
    val get =
      s"""
         |def get${cToPascal(aggregate.name)}(): ServiceCall[Get${cToPascal(aggregate.name)}Cmd, ${cToPascal(aggregate.name)}Vo] = ServiceCall { cmd =>
         |  Future.successful(
         |    db.withTransaction { implicit c =>
         |       ${cToCamel(name)}Dao.get${cToPascal(aggregate.name)}(cmd)
         |    }
         |  )
         |}
     """.stripMargin.trim
    val update = if (nonKeyFieldCount > 1)
      s"""
         |def update${cToPascal(aggregate.name)}(): ServiceCall[Update${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
         |  Future.successful(
         |    db.withTransaction { implicit c =>
         |       ${cToCamel(name)}Dao.update${cToPascal(aggregate.name)}(cmd)
         |    }
         |  )
         |}
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        s"""
           |def add${cToPascal(aggregate.name)}(): ServiceCall[Add${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful(
           |    db.withTransaction { implicit c =>
           |       ${cToCamel(name)}Dao.add${cToPascal(aggregate.name)}(cmd)
           |    }
           |  )
           |}
           |
           |def remove${cToPascal(aggregate.name)}(): ServiceCall[Remove${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful(
           |    db.withTransaction { implicit c =>
           |       ${cToCamel(name)}Dao.remove${cToPascal(aggregate.name)}(cmd)
           |    }
           |  )
           |}
     """.stripMargin.trim
      else
        s"""
           |def change${cToPascal(aggregate.name)}(): ServiceCall[Change${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful(
           |    db.withTransaction { implicit c =>
           |       ${cToCamel(name)}Dao.change${cToPascal(aggregate.name)}(cmd)
           |    }
           |  )
           |}
     """.stripMargin.trim
    } else { // this cannot be happen.
      s"""
         |
     """.stripMargin.trim
    }
    s"""
       |${get}
       |
       |${update}
     """.stripMargin.trim
  }

  def defCallsForEmbeddedAggregateMessages(name: String, aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates.map(defCallsForEmbeddedAggregateMessage(name, _))
  }

  def generateCallsForAggregate(aggregate: Aggregate): String = {
    import aggregate._
    (
      defCrudCalls(name) ++
        defByForeignKeyCalls(name, fields, foreignKeys) ++
        defMessageCalls(name, aggregate.messages) ++
        defCallsForEmbeddedAggregateMessages(aggregate.name, aggregate.aggregates)
      )
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def generateCallsForValueObject(valueObject: ValueObject): String = {
    import valueObject._
    (
      defCrudCalls(name) ++
        defByForeignKeyCalls(name, fields, foreignKeys)
      )
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def defMessageCall(name: String, message: Message): String = {
    val multiple = message.returnType.endsWith("*")
    val returnType = if ("" == message.returnType) "Int"
    else {
      val baseName = message.returnType.replace("*", "")
      if (multiple) {
        if (isAggregateEntity(baseName)) s"${cToPascal(baseName)}ListVo]" else s"${cToPascal(baseName)}Vo"
      } else {
        cToPascal(toJavaType(baseName))
      }
    }
    val daoCall = if (multiple)
      s"""
         |${returnType}(${cToCamel(name)}Dao.${cToCamel(message.name)}(cmd))
       """.stripMargin.trim
    else
      s"""
         |${cToCamel(name)}Dao.${cToCamel(message.name)}(cmd)
       """.stripMargin.trim

    s"""
       |def ${cToCamel(message.name)}(): ServiceCall[${cToPascal(message.name)}Cmd, ${returnType}] = ServiceCall { cmd =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${daoCall}
       |    }
       |  )
       |}
     """.stripMargin.trim
  }

  def defMessageCalls(name: String, messages: Seq[Message]): Seq[String] = {
    messages.map(defMessageCall(name, _))
  }

  def defCrudCalls(name: String): Seq[String] = Seq(
    s"""
       |def create${cToPascal(name)}(): ServiceCall[Create${cToPascal(name)}Cmd, Int] = ServiceCall { cmd =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToCamel(name)}Dao.create${cToPascal(name)}(cmd)
       |    }
       |  )
       |}
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}(): ServiceCall[Retrieve${cToPascal(name)}Cmd, ${cToPascal(name)}Vo] = ServiceCall { cmd =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToCamel(name)}Dao.retrieve${cToPascal(name)}(cmd)
       |    }
       |  )
       |}
     """.stripMargin.trim,
    s"""
       |def update${cToPascal(name)}(): ServiceCall[Update${cToPascal(name)}Cmd, Int] = ServiceCall { cmd =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToCamel(name)}Dao.update${cToPascal(name)}(cmd)
       |    }
       |  )
       |}
     """.stripMargin.trim,
    s"""
       |def delete${cToPascal(name)}(): ServiceCall[Delete${cToPascal(name)}Cmd, Int] = ServiceCall { cmd =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToCamel(name)}Dao.delete${cToPascal(name)}(cmd)
       |    }
       |  )
       |}
     """.stripMargin.trim,
    s"""
       |def query${cToPascal(name)}(): ServiceCall[QueryCommand, ${cToPascal(name)}ListVo] = ServiceCall { cmd =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToPascal(name)}ListVo(${cToCamel(name)}Dao.query${cToPascal(name)}(cmd))
       |    }
       |  )
       |}
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}ByRowid(rowid: String): ServiceCall[NotUsed, ${cToPascal(name)}Vo] = ServiceCall { _ =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToCamel(name)}Dao.retrieve${cToPascal(name)}ByRowid(rowid)
       |    }
       |  )
       |}
     """.stripMargin.trim
  )

  def defByForeignKeyCalls(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)
          .toSet

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        defByForeignKeyCall(name, fkFields)
      })
  }

  def defByForeignKeyCall(name: String, keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def select${cToPascal(name)}By${by}(${defMethodParams(keyFields)}): ServiceCall[NotUsed, ${cToPascal(name)}ListVo] = ServiceCall { _ =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToPascal(name)}ListVo(${cToCamel(name)}Dao.selectBy${by}(${substituteMethodParams(keyFields)}))
       |    }
       |  )
       |}
       |
       |def delete${cToPascal(name)}By${by}(${defMethodParams(keyFields)}): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
       |  Future.successful(
       |    db.withTransaction { implicit c =>
       |       ${cToCamel(name)}Dao.deleteBy${by}(${substituteMethodParams(keyFields)})
       |    }
       |  )
       |}
     """.stripMargin.trim
  }
}


