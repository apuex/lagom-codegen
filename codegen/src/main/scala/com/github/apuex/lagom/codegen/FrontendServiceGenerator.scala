package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters.toJavaType

import scala.xml.Node

object FrontendServiceGenerator {
  def apply(fileName: String): FrontendServiceGenerator = FrontendServiceGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): FrontendServiceGenerator = new FrontendServiceGenerator(modelLoader)
}

class FrontendServiceGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {

    generateServiceImpl()
      .foreach(x => save(
        s"${cToShell(x._1)}.ts",
        s"${x._2}",
        frontendSrcDir
      ))
  }

  def generateServiceImpl(): Seq[(String, String)] = {
    xml.filter(_.label == "entity")
      .map(generateServiceImpl(_))
  }

  def generateServiceImpl(entity: Node): (String, String) = {
    val constructorParams =
        Seq(
          s"private http: HttpClient"
        )
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    (entity.\@("name"), s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |import { Injectable } from '@angular/core';
       |import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
       |import { Observable, of } from 'rxjs';
       |import { tap, delay } from 'rxjs/operators';
       |
       |import {
       |  PredicateType,
       |  LogicalConnectionType,
       |  QueryCommand,
       |  OrderBy,
       |  OrderType,
       |  SearchCommand,
       |  LogicalPredicateVo,
       |  LogicalConnectionVo,
       |  createPredicate,
       |  createConnection
       |} from '../query-command/query-command';
       |import { ${cToPascal(modelName)}Vo, ${cToPascal(modelName)}ListVo } from './ai-history';
       |
       |import { Config } from '../config/config';
       |
       |@Injectable({
       |  providedIn: 'root'
       |})
       |
       |export class ${cToPascal(modelName)}Service {
       |  constructor(${indent(constructorParams, 2)}) { }
       |
       |  ${indent(calls(entity), 2)}
       |
       |}
     """.stripMargin.trim)
  }


  def calls(entity: Node, root: Node = xml): String = {
      if(entity.\@("name") == journalTable) {
        ""
      }
      else {
          val aggregatesTo = entity.\@("aggregatesTo")
          val enum = if ("true" == entity.\@("enum")) true else false
          if (!enum && "" == aggregatesTo) generateCallsForAggregate(toAggregate(entity, root))
          else {
            val valueObject = toValueObject(entity, aggregatesTo, root)
            generateCallsForValueObject(valueObject)
          }
      }
  }

  def defCallsForEmbeddedAggregateMessage(name: String, aggregate: Aggregate): String = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))

    val get =
      s"""
         |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd): Observable<${cToPascal(aggregate.name)}Vo> {
         |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/get-${cToShell(aggregate.name)}', cmd);
         |}
     """.stripMargin.trim
    val update = if (nonKeyFieldCount > 1) {
        s"""
           |def update${cToPascal(aggregate.name)}(cmd: Update${cToPascal(aggregate.name)}Cmd): Observable<number> {
           |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/update-${cToShell(aggregate.name)}', cmd);
           |}
     """.stripMargin.trim
    } else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type) {
          s"""
             |def add${cToPascal(aggregate.name)}(cmd: Add${cToPascal(aggregate.name)}Cmd): Observable<number> {
             |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/add-${cToShell(aggregate.name)}', cmd);
             |}
             |
             |def remove${cToPascal(aggregate.name)}(cmd: Remove${cToPascal(aggregate.name)}Cmd): Observable<number> {
             |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/remove-${cToShell(aggregate.name)}', cmd);
             |}
     """.stripMargin.trim
      } else {
          s"""
             |def change${cToPascal(aggregate.name)}(cmd: Change${cToPascal(aggregate.name)}Cmd): Observable<number> {
             |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/change-${cToShell(aggregate.name)}', cmd);
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
      defCrudCalls(transient, name, fields, primaryKey) ++
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
      defCrudCalls(transient, name, fields, primaryKey) ++
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
        if (isAggregateEntity(baseName)) s"${cToPascal(baseName)}ListVo" else s"${cToPascal(baseName)}Vo"
      } else {
        cToPascal(toJavaType(baseName))
      }
    }

      s"""
         |def ${cToCamel(message.name)}(cmd: ${cToPascal(message.name)}Cmd): Observable<${returnType}> {
         |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(parentName)}/change-${cToShell(message.name)}', cmd);
         |}
     """.stripMargin.trim
  }

  def defMessageCalls(messages: Seq[Message], parentName: String, parentFields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
    messages.map(defMessageCall(_, parentName, parentFields, primaryKey))
  }

  def defCrudCalls(transient: Boolean, name: String, fields: Seq[Field], primaryKey: PrimaryKey): Seq[String] = {
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
           |def create${cToPascal(name)}(cmd: Create${cToPascal(name)}Cmd): Observable<number> {
           |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/create-${cToShell(name)}', cmd);
           |}
     """.stripMargin.trim,
        s"""
           |def retrieve${cToPascal(name)}(cmd: Retrieve${cToPascal(name)}Cmd): Observable<${cToPascal(name)}Vo> {
           |  return this.http.post<${cToPascal(name)}Vo>(Config.API_BASE_HREF + '/${cToShell(name)}/retrieve-${cToShell(name)}', cmd);
           |}
     """.stripMargin.trim,
        if (nonKeyPersistFields.isEmpty)
          s"""
             |
           """.stripMargin.trim
        else
          s"""
             |def update${cToPascal(name)}(cmd: Update${cToPascal(name)}Cmd): Observable<number> {
             |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/update-${cToShell(name)}', cmd);
             |}
     """.stripMargin.trim,
        s"""
           |def delete${cToPascal(name)}(cmd: Delete${cToPascal(name)}Cmd): Observable<number> {
           |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/delete-${cToShell(name)}', cmd);
           |}
     """.stripMargin.trim,
        s"""
           |def query${cToPascal(name)}(q: QueryCommand): Observable<${cToPascal(name)}ListVo> {
           |  return this.http.post<${cToPascal(name)}Vo>(Config.API_BASE_HREF + '/${cToShell(name)}/query-${cToShell(name)}', q);
           |}
     """.stripMargin.trim,
        s"""
           |def retrieve${cToPascal(name)}ByRowid(rowid: string): Observable<${cToPascal(name)}Vo> {
           |  return this.http.get<${cToPascal(name)}Vo>(Config.API_BASE_HREF + '/${cToShell(name)}/retrieve-${cToShell(name)}/' + rowid);
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
       |select${cToPascal(name)}By${by}(${defMethodParams(keyFields)}): Observable<${cToPascal(name)}ListVo> = {
       |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/select-${cToShell(name)}-by-${pascalToShell(by)}?' + ${defUrlParams(keyFields)});
       |}
       |
       |delete${cToPascal(name)}By${by}(${defMethodParams(keyFields)}): Observable<number> {
       |  return this.http.post<number>(Config.API_BASE_HREF + '/${cToShell(name)}/delete-${cToShell(name)}-by-${pascalToShell(by)}?' + ${defUrlParams(keyFields)});
       |}
     """.stripMargin.trim
  }

}


