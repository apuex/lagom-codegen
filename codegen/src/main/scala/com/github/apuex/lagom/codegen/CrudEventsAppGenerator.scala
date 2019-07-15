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
    save(
      s"${cToPascal(s"${modelName}_${query}_${event}_${apply}")}.scala",
      generateServiceImpl(crudImplSrcPackage),
      crudImplSrcDir
    )
  }

  def generateServiceImpl(srcPackage: String): String = {
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
       |package ${srcPackage}
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
       |class ${cToPascal(s"${modelName}_${query}_${event}_${apply}")}(${indent(constructorParams, 2)}) {
       |
       |  def on(ee: EventEnvelope): Any = {
       |    db.withTransaction { implicit c =>
       |      ee.event
       |        .map(unpack)
       |        .map({
       |          case x: Event =>
       |            ${cToCamel(journalTable)}Dao.createEventJournal(
       |              Create${cToPascal(journalTable)}Event(x.userId, 0L, x.entityId, Some(toScalapbTimestamp(new Date())), x.getClass.getName, x.toByteString)
       |            )
       |            dispatch(x)
       |          case x: ValueObject =>
       |            mediator ! Publish(publishQueue, x)
       |        })
       |    }
       |  }
       |
       |  def dispatch(msg: Any)(implicit conn: Connection): Any = msg match {
       |    ${indent(calls(), 4)}
       |    case _ => None
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
        val transient = if ("true" == x.\@("transient")) true else false
        val enum = if ("true" == x.\@("enum")) true else false
        if (transient) {
          ""
        } else {
          if (!enum && "" == aggregatesTo) {
            val aggregate = toAggregate(x, root)
            if (aggregate.fields.filter(!_.transient).isEmpty)
              ""
            else
              generateCallsForAggregate(aggregate)
          }
          else {
            val valueObject = toValueObject(x, aggregatesTo, root)
            if (valueObject.fields.filter(!_.transient).isEmpty)
              ""
            else
              generateCallsForValueObject(valueObject)
          }
        }
      })
  }

  def defCallsForEmbeddedAggregateMessage(name: String, aggregate: Aggregate): String = {
    val persistFields = aggregate.fields.filter(!_.transient)
    val nonKeyFieldCount = persistFields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = persistFields
      .filter(x => !keyFieldNames.contains(x.name))
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
        defMessageCalls(aggregate.messages, name, fields, primaryKey) ++
        defCallsForEmbeddedAggregateMessages(aggregate.name, aggregate.aggregates)
      )
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def generateCallsForValueObject(valueObject: ValueObject): String = {
    import valueObject._
    defCrudCalls(name, fields, primaryKey)
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def defMessageCall(message: Message, parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): String = {
    val key = primaryKey.fields.map(_.name).toSet
    val derived = parentFields.map(_.name).filter(!key.contains(_)).toSet
    val multiple = message.returnType.endsWith("*")
    val returnType = if ("" == message.returnType) ""
    else {
      val baseName = message.returnType.replace("*", "")
      if (multiple) {
        if (isAggregateEntity(baseName)) s"${cToPascal(baseName)}ListVo" else s"${cToPascal(baseName)}Vo"
      } else {
        cToPascal(toJavaType(baseName))
      }
    }

    val hasPersistField = message.fields
      .filter(!_.transient)
      .filter(x => derived.contains(x.name)).isEmpty
    val daoCall = if (message.transient || hasPersistField)
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

    if (message.transient || hasPersistField)
      ""
    else
      s"""
         |case evt: ${cToPascal(message.name)}Event =>
         |  ${indent(daoCall, 6)}
     """.stripMargin.trim
  }

  def defMessageCalls(messages: Seq[Message], parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    messages.map(defMessageCall(_, parentName, parentFields, primaryKey))
  }

  def defCrudCalls(name: String, fields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    val keyFieldNames = primaryKey.fields.map(_.name).toSet
    val persistFields = fields.filter(!_.transient)
    val nonKeyPersistFields = persistFields.filter(x => !keyFieldNames.contains(x.name))

    Seq(
      s"""
         |case evt: Create${cToPascal(name)}Event =>
         |  ${cToCamel(name)}Dao.create${cToPascal(name)}(evt)
     """.stripMargin.trim,
      if (nonKeyPersistFields.isEmpty)
        ""
      else
        s"""
           |case evt: Update${cToPascal(name)}Event =>
           |  ${cToCamel(name)}Dao.update${cToPascal(name)}(evt)
     """.stripMargin.trim,
      s"""
         |case evt: Delete${cToPascal(name)}Event =>
         |  ${cToCamel(name)}Dao.delete${cToPascal(name)}(evt)
     """.stripMargin.trim
    )
  }
}


