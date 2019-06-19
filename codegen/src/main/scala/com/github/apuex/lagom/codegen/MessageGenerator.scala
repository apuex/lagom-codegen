package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.MessageGenerator._
import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

class MessageGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    val prelude = generateProtoPrelude(messageSrcPackage)
    val messages = xml.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        if ("" == aggregatesTo) generateMessagesForAggregate(toAggregate(x, xml), messageSrcPackage)
        else {
          val valueObject = toValueObject(x, aggregatesTo, xml)
          generateValueObject(valueObject.name, valueObject.fields, messageSrcPackage)
        }
      })
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")

    val content =
      s"""
         |${prelude}
         |
         |${messages}
       """.stripMargin.trim
    save(
      "messages.proto",
      s"${content}\n",
      messageSrcDir
    )
  }
}

object MessageGenerator {
  def apply(fileName: String): MessageGenerator = new MessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MessageGenerator = new MessageGenerator(modelLoader)

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

  def generateMessagesForEmbeddedAggregate(entity: Aggregate, name: String, messageSrcPackage: String): Seq[String] = {
    generateValueObject(entity.name, entity.fields, messageSrcPackage) ++
    Seq(
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
  }

  def generateMessagesForAggregate(entity: Aggregate, messageSrcPackage: String): Seq[String] = {
    entity.aggregates.map(generateMessagesForEmbeddedAggregate(_, entity.name, messageSrcPackage)).flatMap(x => x) ++
      generateValueObject(entity.name, entity.fields, messageSrcPackage) ++
      generateCrudCmd(entity.name, entity.fields, entity.primaryKey.fields, messageSrcPackage) ++
      (if (entity.transient) Seq() else generateCrudEvt(entity.name, entity.fields, entity.primaryKey.fields, messageSrcPackage)) ++
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

  def generateCrudEvt(name: String, fields: Seq[Field], pkFields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |message Create${cToPascal(name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Update${cToPascal(name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Delete${cToPascal(name)}Event {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Event";
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim
  )

  def generateCrudCmd(name: String, fields: Seq[Field], pkFields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |message Create${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Retrieve${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Update${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |message Delete${cToPascal(name)}Cmd {
       |  option (scalapb.message).extends = "${messageSrcPackage}.${cToPascal(name)}Command";
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
    """.stripMargin.trim,
    s"""
       |message ${cToPascal(name)}ListVo {
       |  option (scalapb.message).extends = "${messageSrcPackage}.ValueObject";
       |  repeated ${cToPascal(name)}Vo items = 1;
       |}
     """.stripMargin.trim
  )

  def generateField(field: Field, no: Int): String = {
    import field._
    val protobufType = if ("array" == _type) s"repeated ${toProtobufType(valueType)}"
    else if ("map" == _type) s"map <${toProtobufType(keyType)}, ${toProtobufType(valueType)}>"
    else toProtobufType(_type)

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
      })
      .map(x => x)
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
  }
}
