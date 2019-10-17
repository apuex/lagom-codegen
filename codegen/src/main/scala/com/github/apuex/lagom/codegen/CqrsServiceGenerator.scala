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
  val crudServiceGenerator = CrudServiceGenerator(modelLoader)

  def generate(): Unit = {
    save(
      s"${cToPascal(s"${modelName}_${service}_${impl}")}.scala",
      generateServiceImpl(implSrcPackage),
      implSrcDir
    )
  }

  def generateServiceImpl(srcPackage: String): String = {
    val constructorParams = Seq(
      "config: Config",
      "clusterShardingModule: ClusterShardingModule",
      "daoModule: DaoModule",
      s"eventApply: ${cToPascal(s"${modelName}_${domain}_${event}_${apply}")}",
      "mediator: ActorRef",
      "readJournal: EventsByTagQuery",
      "db: Database"
    )
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${srcPackage}
       |
       |import java.util.{Date, UUID}
       |
       |import akka._
       |import akka.actor._
       |import akka.cluster.pubsub.DistributedPubSubMediator._
       |import akka.pattern.ask
       |import akka.persistence.query._
       |import akka.persistence.query.scaladsl.EventsByTagQuery
       |import akka.stream.scaladsl._
       |import akka.stream.{OverflowStrategy, SourceShape}
       |import akka.util.Timeout
       |import ${messageSrcPackage}.ScalapbJson._
       |import ${messageSrcPackage}._
       |import ${messageSrcPackage}.${dao}.${mysql}._
       |import ${messageSrcPackage}.${shard}._
       |import com.github.apuex.events.play.EventEnvelope
       |import com.github.apuex.springbootsolution.runtime.DateFormat._
       |import com.github.apuex.springbootsolution.runtime._
       |import com.google.protobuf.any.Any
       |import com.lightbend.lagom.scaladsl.api._
       |import com.typesafe.config.Config
       |import play.api.db.Database
       |import scalapb.GeneratedMessage
       |
       |import scala.concurrent.Future
       |import scala.concurrent.duration.{Duration, FiniteDuration}
       |
       |class ${cToPascal(modelName)}ServiceImpl (${indent(constructorParams, 2)})
       |  extends ${cToPascal(modelName)}Service {
       |
       |  import clusterShardingModule._
       |  import daoModule._
       |
       |  val publishQueue = config.getString("${cToShell(modelName)}.instant-event-publish-queue")
       |  implicit val duration = Duration(config.getString("db.${cToShell(modelDbSchema)}-db.event.query-interval")).asInstanceOf[FiniteDuration]
       |  implicit val timeout = Timeout(Duration(config.getString("${cToShell(modelName)}.request-timeout")).asInstanceOf[FiniteDuration])
       |
       |  ${indent(calls(), 2)}
       |
       |  ${indent(defCurrentEvents(), 2)}
       |
       |  ${indent(defEvents(), 2)}
       |}
     """.stripMargin.trim
  }

  def calls(): String = calls(xml)
    .reduceOption((l, r) => s"${l}\n\n${r}")
    .getOrElse("")

  def calls(root: Node): Seq[String] = {
    root.child.filter(_.label == "entity")
      .filter(x => x.\@("name") != journalTable)
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) {
          val name = x.\@("name")
          if(journalTable == name) {
            val valueObject = toValueObject(x, name, root)
            generateCallsForValueObject(valueObject)
          } else {
            generateCallsForAggregate(toAggregate(x, root))
          }
        } else {
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
         |  ${cToCamel(s"${shard}_${name}")}s.ask(cmd).mapTo[${cToPascal(aggregate.name)}Vo]
         |}
     """.stripMargin.trim
    val update = if (nonKeyFieldCount > 1) {
      s"""
         |def update${cToPascal(aggregate.name)}(): ServiceCall[Update${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
         |  Future.successful({
         |    ${cToCamel(s"${shard}_${name}")}s ! cmd
         |    0
         |  })
         |}
     """.stripMargin.trim
    } else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type) {
        s"""
           |def add${cToPascal(aggregate.name)}(): ServiceCall[Add${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful({
           |    ${cToCamel(s"${shard}_${name}")}s ! cmd
           |    0
           |  })
           |}
           |
             |def remove${cToPascal(aggregate.name)}(): ServiceCall[Remove${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful({
           |    ${cToCamel(s"${shard}_${name}")}s ! cmd
           |    0
           |  })
           |}
           """.stripMargin.trim
      } else {
        s"""
           |def change${cToPascal(aggregate.name)}(): ServiceCall[Change${cToPascal(aggregate.name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful({
           |    ${cToCamel(s"${shard}_${name}")}s ! cmd
           |    0
           |  })
           |}
           """.stripMargin.trim
      }
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
      defAggregateCrudCalls(transient, name, fields, primaryKey) ++
        defByForeignKeyCalls(transient, name, fields, foreignKeys) ++
        defMessageCalls(aggregate.messages, name, fields, primaryKey) ++
        defCallsForEmbeddedAggregateMessages(aggregate.name, aggregate.aggregates)
      )
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def generateCallsForValueObject(valueObject: ValueObject): String = {
    import valueObject._
    (
      crudServiceGenerator.defCrudCalls(transient, name, fields, primaryKey) ++
        defByForeignKeyCalls(transient, name, fields, foreignKeys)
      )
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def defMessageCall(message: Message, parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): String = {
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

    val call = if("Int" == returnType)
      s"""
         |Future.successful({
         |  ${cToCamel(s"${shard}_${parentName}")}s ! cmd
         |  0
         |})
       """.stripMargin.trim
     else
      s"""
         |${cToCamel(s"${shard}_${parentName}")}s.ask(cmd).mapTo[${cToPascal(returnType)}]
       """.stripMargin.trim

    s"""
       |def ${cToCamel(message.name)}(): ServiceCall[${cToPascal(message.name)}Cmd, ${returnType}] = ServiceCall { cmd =>
       |  ${indent(call, 2)}
       |}
     """.stripMargin.trim
  }

  def defMessageCalls(messages: Seq[Message], parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    messages.map(defMessageCall(_, parentName, parentFields, primaryKey))
  }

  def defAggregateCrudCalls(transient: Boolean, name: String, fields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    val keyFieldNames = primaryKey.fields.map(_.name).toSet
    val persistFields = fields
      .filter(!_.transient)
    val nonKeyPersistFields = persistFields
      .filter(x => !keyFieldNames.contains(x.name))

    if (transient)
      Seq()
    else
      Seq(
        s"""
           |def create${cToPascal(name)}(): ServiceCall[Create${cToPascal(name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful({
           |    ${cToCamel(s"${shard}_${name}")}s ! cmd
           |    0
           |  })
           |}
     """.stripMargin.trim,
        s"""
           |def retrieve${cToPascal(name)}(): ServiceCall[Retrieve${cToPascal(name)}Cmd, ${cToPascal(name)}Vo] = ServiceCall { cmd =>
           |  ${cToCamel(s"${shard}_${name}")}s.ask(cmd).mapTo[${cToPascal(name)}Vo]
           |}
     """.stripMargin.trim,
        if (nonKeyPersistFields.isEmpty)
          s"""
             |
           """.stripMargin.trim
        else
          s"""
             |def update${cToPascal(name)}(): ServiceCall[Update${cToPascal(name)}Cmd, Int] = ServiceCall { cmd =>
             |  Future.successful({
             |    ${cToCamel(s"${shard}_${name}")}s ! cmd
             |    0
             |  })
             |}
     """.stripMargin.trim,
        s"""
           |def delete${cToPascal(name)}(): ServiceCall[Delete${cToPascal(name)}Cmd, Int] = ServiceCall { cmd =>
           |  Future.successful({
           |    ${cToCamel(s"${shard}_${name}")}s ! cmd
           |    0
           |  })
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
  }

  def defByForeignKeyCalls(transient: Boolean, name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    if (transient)
      Seq()
    else
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

  def defCurrentEvents(): String = {
    s"""
       |def currentEvents(): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
       |  ServiceCall { is =>
       |    Future.successful({
       |      val replySource = is
       |        .map(parseJson)
       |        .filter(x => x.event.isDefined)
       |        .map(x => unpack(x.event.get))
       |        .map(eventApply.on)
       |        .filter(_ => false) // to drainage
       |        .map(x => printer.print(x.asInstanceOf[GeneratedMessage]))
       |
       |      val commandSource: Source[String, ActorRef] = Source.actorRef[scala.Any](
       |        512,
       |        OverflowStrategy.dropHead)
       |        .filter(x => x.isInstanceOf[Event] || x.isInstanceOf[ValueObject])
       |        .map({
       |          case x: Event =>
       |            EventEnvelope(
       |              "",          // offset
       |              x.entityId,  // persistence_id
       |              0L,          // sequence_nr
       |              Some(pack(x.asInstanceOf[GeneratedMessage])))
       |          case x: ValueObject =>
       |            EventEnvelope(
       |              "",          // offset
       |              "",          // persistence_id
       |              0L,          // sequence_nr
       |              Some(pack(x.asInstanceOf[GeneratedMessage])))
       |        })
       |        .map(printer.print(_))
       |
       |      Source.fromGraph(GraphDSL.create() { implicit builder =>
       |        import akka.stream.scaladsl.GraphDSL.Implicits._
       |        val replyShape = builder.add(replySource)
       |        val materializedCommandSource = commandSource.mapMaterializedValue(actorRef => mediator ! Subscribe(publishQueue, actorRef))
       |        val commandShape = builder.add(materializedCommandSource)
       |
       |        val merge = builder.add(Merge[String](2))
       |
       |        replyShape ~> merge
       |        commandShape ~> merge
       |
       |        SourceShape(merge.out)
       |      })
       |    })
       |  }
       |}
     """.stripMargin.trim
  }

  def defEvents(): String = {
    s"""
       |def events(offset: Option[String]): ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = {
       |  ServiceCall { is =>
       |    Future.successful({
       |      // reply/confirm to inbound message...
       |      val replySource = is
       |        .map(parseJson)
       |        .filter(x => x.event.isDefined)
       |        .map(x => unpack(x.event.get))
       |        .map(eventApply.on)
       |        .filter(_ => false) // to drainage
       |        .map(x => printer.print(x.asInstanceOf[GeneratedMessage]))
       |
       |      val commandSource: Source[String, ActorRef] = Source.actorRef[scala.Any](
       |        512,
       |        OverflowStrategy.dropHead)
       |        .filter(x => x.isInstanceOf[Command])
       |        .map({
       |          case x: Command =>
       |            EventEnvelope(
       |              "",          // offset
       |              x.entityId,  // persistence_id
       |              0L,          // sequence_nr
       |              Some(pack(x.asInstanceOf[GeneratedMessage])))
       |        })
       |        .map(printer.print(_))
       |
       |      val eventSource = readJournal
       |        .eventsByTag(
       |          "all",
       |          offset
       |            .map(x => {
       |              if (x.matches("^[\\\\+\\\\-]{0,1}[0-9]+$$")) Offset.sequence(x.toLong)
       |              else Offset.timeBasedUUID(UUID.fromString(x))
       |            })
       |            .getOrElse(Offset.noOffset)
       |        )
       |        .filter(ee => ee.event.isInstanceOf[GeneratedMessage])
       |        .map(ee => EventEnvelope(
       |          ee.offset match {
       |            case Sequence(value) => value.toString
       |            case TimeBasedUUID(value) => value.toString
       |            case x => x.toString
       |          },
       |          ee.persistenceId,
       |          ee.sequenceNr,
       |          Some(pack(ee.event.getClass.getName, ee.event.asInstanceOf[GeneratedMessage].toByteString)))
       |        )
       |        .map(printer.print(_))
       |
       |      Source.fromGraph(GraphDSL.create() { implicit builder =>
       |        import akka.stream.scaladsl.GraphDSL.Implicits._
       |        val replyShape = builder.add(replySource)
       |        val eventShape = builder.add(eventSource)
       |        val materializedCommandSource = commandSource.mapMaterializedValue(actorRef => mediator ! Subscribe(publishQueue, actorRef))
       |        val commandShape = builder.add(materializedCommandSource)
       |
       |        val merge = builder.add(Merge[String](3))
       |
       |        replyShape ~> merge
       |        eventShape ~> merge
       |        commandShape ~> merge
       |
       |        SourceShape(merge.out)
       |      })
       |    })
       |  }
       |}
    """.stripMargin.trim
  }

  private def defPublishCmdOrEvent(transient: Boolean, noPersistFields: Boolean, returnVal: String = ""): String = {
    if (transient || noPersistFields) {
      if ("" == returnVal || "Int" == returnVal) {
        s"""
           |mediator ! Publish(publishQueue, cmd)
       """.stripMargin.trim
      } else {
        s"""
           |mediator.ask(Publish(publishQueue, cmd))(Timeout(duration))
           |  .mapTo[${returnVal}]
       """.stripMargin.trim
      }
    } else {
      s"""
         |${cToCamel(journalTable)}Dao.create${cToPascal(journalTable)}(
         |  Create${cToPascal(journalTable)}Event(cmd.userId, 0L, cmd.entityId, Some(toScalapbTimestamp(new Date())), evt.getClass.getName, evt.toByteString)
         |)
         |mediator ! Publish(publishQueue, evt)
       """.stripMargin.trim
    }
  }
}


