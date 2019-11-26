package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._


import scala.xml.Node


object FrontendMessageGenerator {
  def apply(fileName: String): FrontendMessageGenerator = new FrontendMessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): FrontendMessageGenerator = new FrontendMessageGenerator(modelLoader)
}

class FrontendMessageGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    generateMessageContent(xml, messageSrcPackage)
      .foreach(x => save(
        s"${cToShell(x._1)}.ts",
        s"${x._2}",
        frontendSrcDir
      ))
  }

  def generateMessageContent(xml: Node, messageSrcPackage: String): Seq[(String, String)] = {
    xml.child.filter(_.label == "entity")
      .map(x => {
        val entityName = x.\@("name")
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        val results: Seq[String] = if (!enum && "" == aggregatesTo) generateMessagesForAggregate(toAggregate(x, xml), messageSrcPackage)
        else {
          val valueObject = toValueObject(x, aggregatesTo, xml)
          val valueObjects = generateValueObject(valueObject.name, valueObject.fields, messageSrcPackage)
          if (enum) {
            val enumration = toEnumeration(x, aggregatesTo, xml)
            valueObjects ++
              generateCrudCmd(valueObject.name, valueObject.name, valueObject.fields, valueObject.primaryKey.fields, messageSrcPackage) ++ (
              if (valueObject.transient)
                Seq()
              else
                generateCrudEvt(valueObject.name, valueObject.name, valueObject.fields.filter(!_.transient), valueObject.primaryKey.fields, messageSrcPackage)
              ) ++
              generateEnumeration(enumration.name, enumration.options, messageSrcPackage)
          } else {
            valueObjects ++
              generateCrudCmd(aggregatesTo, valueObject.name, valueObject.fields, valueObject.primaryKey.fields, messageSrcPackage) ++ (
              if (valueObject.transient)
                Seq()
              else
                generateCrudEvt(aggregatesTo, valueObject.name, valueObject.fields.filter(!_.transient), valueObject.primaryKey.fields, messageSrcPackage)
              )
          }
        }
        (entityName, results)
      })
      .filter(x => !x._2.isEmpty)
      .map(x => (x._1, x._2.reduceOption((l, r) => s"${l}\n\n${r}").getOrElse("")))
  }

  def generateMessagesForEmbeddedAggregate(entity: Aggregate, name: String, messageSrcPackage: String): Seq[String] = {
    val nonKeyFieldCount = entity.fields.length - entity.primaryKey.fields.length
    val keyFieldNames = entity.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = entity.fields.filter(x => !keyFieldNames.contains(x.name))
    val nonKeyPersistFields = entity.fields
      .filter(!_.transient)
      .filter(x => !keyFieldNames.contains(x.name))
    val messages = if (nonKeyFieldCount > 1) Seq(
      s"""
         |export class Get${cToPascal(entity.name)}Cmd {
         |  ${indent(generateFields(userField +: entity.primaryKey.fields), 2)}
         |}
       """.stripMargin.trim,
      s"""
         |export class Update${cToPascal(entity.name)}Cmd {
         |  ${indent(generateFields(userField +: entity.fields), 2)}
         |}
       """.stripMargin.trim,
      if (nonKeyPersistFields.isEmpty)
        ""
      else
        s"""
           |export class Update${cToPascal(entity.name)}Event{
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim
    )
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type) Seq(
        s"""
           |export class Get${cToPascal(entity.name)}Cmd {
           |  ${indent(generateFields(userField +: entity.primaryKey.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |export class Add${cToPascal(entity.name)}Cmd {
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        if (nonKeyPersistFields.isEmpty)
          ""
        else
          s"""
             |export class Add${cToPascal(entity.name)}Event{
             |  ${indent(generateFields(userField +: entity.fields), 2)}
             |}
       """.stripMargin.trim,
        s"""
           |export class Remove${cToPascal(entity.name)}Cmd {
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        if (nonKeyPersistFields.isEmpty)
          ""
        else
          s"""
             |export class Remove${cToPascal(entity.name)}Event{
             |  ${indent(generateFields(userField +: entity.fields), 2)}
             |}
       """.stripMargin.trim
      )
      else Seq(
        s"""
           |export class Get${cToPascal(entity.name)}Cmd {
           |  ${indent(generateFields(userField +: entity.primaryKey.fields), 2)}
           |}
       """.stripMargin.trim,
        s"""
           |export class Change${cToPascal(entity.name)}Cmd {
           |  ${indent(generateFields(userField +: entity.fields), 2)}
           |}
       """.stripMargin.trim,
        if (nonKeyPersistFields.isEmpty)
          ""
        else
          s"""
             |export class Change${cToPascal(entity.name)}Event{
             |  ${indent(generateFields(userField +: entity.fields), 2)}
             |}
       """.stripMargin.trim
      )
    } else { // this cannot be happen.
      Seq()
    }
    generateValueObject(entity.name, entity.fields, messageSrcPackage) ++
      messages.filter("" != _)
  }

  def generateMessagesForAggregate(entity: Aggregate, messageSrcPackage: String): Seq[String] = {
    entity.aggregates.map(generateMessagesForEmbeddedAggregate(_, entity.name, messageSrcPackage)).flatMap(x => x) ++
      generateValueObject(entity.name, entity.fields, messageSrcPackage) ++
      generateCrudCmd(entity.name, entity.name, entity.fields, entity.primaryKey.fields, messageSrcPackage) ++
      (if (entity.transient) Seq() else generateCrudEvt(entity.name, entity.name, entity.fields.filter(!_.transient), entity.primaryKey.fields, messageSrcPackage)) ++
      generateMessages(entity.messages, entity.name, messageSrcPackage) ++
      (if (entity.transient) Seq() else generateEvents(entity.messages, entity.name, messageSrcPackage))
  }

  def generateMessage(message: Message, name: String, messageSrcPackage: String): String = {
    s"""
       |export class ${cToPascal(message.name)}Cmd {
       |  ${indent(generateFields(userField +: message.fields), 2)}
       |}
     """.stripMargin.trim
  }

  def generateEvent(message: Message, name: String, messageSrcPackage: String): String = {
    s"""
       |export class ${cToPascal(message.name)}Event {
       |  ${indent(generateFields(userField +: message.fields.filter(!_.transient)), 2)}
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

  def generateCrudEvt(aggregate: String, name: String, fields: Seq[Field], pkFields: Seq[Field], messageSrcPackage: String): Seq[String] = {
    val keyFieldNames = pkFields.map(_.name).toSet
    val persistFields = fields.filter(!_.transient)
    val nonKeyPersistFields = persistFields.filter(x => !keyFieldNames.contains(x.name))

    Seq(
      s"""
         |export class Create${cToPascal(name)}Event {
         |  ${indent(generateFields(userField +: fields), 2)}
         |}
     """.stripMargin.trim,
      if (nonKeyPersistFields.isEmpty)
        ""
      else
        s"""
           |export class Update${cToPascal(name)}Event {
           |  ${indent(generateFields(userField +: fields), 2)}
           |}
     """.stripMargin.trim,
      s"""
         |export class Delete${cToPascal(name)}Event {
         |  ${indent(generateFields(userField +: pkFields), 2)}
         |}
     """.stripMargin.trim
    )
  }

  def generateCrudCmd(aggregate: String, name: String, fields: Seq[Field], pkFields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |export class Create${cToPascal(name)}Cmd {
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |export class Retrieve${cToPascal(name)}Cmd {
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |export class Update${cToPascal(name)}Cmd {
       |  ${indent(generateFields(userField +: fields), 2)}
       |}
     """.stripMargin.trim,
    s"""
       |export class Delete${cToPascal(name)}Cmd {
       |  ${indent(generateFields(userField +: pkFields), 2)}
       |}
     """.stripMargin.trim
  )

  def generateValueObject(name: String, fields: Seq[Field], messageSrcPackage: String): Seq[String] = Seq(
    s"""
       |export class ${cToPascal(name)}Vo {
       |  ${indent(generateFields(fields), 2)}
       |}
       |
       |export class ${cToPascal(name)}ListVo {
       |  items: ${cToPascal(name)}Vo[];
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
       |  ${indent(generateEnumOptions(options), 2)}
       |}
    """.stripMargin.trim
  )

  def generateField(field: Field, no: Int): String = {
    import field._

    val typeScriptType = if ("array" == _type) s"${toTypeScriptType(valueType)}${if (isAggregateEntity(valueType)) "Vo" else ""}"
    else if ("map" == _type) s"map <${toTypeScriptType(keyType)}${if (isAggregateEntity(keyType)) "Vo" else ""}, ${toTypeScriptType(valueType)}${if (isAggregateEntity(valueType)) "Vo" else ""}>"
    else s"${toTypeScriptType(_type)}${if (isAggregateEntity(_type)) "Vo" else ""}"

    s"""
       |${cToCamel(name)}: ${typeScriptType }; // ${comment}
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

}
