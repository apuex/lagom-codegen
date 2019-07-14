package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters.toJavaType


object ActorCommandPatternsGenerator {
  def apply(fileName: String): ActorCommandPatternsGenerator = ActorCommandPatternsGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ActorCommandPatternsGenerator = new ActorCommandPatternsGenerator(modelLoader)
}

class ActorCommandPatternsGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def defCallsForEmbeddedAggregateMessage(name: String, aggregate: Aggregate): String = {
    val persistFields = aggregate.fields.filter(!_.transient)
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields
      .filter(x => !keyFieldNames.contains(x.name))
    val nonKeyPersistFields = persistFields
      .filter(x => !keyFieldNames.contains(x.name))
    val nonKeyTransientFields = aggregate.fields.filter(_.transient)
      .filter(x => !keyFieldNames.contains(x.name))
    val get =
      s"""
         |case _: Get${cToPascal(aggregate.name)}Cmd =>
         |  sender() ! ${cToPascal(aggregate.name)}Vo(${substituteMethodParams(aggregate.fields)})
       """.stripMargin.trim

    val updateOp = if (nonKeyPersistFields.isEmpty) {
      s"""
         |${updateFields(nonKeyTransientFields, "cmd")}
     """.stripMargin.trim
    } else {
      s"""
         |${updateFields(nonKeyTransientFields, "cmd")}
         |val evt = Update${cToPascal(aggregate.name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
         |persist(evt)(updateState)
     """.stripMargin.trim
    }

    val update = if (nonKeyFieldCount > 1)
      s"""
         |case cmd: Update${cToPascal(aggregate.name)}Cmd =>
         |  ${indent(updateOp, 2)}
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      val addOp = if (nonKeyPersistFields.isEmpty) {
        s"""
           |${indent(addToField(field, "cmd."), 2)}
     """.stripMargin.trim
      } else {
        s"""
           |${indent(addToField(field, "cmd."), 2)}
           |val evt = Add${cToPascal(aggregate.name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
           |persist(evt)(updateState)
     """.stripMargin.trim
      }
      val removeOp = if (nonKeyPersistFields.isEmpty) {
        s"""
           |${indent(removeFromField(field, "cmd."), 2)}
     """.stripMargin.trim
      } else {
        s"""
           |${indent(removeFromField(field, "cmd."), 2)}
           |val evt = Remove${cToPascal(aggregate.name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
           |persist(evt)(updateState)
     """.stripMargin.trim
      }
      val changeOp = if (nonKeyPersistFields.isEmpty) {
        s"""
           |${indent(updateFields(nonKeyTransientFields, "cmd"), 2)}
     """.stripMargin.trim
      } else {
        s"""
           |${indent(updateFields(nonKeyTransientFields, "cmd"), 2)}
           |val evt = Change${cToPascal(aggregate.name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
           |persist(evt)(updateState)
     """.stripMargin.trim
      }

      if ("array" == field._type || "map" == field._type)
        s"""
           |case cmd: Add${cToPascal(aggregate.name)}Cmd =>
           |  ${indent(addOp, 2)}
           |
           |case cmd: Remove${cToPascal(aggregate.name)}Cmd =>
           |  ${indent(removeOp, 2)}
           |
     """.stripMargin.trim
      else
        s"""
           |case cmd: Change${cToPascal(aggregate.name)}Cmd =>
           |  ${indent(changeOp, 2)}
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
      defCrudCalls(name, fields, primaryKey) ++
        defMessageCalls(aggregate.messages, name, fields, primaryKey) ++
        defCallsForEmbeddedAggregateMessages(aggregate.name, aggregate.aggregates)
      )
      .filter(_ != "")
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def defMessageCall(message: Message, parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): String = {
    val key = primaryKey.fields.map(_.name)
      .toSet
    val fieldNames = message.fields
      .map(_.name)
      .toSet
    val derivedFields = parentFields
      .filter(x => fieldNames.contains(x.name))
      .filter(x => !key.contains(x.name))
    val derived = derivedFields.map(_.name)
      .toSet

    val persistFields = message.fields
      .filter(!_.transient)
    val transientFields = message.fields
      .filter(_.transient)
      .filter(x => !key.contains(x.name))
      .filter(x => derived.contains(x.name))

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

    // TODO: implement return message
    val replyVal = if(message.transient && "Int" != returnType) {
      s"""
         |
       """.stripMargin.trim
    } else {
      s"""
         |
       """.stripMargin.trim
    }

    val messageOp = if (message.transient) {
      s"""
         |${updateFields(derivedFields, "cmd")}
         |${replyVal}
     """.stripMargin.trim
    } else {
        s"""
           |${indent(updateFields(transientFields, "cmd"), 2)}
           |val evt = ${cToPascal(message.name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
           |persist(evt)(updateState)
     """.stripMargin.trim
    }
    s"""
       |case cmd: ${cToPascal(message.name)}Cmd =>
       |  ${indent(messageOp, 2)}
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
         |case cmd: Create${cToPascal(name)}Cmd =>
         |  val evt = Create${cToPascal(name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
         |  persist(evt)(updateState)
     """.stripMargin.trim,
      s"""
         |case _: Retrieve${cToPascal(name)}Cmd =>
         |  sender() ! ${cToPascal(name)}Vo(${substituteMethodParams(fields)})
     """.stripMargin.trim,
      if (nonKeyPersistFields.isEmpty)
        ""
      else
        s"""
           |case cmd: Update${cToPascal(name)}Cmd =>
           |  val evt = Update${cToPascal(name)}Event(${substituteMethodParams(userField +: persistFields, "cmd")})
           |  persist(evt)(updateState)
     """.stripMargin.trim,
      s"""
         |case cmd: Delete${cToPascal(name)}Cmd =>
         |  val evt = Delete${cToPascal(name)}Event(${substituteMethodParams(userField +: primaryKey.fields, "cmd")})
         |  persist(evt)(updateState)
     """.stripMargin.trim
    )
  }

}


