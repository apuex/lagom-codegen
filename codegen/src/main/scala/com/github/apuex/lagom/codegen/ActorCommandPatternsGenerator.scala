package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters.toJavaType

import scala.xml.Node


object ActorCommandPatternsGenerator {
  def apply(fileName: String): ActorCommandPatternsGenerator = ActorCommandPatternsGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): ActorCommandPatternsGenerator = new ActorCommandPatternsGenerator(modelLoader)
}

class ActorCommandPatternsGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def defCallsForEmbeddedAggregateMessage(name: String, aggregate: Aggregate): String = {
    val persistFields = aggregate.fields.filter(!_.transient)
    val nonKeyFieldCount = persistFields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = persistFields
      .filter(x => !keyFieldNames.contains(x.name))
    val update = if (nonKeyFieldCount > 1)
      s"""
         |case cmd: Update${cToPascal(aggregate.name)}Cmd =>
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        s"""
           |case cmd: Add${cToPascal(aggregate.name)}Cmd =>
           |
           |case cmd: Remove${cToPascal(aggregate.name)}Cmd =>
     """.stripMargin.trim
      else
        s"""
           |case cmd: Change${cToPascal(aggregate.name)}Cmd =>
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

  def defMessageCall(message: Message, parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): String = {
    val key = primaryKey.fields.map(_.name).toSet
    val derived = parentFields.map(_.name).filter(!key.contains(_)).toSet

    val hasPersistField = message.fields
      .filter(!_.transient)
      .filter(x => derived.contains(x.name)).isEmpty

    if (message.transient || hasPersistField)
      ""
    else
      s"""
         |case cmd: ${cToPascal(message.name)}Cmd =>
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
     """.stripMargin.trim,
      s"""
         |case cmd: Retrieve${cToPascal(name)}Cmd =>
     """.stripMargin.trim,
      if (nonKeyPersistFields.isEmpty)
        ""
      else
        s"""
           |case cmd: Update${cToPascal(name)}Cmd =>
     """.stripMargin.trim,
      s"""
         |case cmd: Delete${cToPascal(name)}Cmd =>
     """.stripMargin.trim
    )
  }

}


