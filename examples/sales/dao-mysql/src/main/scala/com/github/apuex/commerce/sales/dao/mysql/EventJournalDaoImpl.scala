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
import com.github.apuex.springbootsolution.runtime.TextUtils._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.ByteString
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._

class EventJournalDaoImpl() extends EventJournalDao {
  val log = Logger.of(getClass)

  private def offsetParser(implicit c: Connection): RowParser[Long] = {
    get[Long]("offset") map {
      case offset => offset
    }
  }
  
  def selectCurrentOffset()(implicit conn: Connection): Long = {
    try {
      val max = SQL("SELECT max(event_journal.offset) as offset FROM sales.event_journal").as(offsetParser.*)
      if (max.isEmpty) 0 else max.head
    } catch {
      case _ => 0
    }
  }

  def createEventJournal(evt: CreateEventJournalEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.event_journal
      |  SET
      |    event_journal.persistence_id = {persistenceId},
      |    event_journal.occurred_time = {occurredTime},
      |    event_journal.meta_data = {metaData},
      |    event_journal.content = {content}
      |  WHERE offset = {offset}
     """.stripMargin.trim)
    .on(
      "offset" -> evt.offset,
      "persistenceId" -> evt.persistenceId,
      "occurredTime" -> scalapbToDate(evt.occurredTime),
      "metaData" -> evt.metaData,
      "content" -> evt.content.toByteArray
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
        "offset" -> evt.offset,
        "persistenceId" -> evt.persistenceId,
        "occurredTime" -> scalapbToDate(evt.occurredTime),
        "metaData" -> evt.metaData,
        "content" -> evt.content.toByteArray
      ).executeUpdate()
    else rowsAffected
  }

  def retrieveEventJournal(cmd: RetrieveEventJournalCmd)(implicit conn: Connection): EventJournalVo = {
    SQL(s"""
      |SELECT
      |    event_journal.offset,
      |    event_journal.persistence_id,
      |    event_journal.occurred_time,
      |    event_journal.meta_data,
      |    event_journal.content
      |  FROM sales.event_journal
      |  WHERE offset = {offset}
     """.stripMargin.trim)
    .on(
      "offset" -> cmd.offset
    ).as(eventJournalParser.single)
  }

  def updateEventJournal(evt: UpdateEventJournalEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.event_journal
      |  SET
      |    event_journal.persistence_id = {persistenceId},
      |    event_journal.occurred_time = {occurredTime},
      |    event_journal.meta_data = {metaData},
      |    event_journal.content = {content}
      |  WHERE offset = {offset}
     """.stripMargin.trim)
    .on(
      "offset" -> evt.offset,
      "persistenceId" -> evt.persistenceId,
      "occurredTime" -> scalapbToDate(evt.occurredTime),
      "metaData" -> evt.metaData,
      "content" -> evt.content.toByteArray
    ).executeUpdate()
  }

  def deleteEventJournal(evt: DeleteEventJournalEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |DELETE
      |  FROM sales.event_journal
      |  WHERE offset = {offset}
     """.stripMargin.trim)
    .on(
      "offset" -> evt.offset
    ).executeUpdate()
  }

  def queryEventJournal(cmd: QueryCommand)(implicit conn: Connection): Seq[EventJournalVo] = {
    val sqlStr = s"""
      |${selectEventJournalSql}
      |  ${whereClause.toWhereClause(cmd, 4)}
      |  ORDER BY ${indent(if(!cmd.orderBy.isEmpty) whereClause.orderBy(cmd.orderBy, "t") else "", 4)}
     """.stripMargin.trim
    val stmt = SQL(sqlStr)
    val params = namedParams(cmd)
  
    if(log.isDebugEnabled) log.debug(
      s"""
      |[SQL statement] =>
      |  ${indent(sqlStr, 2)}
      |  [params for substitution] =>
      |    {}
     """.stripMargin.trim,
      params
    )
  
    if (params.isEmpty) {
      stmt.as(eventJournalParser.*)
    } else {
      stmt.on(
        params: _*
      ).as(eventJournalParser.*)
    }
  }

  def retrieveEventJournalByRowid(rowid: String)(implicit conn: Connection): EventJournalVo = {
    SQL(s"""
      |SELECT
      |    event_journal.offset,
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
      |    t.offset,
      |    t.persistence_id,
      |    t.occurred_time,
      |    t.meta_data,
      |    t.content
      |  FROM sales.event_journal t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "offset" => "offset"
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
    case "offset" => paramName -> LongParser.parse(paramValue)
    case "persistenceId" => paramName -> paramValue
    case "occurredTime" => paramName -> DateParser.parse(paramValue)
    case "metaData" => paramName -> paramValue
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "offset" => paramName -> paramValue.map(LongParser.parse(_))
    case "persistenceId" => paramName -> paramValue
    case "occurredTime" => paramName -> paramValue.map(DateParser.parse(_))
    case "metaData" => paramName -> paramValue
  }

  private def eventJournalParser(implicit c: Connection): RowParser[EventJournalVo] = {
    get[Long]("offset") ~ 
    get[String]("persistence_id") ~ 
    get[Date]("occurred_time") ~ 
    get[String]("meta_data") ~ 
    get[InputStream]("content") map {
      case offset ~ persistenceId ~ occurredTime ~ metaData ~ content =>
        EventJournalVo(
          offset,
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
