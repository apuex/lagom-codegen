package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.Node

object MessageGenerator {
  def apply(fileName: String): MessageGenerator = new MessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MessageGenerator = new MessageGenerator(modelLoader)
}

class MessageGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    save(
      "ValueObject.scala",
      s"${generateValueObject(messageSrcPackage)}\n",
      messageSrcDir
    )
    save(
      "Command.scala",
      s"${generateCommand(messageSrcPackage)}\n",
      messageSrcDir
    )
    save(
      "ShardingEntityCommand.scala",
      s"${generateShardingEntityCommand(messageSrcPackage)}\n",
      messageSrcDir
    )
    save(
      "Event.scala",
      s"${generateEvent(messageSrcPackage)}\n",
      messageSrcDir
    )

    val aggregates = xml.child.filter(_.label == "entity")
      .filter(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        ("" == aggregatesTo)
      })
    aggregates
      .map(x => (x.\@("name"), generateShardingEntityCommand(x.\@("name"), getPrimaryKey(x, xml).fields, messageSrcPackage)))
      .foreach(x => save(s"${cToPascal(x._1)}Command.scala", x._2, messageSrcDir))
    aggregates
      .map(x => (x.\@("name"), generateShardingEntityEvent(x.\@("name"), getPrimaryKey(x, xml).fields, messageSrcPackage)))
      .foreach(x => save(s"${cToPascal(x._1)}Event.scala", x._2, messageSrcDir))

    save(
      "messages.proto",
      s"${generateProtoContent(xml, messageSrcPackage)}\n",
      messageProtoDir
    )
  }

  def generateProtoPrelude(messageSrcPackage: String): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |syntax = "proto3";
       |import "google/protobuf/timestamp.proto";
       |
       |package ${messageSrcPackage};
       |option java_package = "${messageSrcPackage}";
       |option java_outer_classname = "Messages";
       |option java_multiple_files = true;
       |
       |import "scalapb/scalapb.proto";
       |option (scalapb.options) = {
       |  flat_package: true
       |};
     """.stripMargin.trim
  }

  def generateProtoContent(xml: Node, messageSrcPackage: String): String = {
    val prelude = generateProtoPrelude(messageSrcPackage)
    val messages = xml.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) generateMessagesForAggregate(toAggregate(x, xml), messageSrcPackage)
        else {
          val valueObject = toValueObject(x, aggregatesTo, xml)
          val valueObjects = generateValueObject(valueObject.name, valueObject.fields, messageSrcPackage)
          if (enum) {
            val enumration = toEnumeration(x, aggregatesTo, xml)
            valueObjects ++
              generateCrudCmd(valueObject.name, valueObject.name, valueObject.fields, valueObject.primaryKey.fields, messageSrcPackage) ++
              generateEnumeration(enumration.name, enumration.options, messageSrcPackage)
          } else {
            valueObjects ++
              generateCrudCmd(aggregatesTo, valueObject.name, valueObject.fields, valueObject.primaryKey.fields, messageSrcPackage) ++
              generateCrudEvt(aggregatesTo, valueObject.name, valueObject.fields, valueObject.primaryKey.fields, messageSrcPackage)
          }
        }
      })
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")

    s"""
       |${prelude}
       |
       |${messages}
       """.stripMargin.trim
  }

  def generateMessagesForEmbeddedAggregate(entity: Aggregate, name: String, messageSrcPackage: String): Seq[String] = {
    val nonKeyFieldCount = entity.fields.length - entity.primaryKey.fields.length
    val keyFieldNames = entity.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = entity.fields.filter(x => !keyFieldNames.contains(x.name))
    val messages = if (nonKeyFieldCount > 1) Seq(
      s"""
         |message Get${cToPascal(entity.name)}Cmd {
         |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
         |  ${indent(generateFields(userField +: entity.primaryKey.fields), 2)}
         |}
       """.stripMargin.trim,
      s"""
         |message Update${cToPascal(entity.name)}Cmd {
         |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
         |  ${indent(generateFields(userField +: entity.fields), 2)}
         |}
       """.stripMargin.trim,
      s"""
         |message Update${cToPascal(entity.name)}Event{
         |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
         |  ${indent(generateFields(userField +: entity.fields), 2)}
         |}
       """.stripMargin.trim
    )
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if("array" == field._type || "map" == field._type) Seq(
        s"""
           |message Get${cToPascal(entity.name)}Cmd {
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
           |  ${indent(generateFields(userField +: entity.primaryKey.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |message Add${cToPascal(entity.name)}Cmd {
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |message Add${cToPascal(entity.name)}Event{
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |message Remove${cToPascal(entity.name)}Cmd {
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |message Remove${cToPascal(entity.name)}Event{
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim
      )
      else Seq(
        s"""
           |message Get${cToPascal(entity.name)}Cmd {
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
           |  ${indent(generateFields(userField +: entity.primaryKey.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |message Change${cToPascal(entity.name)}Cmd {
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |message Change${cToPascal(entity.name)}Event{
           |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim
      )
    } else { // this cannot be happen.
      Seq()
    }
    generateValueObject(entity.name, entity.fields, messageSrcPackage) ++ messages
  }

  def generateMessagesForAggregate(entity: Aggregate, messageSrcPackage: String): Seq[String] = {
    entity.aggregates.map(generateMessagesForEmbeddedAggregate(_, entity.name, messageSrcPackage)).flatMap(x => x) ++
      generateValueObject(entity.name, entity.fields, messageSrcPackage) ++
      generateCrudCmd(entity.name, entity.name, entity.fields, entity.primaryKey.fields, messageSrcPackage) ++
      (if (entity.transient) Seq() else generateCrudEvt(entity.name, entity.name, entity.fields, entity.primaryKey.fields, messageSrcPackage)) ++
      generateMessages(entity.messages, entity.name, messageSrcPackage) ++
      (if (entity.transient) Seq() else generateEvents(entity.messages, entity.name, messageSrcPackage))
  }

  def generateMessage(message: Message, name: String, messageSrcPackage: String): String = {
    s"""
       |message ${cToPascal(message.name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
       |  ${indent(generateFields(userField +: message.fields), 2)}
       |}
     """.stripMargin.trim
  }

  def generateEvent(message: Message, name: String, messageSrcPackage: String): String = {
    s"""
       |message ${cToPascal(message.name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
       |  ${indent(generateFields(userField +: message.fields), 2)}
       |}
     """.stripMargin.trim
  }

  def generateMessages(messages: Seq[Message], name: String, messageSrcPackage: String): Seq[String] = {
    messages
      .map(x => generateMessage(x, name, messageSrcPackage))
  }

  def generateEvents(messages: Seq[Message], name: String, messageSrcPackage: String): Seq[String] = {
    messages
      .filter(!_.transient)
      .map(x => generateEvent(x, name, messageSrcPackage))
  }

  def generateCrudEvt(aggregate: String, name: String, fields: Seq[Field], pkFields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |message Create${cToPascal(name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Event";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Update${cToPascal(name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Event";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Delete${cToPascal(name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Event";
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim
  )

  def generateCrudCmd(aggregate: String, name: String, fields: Seq[Field], pkFields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |message Create${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Command";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Retrieve${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Command";
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Update${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Command";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Delete${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(aggregate)}Command";
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim
  )

  def generateValueObject(name: String, fields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |message ${cToPascal(name)}Vo {
       |  option (scalapb.message).extends = "${messageSrcPackage}.ValueObject";
       |  ${indent(generateFields(fields), 2)}
       |}
       |
       |message ${cToPascal(name)}ListVo {
       |  option (scalapb.message).extends = "${messageSrcPackage}.ValueObject";
       |  repeated ${cToPascal(name)}Vo items = 1;
       |}
     """.stripMargin.trim
  )

  def generateEnumOptions(options: Seq[EnumOption]): String = {
    options
      .map(x => s"${x.name} = ${x.value}; // ${x.label}")
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def generateEnumeration(name: String, options: Seq[EnumOption], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |enum ${cToPascal(name)} {
       |  option (scalapb.enum_options).extends = "${messageSrcPackage}.ValueObject";
       |  ${indent(generateEnumOptions(options), 2)}
       |}
    """.stripMargin.trim
  )

  def generateField(field: Field, no: Int): String = {
    import field._

    val protobufType = if ("array" == _type) s"repeated ${toProtobufType(valueType)}${if (isAggregateEntity(valueType)) "Vo" else ""}"
    else if ("map" == _type) s"map <${toProtobufType(keyType)}${if (isAggregateEntity(keyType)) "Vo" else ""}, ${toProtobufType(valueType)}${if (isAggregateEntity(valueType)) "Vo" else ""}>"
    else s"${toProtobufType(_type)}${if (isAggregateEntity(_type)) "Vo" else ""}"

    s"""
       |${protobufType} ${cToCamel(name)} = ${no}; // ${comment}
    """.stripMargin.trim
  }

  def generateFields(fields: Seq[Field]): String = {
    var no = 0
    fields
      .map(x => {
        no += 1
        generateField(x, no)
      }
      )
      .map(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }

  def generateValueObject(messageSrcPackage: String): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${messageSrcPackage}
       |
       |trait ValueObject
     """.stripMargin.trim
  }

  def generateCommand(messageSrcPackage: String): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${messageSrcPackage}
       |
       |trait Command {
       |  def userId: String
       |}
     """.stripMargin.trim
  }

  def generateEntityIdFields(name: String, keyFields: Seq[Field]): (String, String) = {
    var dependencies: Set[String] = Set()
    var entityIdFields = keyFields
      .map(x => {
        if ("timestamp" == x._type) {
          dependencies += "com.google.protobuf.timestamp.Timestamp"
          s"""
             |def ${cToCamel(x.name)}: Option[${cToPascal(toJavaType(x._type))}]
         """.stripMargin.trim
        } else {
          s"""
             |def ${cToCamel(x.name)}: ${cToPascal(toJavaType(x._type))}
         """.stripMargin.trim
        }
      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    (dependencies
      .map(x => s"import ${x}")
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse(""),
      entityIdFields)
  }

  def generateEntityId(name: String, keyFields: Seq[Field]): (String, String) = {
    var dependencies: Set[String] = Set()
    val key = keyFields
      .map(x => if ("timestamp" == x._type) {
        dependencies += "com.google.protobuf.timestamp.Timestamp"
        dependencies += "com.github.apuex.springbootsolution.runtime.DateFormat._"
        s"""
           |$${${cToCamel(x.name)}.map(x => formatTimestamp(x.seconds * 1000 + x.nanos / 1000000))}
         """.stripMargin.trim
      } else {
        s"""
           |$${${cToCamel(x.name)}}
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}_${r}")
      .getOrElse("")

    (dependencies
      .map(x => s"import ${x}")
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse(""),
      s"""
         |s"${cToCamel(name)}_${key}"
     """.stripMargin.trim)
  }

  def generateShardingEntityCommand(name: String, keyFields: Seq[Field], messageSrcPackage: String): String = {
    val (dependencies, entityId) = generateEntityId(name, keyFields)
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${messageSrcPackage}
       |${dependencies}
       |
       |trait ${cToPascal(name)}Command extends ShardingEntityCommand {
       |  ${indent(generateEntityIdFields(name, keyFields)._2, 2)}
       |  override def entityId: String = {
       |    ${entityId}
       |  }
       |}
     """.stripMargin.trim
  }

  def generateShardingEntityEvent(name: String, keyFields: Seq[Field], messageSrcPackage: String): String = {
    val (dependencies, entityIdFields) = generateEntityIdFields(name, keyFields)
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${messageSrcPackage}
       |${dependencies}
       |
       |trait ${cToPascal(name)}Event extends Event {
       |  ${indent(entityIdFields, 2)}
       |}
     """.stripMargin.trim
  }

  def generateShardingEntityCommand(messageSrcPackage: String): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${messageSrcPackage}
       |
       |trait ShardingEntityCommand extends Command {
       |  def entityId: String
       |}
     """.stripMargin.trim
  }

  def generateEvent(messageSrcPackage: String): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${messageSrcPackage}
       |
       |trait Event {
       |  def userId: String
       |}
     """.stripMargin.trim
  }
}
