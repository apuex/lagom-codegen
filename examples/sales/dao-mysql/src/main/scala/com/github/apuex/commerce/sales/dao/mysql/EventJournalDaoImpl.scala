/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao.mysql

import java.io.InputStream
import java.sql.Connection
import java.util.Date

import anorm.ParameterValue._
import anorm.SqlParser._
import anorm._
import play._
import com.github.apuex.springbootsolution.runtime.DateFormat.{scalapbToDate, toScalapbTimestamp}
import com.github.apuex.springbootsolution.runtime.EnumConvert._
import com.github.apuex.springbootsolution.runtime.Parser._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.ByteString
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._

class EventJournalDaoImpl() extends EventJournalDao {
  def createEventJournal(cmd: CreateEventJournalCmd)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.event_journal
       |  SET
       |    event_journal.meta_data = {metaData},
       |    event_journal.content = {content}
       |  WHERE persistence_id = {persistenceId}
       |    AND occurred_time = {occurredTime}
     """.stripMargin.trim)
    .on(
      "persistenceId" -> cmd.persistenceId,
      "occurredTime" -> scalapbToDate(cmd.occurredTime),
      "metaData" -> cmd.metaData,
      "content" -> cmd.content.toByteArray
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
        |INSERT INTO sales.event_journal(
         |    event_journal.persistence_id,
         |    event_journal.occurred_time,
         |    event_journal.meta_data,
         |    event_journal.content
         |  ) VALUES (
         |    {persistenceId},
         |    {occurredTime},
         |    {metaData},
         |    {content}
         |  )
       """.stripMargin.trim)
      .on(
        "persistenceId" -> cmd.persistenceId,
        "occurredTime" -> scalapbToDate(cmd.occurredTime),
        "metaData" -> cmd.metaData,
        "content" -> cmd.content.toByteArray
      ).executeUpdate()
    else rowsAffected
  }

  def retrieveEventJournal(cmd: RetrieveEventJournalCmd)(implicit conn: Connection): EventJournalVo = {
    SQL(s"""
      |SELECT
       |    event_journal.persistence_id,
       |    event_journal.occurred_time,
       |    event_journal.meta_data,
       |    event_journal.content
       |  FROM sales.event_journal
       |  WHERE persistence_id = {persistenceId}
       |    AND occurred_time = {occurredTime}
     """.stripMargin.trim)
    .on(
      "persistenceId" -> cmd.persistenceId,
      "occurredTime" -> scalapbToDate(cmd.occurredTime)
    ).as(eventJournalParser.single)
  }

  def updateEventJournal(cmd: UpdateEventJournalCmd)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.event_journal
       |  SET
       |    event_journal.meta_data = {metaData},
       |    event_journal.content = {content}
       |  WHERE persistence_id = {persistenceId}
       |    AND occurred_time = {occurredTime}
     """.stripMargin.trim)
    .on(
      "persistenceId" -> cmd.persistenceId,
      "occurredTime" -> scalapbToDate(cmd.occurredTime),
      "metaData" -> cmd.metaData,
      "content" -> cmd.content.toByteArray
    ).executeUpdate()
  }

  def deleteEventJournal(cmd: DeleteEventJournalCmd)(implicit conn: Connection): Int = {
    SQL(s"""
      |DELETE
       |  FROM sales.event_journal
       |  WHERE persistence_id = {persistenceId}
       |    AND occurred_time = {occurredTime}
     """.stripMargin.trim)
    .on(
      "persistenceId" -> cmd.persistenceId,
      "occurredTime" -> scalapbToDate(cmd.occurredTime)
    ).executeUpdate()
  }

  def queryEventJournal(cmd: QueryCommand)(implicit conn: Connection): Seq[EventJournalVo] = {
    Seq()
  }

  def retrieveEventJournalByRowid(rowid: String)(implicit conn: Connection): EventJournalVo = {
    SQL(s"""
      |SELECT
       |    event_journal.persistence_id,
       |    event_journal.occurred_time,
       |    event_journal.meta_data,
       |    event_journal.content
       |  FROM sales.event_journal
       |  WHERE rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> rowid
    ).as(eventJournalParser.single)
  }

  private val selectEventJournalSql =
    s"""
      |SELECT
       |    t.persistence_id,
       |    t.occurred_time,
       |    t.meta_data,
       |    t.content
       |  FROM sales.event_journal t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "persistenceId" => "persistence_id"
    case "occurredTime" => "occurred_time"
    case "metaData" => "meta_data"
    case "content" => "content"
    case x: String => camelToC(x)
  }

  private val whereClause = WhereClauseWithNamedParams(fieldConverter)

  private def parseParam(fieldName: String, paramName:String, paramValue: scala.Any): NamedParameter = paramValue match {
    case x: String => parseParam(fieldName, paramName, x)
    case x: Array[String] => parseParam(fieldName, paramName, x.toSeq)
    case x: scala.Any => throw new RuntimeException(x.toString)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
    case "persistenceId" => paramName -> paramValue
    case "occurredTime" => paramName -> DateParser.parse(paramValue)
    case "metaData" => paramName -> paramValue
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "persistenceId" => paramName -> paramValue
    case "occurredTime" => paramName -> paramValue.map(DateParser.parse(_))
    case "metaData" => paramName -> paramValue
  }

  private def eventJournalParser(implicit c: Connection): RowParser[EventJournalVo] = {
    get[String]("persistence_id") ~ 
    get[Date]("occurred_time") ~ 
    get[String]("meta_data") ~ 
    get[InputStream]("content") map {
      case persistenceId ~ occurredTime ~ metaData ~ content =>
        EventJournalVo(
          persistenceId,
          Some(toScalapbTimestamp(occurredTime)),
          metaData,
          ByteString.readFrom(content)
        )
    }
  }

  private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
    whereClause.toNamedParams(q.getPredicate, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]]
  }
}
