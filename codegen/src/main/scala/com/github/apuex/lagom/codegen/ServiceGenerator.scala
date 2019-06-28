package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters.toJavaType

import scala.xml.Node


object ServiceGenerator {
  def apply(fileName: String): ServiceGenerator = new ServiceGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ServiceGenerator = new ServiceGenerator(modelLoader)
}

class ServiceGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    save(
      s"${cToPascal(s"${modelName}_${service}")}.scala",
      generateService(),
      apiSrcDir
    )
  }

  def generateService(): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${apiSrcPackage}
       |
       |import ${messageSrcPackage}._
       |import com.github.apuex.springbootsolution.runtime._
       |import com.google.protobuf.timestamp.Timestamp
       |import akka._
       |import akka.stream.scaladsl._
       |import com.lightbend.lagom.scaladsl.api._
       |import play.api.libs.json.Json
       |
       |trait ${cToPascal(modelName)}Service extends Service {
       |
       |  ${indent(calls(), 2)}
       |
       |  def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]
       |
       |  override def descriptor: Descriptor = {
       |    import Service._
       |    import ScalapbJson._
       |
       |    ${indent(callJsonFormats(), 4)}
       |
       |    named("${cToShell(modelName)}")
       |      .withCalls(
       |        pathCall("/api/events?offset", events _),
       |        ${indent(callDescs(), 8)}
       |      ).withAutoAcl(true)
       |  }
       |}
     """.stripMargin.trim
  }

  def calls(): String = calls(xml)
    .reduceOption((l, r) => s"${l}\n\n${r}")
    .getOrElse("")

  def callJsonFormats(): String = ""

  def callDescs(): String = callDescs(xml)
    .reduceOption((l, r) => s"${l},\n${r}")
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

  def defCallsForEmbeddedAggregateMessage(aggregate: Aggregate): String = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))
    val get =
      s"""
         |def get${cToPascal(aggregate.name)}(): ServiceCall[Get${cToPascal(aggregate.name)}Cmd, ${cToPascal(aggregate.name)}Vo]
     """.stripMargin.trim
    val update = if (nonKeyFieldCount > 1)
      s"""
         |def update${cToPascal(aggregate.name)}(): ServiceCall[Update${cToPascal(aggregate.name)}Cmd, Int]
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        s"""
           |def add${cToPascal(aggregate.name)}(): ServiceCall[Add${cToPascal(aggregate.name)}Cmd, Int]
           |
           |def remove${cToPascal(aggregate.name)}(): ServiceCall[Remove${cToPascal(aggregate.name)}Cmd, Int]
     """.stripMargin.trim
      else
        s"""
           |def change${cToPascal(aggregate.name)}(): ServiceCall[Change${cToPascal(aggregate.name)}Cmd, Int]
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

  def defCallsForEmbeddedAggregateMessages(aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates.map(defCallsForEmbeddedAggregateMessage(_))
  }

  def generateCallsForAggregate(aggregate: Aggregate): String = {
    import aggregate._
    (
      defCrudCalls(name) ++
        defByForeignKeyCalls(name, fields, foreignKeys) ++
        defMessageCalls(aggregate.messages) ++
        defCallsForEmbeddedAggregateMessages(aggregate.aggregates)
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

  def defMessageCall(message: Message): String = {
    val returnType = if ("" == message.returnType) "Int"
    else {
      val baseName = message.returnType.replace("*", "")
      val multiple = message.returnType.endsWith("*")
      if (multiple) {
        if (isAggregateEntity(baseName)) s"${cToPascal(baseName)}ListVo]" else s"${cToPascal(baseName)}Vo"
      } else {
        cToPascal(toJavaType(baseName))
      }
    }
    s"""
       |def ${cToCamel(message.name)}(): ServiceCall[${cToPascal(message.name)}Cmd, ${returnType}]
     """.stripMargin.trim
  }

  def defMessageCalls(messages: Seq[Message]): Seq[String] = {
    messages.map(defMessageCall(_))
  }

  def defCrudCalls(name: String): Seq[String] = Seq(
    s"""
       |def create${cToPascal(name)}(): ServiceCall[Create${cToPascal(name)}Cmd, Int]
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}(): ServiceCall[Retrieve${cToPascal(name)}Cmd, ${cToPascal(name)}Vo]
     """.stripMargin.trim,
    s"""
       |def update${cToPascal(name)}(): ServiceCall[Update${cToPascal(name)}Cmd, Int]
     """.stripMargin.trim,
    s"""
       |def delete${cToPascal(name)}(): ServiceCall[Delete${cToPascal(name)}Cmd, Int]
     """.stripMargin.trim,
    s"""
       |def query${cToPascal(name)}(): ServiceCall[QueryCommand, ${cToPascal(name)}ListVo]
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}ByRowid(rowid: String): ServiceCall[NotUsed, ${cToPascal(name)}Vo]
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
       |def select${cToPascal(name)}By${by}(${defMethodParams(keyFields)}): ServiceCall[NotUsed, ${cToPascal(name)}ListVo]
       |
       |def delete${cToPascal(name)}By${by}(${defMethodParams(keyFields)}): ServiceCall[NotUsed, Int]
     """.stripMargin.trim
  }

  def callDescs(root: Node): Seq[String] = {
    root.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) generateCallDescsForAggregate(toAggregate(x, root))
        else {
          val valueObject = toValueObject(x, aggregatesTo, root)
          generateCallDescsForValueObject(valueObject)
        }
      })
      .flatMap(x => x)
  }

  def defCallDescsForEmbeddedAggregateMessage(aggregate: Aggregate): Seq[String] = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))
    val get = Seq(
      s"""
         |pathCall("/api/${cToShell(aggregate.name)}/get-${cToShell(aggregate.name)}", get${cToPascal(aggregate.name)} _)
     """.stripMargin.trim)
    val update = if (nonKeyFieldCount > 1)
      Seq(
        s"""
           |pathCall("/api/${cToShell(aggregate.name)}/update-${cToShell(aggregate.name)}", update${cToPascal(aggregate.name)} _)
     """.stripMargin.trim)
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        Seq(
          s"""
             |pathCall("/api/${cToShell(aggregate.name)}/add-${cToShell(aggregate.name)}", add${cToPascal(aggregate.name)} _)
     """.stripMargin.trim,
          s"""
             |pathCall("/api/${cToShell(aggregate.name)}/remove-${cToShell(aggregate.name)}", remove${cToPascal(aggregate.name)} _)
     """.stripMargin.trim)
      else
        Seq(s"""
           |pathCall("/api/${cToShell(aggregate.name)}/change-${cToShell(aggregate.name)}", change${cToPascal(aggregate.name)} _)
     """.stripMargin.trim)
    } else { // this cannot be happen.
      Seq(s"""
         |
     """.stripMargin.trim)
    }
    get ++ update
  }

  def defCallDescsForEmbeddedAggregateMessages(aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates
      .map(defCallDescsForEmbeddedAggregateMessage(_))
      .flatMap(x => x)
  }

  def generateCallDescsForAggregate(aggregate: Aggregate): Seq[String] = {
    import aggregate._
    (
      defCrudCallDescs(name) ++
        defByForeignKeyCallDescs(name, fields, foreignKeys) ++
        defMessageCallDescs(aggregate.name, aggregate.messages) ++
        defCallDescsForEmbeddedAggregateMessages(aggregate.aggregates)
      )
  }

  def generateCallDescsForValueObject(valueObject: ValueObject): Seq[String] = {
    import valueObject._
    (
      defCrudCallDescs(name) ++
        defByForeignKeyCallDescs(name, fields, foreignKeys)
      )
  }

  def defMessageCallDesc(name: String, message: Message): String = {
    s"""
       |pathCall("/api/${cToShell(name)}/${cToShell(message.name)}", ${cToCamel(message.name)} _)
     """.stripMargin.trim
  }

  def defMessageCallDescs(name: String, messages: Seq[Message]): Seq[String] = {
    messages.map(defMessageCallDesc(name, _))
  }

  def defCrudCallDescs(name: String): Seq[String] = Seq(
    s"""
       |pathCall("/api/${cToShell(name)}/create-${cToShell(name)}", create${cToPascal(name)} _)
     """.stripMargin.trim,
    s"""
       |pathCall("/api/${cToShell(name)}/retrieve-${cToShell(name)}", retrieve${cToPascal(name)} _)
     """.stripMargin.trim,
    s"""
       |pathCall("/api/${cToShell(name)}/update-${cToShell(name)}", update${cToPascal(name)} _)
     """.stripMargin.trim,
    s"""
       |pathCall("/api/${cToShell(name)}/delete-${cToShell(name)}", delete${cToPascal(name)} _)
     """.stripMargin.trim,
    s"""
       |pathCall("/api/${cToShell(name)}/query-${cToShell(name)}", query${cToPascal(name)} _)
     """.stripMargin.trim,
    s"""
       |pathCall("/api/${cToShell(name)}/retrieve-${cToShell(name)}-by-rowid/:rowid", retrieve${cToPascal(name)}ByRowid _)
     """.stripMargin.trim
  )

  def defByForeignKeyCallDescs(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)
          .toSet

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        defByForeignKeyCallDesc(name, fkFields)
      })
      .flatMap(x => x)
  }

  def defByForeignKeyCallDesc(name: String, keyFields: Seq[Field]): Seq[String] = {
    val byMethod = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}-${y}")
      .getOrElse("")

    val byPath = keyFields
      .map(x => cToShell(x.name))
      .reduceOption((x, y) => s"${x}-${y}")
      .getOrElse("")

    Seq(
      s"""
         |pathCall("/api/${cToShell(name)}/select-${cToShell(name)}-by-${byPath}?${defQueryParams(keyFields)}", select${cToPascal(name)}By${byMethod} _)
     """.stripMargin.trim,
      s"""
         |pathCall("/api/${cToShell(name)}/delete-${cToShell(name)}-by-${byPath}?${defQueryParams(keyFields)}", delete${cToPascal(name)}By${byMethod} _)
     """.stripMargin.trim,
    )
  }

  def defQueryParams(fields: Seq[Field]): String = {
    fields.map(x => cToCamel(x.name))
      .reduceOption((x, y) => s"${x}&${y}")
      .getOrElse("")
  }
}


