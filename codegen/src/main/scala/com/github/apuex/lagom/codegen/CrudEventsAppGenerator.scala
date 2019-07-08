package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters.toJavaType

import scala.xml.Node


object CrudEventsAppGenerator {
  def apply(fileName: String): CrudEventsAppGenerator = CrudEventsAppGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): CrudEventsAppGenerator = new CrudEventsAppGenerator(modelLoader)
}

class CrudEventsAppGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    val content = generateServiceImpl()
    save(
      s"${cToPascal(s"${modelName}_${event}_${apply}")}.scala",
      content,
      crudImplSrcDir
    )
  }

  def generateServiceImpl(): String = {
    val constructorParams = (
      xml.child.filter(_.label == "entity").map(_.\@("name"))
        .map(x => s"${cToCamel(x)}Dao: ${cToPascal(x)}Dao") ++
        Seq(
          "publishQueue: String",
          "mediator: ActorRef",
          "db: Database"
        )
      )
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${crudImplSrcPackage}
       |
       |import java.sql.Connection
       |import java.util.Date
       |
       |import akka.actor._
       |import akka.cluster.pubsub.DistributedPubSubMediator._
       |import ${messageSrcPackage}.ScalapbJson._
       |import ${messageSrcPackage}._
       |import ${messageSrcPackage}.dao._
       |import com.github.apuex.events.play.EventEnvelope
       |import com.github.apuex.springbootsolution.runtime.DateFormat._
       |import play.api.db.Database
       |
       |class ${cToPascal(s"${modelName}_${event}_${apply}")}(${indent(constructorParams, 2)}) {
       |
       |  def on(ee: EventEnvelope): Any = {
       |    db.withTransaction { implicit c =>
       |      ee.event
       |        .map(unpack)
       |        .map({
       |          case x: Event =>
       |            if (ee.sequenceNr > 0 && "" != ee.offset && "0" != ee.offset) {
       |              ${cToCamel(journalTable)}Dao.createEventJournal(
       |                Create${cToPascal(journalTable)}Event(x.userId, 0L, x.entityId, Some(toScalapbTimestamp(new Date())), x.getClass.getName, x.toByteString)
       |              )
       |              dispatch(x)
       |            }
       |          case x: ValueObject =>
       |            mediator ! Publish(publishQueue, x)
       |        })
       |    }
       |  }
       |
       |  def dispatch(msg: Any)(implicit conn: Connection): Any = msg match {
       |    ${indent(calls(), 4)}
       |  }
       |}
     """.stripMargin.trim
  }

  def calls(): String = calls(xml)
    .filter(_ != "")
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
    val update = if (nonKeyFieldCount > 1)
      s"""
         |case evt: Update${cToPascal(aggregate.name)}Event =>
         |  ${cToCamel(name)}Dao.update${cToPascal(aggregate.name)}(evt)
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        s"""
           |case evt: Add${cToPascal(aggregate.name)}Event =>
           |  ${cToCamel(name)}Dao.add${cToPascal(aggregate.name)}(evt)
           |
           |case evt: Remove${cToPascal(aggregate.name)}Event =>
           |  ${cToCamel(name)}Dao.remove${cToPascal(aggregate.name)}(evt)
     """.stripMargin.trim
      else
        s"""
           |case evt: Change${cToPascal(aggregate.name)}Event =>
           |  ${cToCamel(name)}Dao.change${cToPascal(aggregate.name)}(evt)
     """.stripMargin.trim
    } else { // this cannot be happen.
      s"""
         |
     """.stripMargin.trim
    }
    update
  }

  def defCallsForEmbeddedAggregateMessages(name: String, aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates.map(defCallsForEmbeddedAggregateMessage(name, _))
  }

  def generateCallsForAggregate(aggregate: Aggregate): String = {
    import aggregate._
    (
      defCrudCalls(name, fields, primaryKey) ++
        defByForeignKeyCalls(name, fields, foreignKeys) ++
        defMessageCalls(aggregate.messages, name, fields, primaryKey) ++
        defCallsForEmbeddedAggregateMessages(aggregate.name, aggregate.aggregates)
      )
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def generateCallsForValueObject(valueObject: ValueObject): String = {
    import valueObject._
    (
      defCrudCalls(name, fields, primaryKey) ++
        defByForeignKeyCalls(name, fields, foreignKeys)
      )
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def defMessageCall(message: Message, parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): String = {
    val key = primaryKey.fields.map(_.name).toSet
    val derived = parentFields.map(_.name).filter(!key.contains(_)).toSet
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

    val daoCall = if (message.transient || message.fields.filter(x => derived.contains(x.name)).isEmpty)
      s"""
       """.stripMargin.trim
    else if (multiple)
      s"""
         |${returnType}(
         |  ${cToCamel(parentName)}Dao.${cToCamel(message.name)}(evt)
         |)
       """.stripMargin.trim
    else
      s"""
         |${cToCamel(parentName)}Dao.${cToCamel(message.name)}(evt)
       """.stripMargin.trim

    s"""
       |case evt: ${cToPascal(message.name)}Event =>
       |  ${indent(daoCall, 6)}
     """.stripMargin.trim
  }

  def defMessageCalls(messages: Seq[Message], parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    messages.map(defMessageCall(_, parentName, parentFields, primaryKey))
  }

  def defCrudCalls(name: String, fields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = Seq(
    s"""
       |case evt: Create${cToPascal(name)}Event =>
       |  ${cToCamel(name)}Dao.create${cToPascal(name)}(evt)
     """.stripMargin.trim,
    s"""
       |case evt: Update${cToPascal(name)}Event =>
       |  ${cToCamel(name)}Dao.update${cToPascal(name)}(evt)
     """.stripMargin.trim,
    s"""
       |case evt: Delete${cToPascal(name)}Event =>
       |  ${cToCamel(name)}Dao.delete${cToPascal(name)}(evt)
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
     """.stripMargin.trim
  }
}


