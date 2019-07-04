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

class AlarmDaoImpl() extends AlarmDao {
  def createAlarm(evt: CreateAlarmEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.alarm
      |  SET
      |    alarm.alarm_end = {alarmEnd},
      |    alarm.alarm_desc = {alarmDesc}
      |  WHERE alarm_id = {alarmId}
      |    AND alarm_begin = {alarmBegin}
     """.stripMargin.trim)
    .on(
      "alarmId" -> evt.alarmId,
      "alarmBegin" -> scalapbToDate(evt.alarmBegin),
      "alarmEnd" -> scalapbToDate(evt.alarmEnd),
      "alarmDesc" -> evt.alarmDesc
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
        |INSERT INTO sales.alarm(
        |    alarm.alarm_id,
        |    alarm.alarm_begin,
        |    alarm.alarm_end,
        |    alarm.alarm_desc
        |  ) VALUES (
        |    {alarmId},
        |    {alarmBegin},
        |    {alarmEnd},
        |    {alarmDesc}
        |  )
       """.stripMargin.trim)
      .on(
        "alarmId" -> evt.alarmId,
        "alarmBegin" -> scalapbToDate(evt.alarmBegin),
        "alarmEnd" -> scalapbToDate(evt.alarmEnd),
        "alarmDesc" -> evt.alarmDesc
      ).executeUpdate()
    else rowsAffected
  }

  def retrieveAlarm(cmd: RetrieveAlarmCmd)(implicit conn: Connection): AlarmVo = {
    SQL(s"""
      |SELECT
      |    alarm.alarm_id,
      |    alarm.alarm_begin,
      |    alarm.alarm_end,
      |    alarm.alarm_desc
      |  FROM sales.alarm
      |  WHERE alarm_id = {alarmId}
      |    AND alarm_begin = {alarmBegin}
     """.stripMargin.trim)
    .on(
      "alarmId" -> cmd.alarmId,
      "alarmBegin" -> scalapbToDate(cmd.alarmBegin)
    ).as(alarmParser.single)
  }

  def updateAlarm(evt: UpdateAlarmEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |UPDATE sales.alarm
      |  SET
      |    alarm.alarm_end = {alarmEnd},
      |    alarm.alarm_desc = {alarmDesc}
      |  WHERE alarm_id = {alarmId}
      |    AND alarm_begin = {alarmBegin}
     """.stripMargin.trim)
    .on(
      "alarmId" -> evt.alarmId,
      "alarmBegin" -> scalapbToDate(evt.alarmBegin),
      "alarmEnd" -> scalapbToDate(evt.alarmEnd),
      "alarmDesc" -> evt.alarmDesc
    ).executeUpdate()
  }

  def deleteAlarm(evt: DeleteAlarmEvent)(implicit conn: Connection): Int = {
    SQL(s"""
      |DELETE
      |  FROM sales.alarm
      |  WHERE alarm_id = {alarmId}
      |    AND alarm_begin = {alarmBegin}
     """.stripMargin.trim)
    .on(
      "alarmId" -> evt.alarmId,
      "alarmBegin" -> scalapbToDate(evt.alarmBegin)
    ).executeUpdate()
  }

  def queryAlarm(cmd: QueryCommand)(implicit conn: Connection): Seq[AlarmVo] = {
    val sqlStr = s"""
      |${selectAlarmSql}
      |  ${whereClause.toWhereClause(cmd, 4)}
     """.stripMargin.trim
    val stmt = SQL(sqlStr)
    val params = namedParams(cmd)
  
    Logger.of(getClass).info(
      s"""
      |[SQL statement] =>
      |  ${indent(sqlStr, 2)}
      |[params for substitution] =>
      |  {}
     """.stripMargin.trim,
      params
    )
  
    if (params.isEmpty) {
      stmt.as(alarmParser.*)
    } else {
      stmt.on(
        params: _*
      ).as(alarmParser.*)
    }
  }

  def retrieveAlarmByRowid(rowid: String)(implicit conn: Connection): AlarmVo = {
    SQL(s"""
      |SELECT
      |    alarm.alarm_id,
      |    alarm.alarm_begin,
      |    alarm.alarm_end,
      |    alarm.alarm_desc
      |  FROM sales.alarm
      |  WHERE rowid = {rowid}
     """.stripMargin.trim)
    .on(
      "rowid" -> rowid
    ).as(alarmParser.single)
  }

  def beginAlarm(evt: BeginAlarmEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.alarm
      |  SET
      |    alarm.alarm_desc = {alarmDesc}
      |  WHERE alarm_id = {alarmId}
      |    AND alarm_begin = {alarmBegin}
     """.stripMargin.trim)
    .on(
      "alarmDesc" -> evt.alarmDesc
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
        |INSERT INTO sales.alarm(
        |    alarm.alarm_desc
        |  ) VALUES (
        |    {alarmDesc}
        |  )
       """.stripMargin.trim)
      .on(
        "alarmDesc" -> evt.alarmDesc
      ).executeUpdate()
    else rowsAffected
  }

  def endAlarm(evt: EndAlarmEvent)(implicit conn: Connection): Int = {
    val rowsAffected = SQL(s"""
      |UPDATE sales.alarm
      |  SET
      |    alarm.alarm_end = {alarmEnd},
      |    alarm.alarm_desc = {alarmDesc}
      |  WHERE alarm_id = {alarmId}
      |    AND alarm_begin = {alarmBegin}
     """.stripMargin.trim)
    .on(
      "alarmEnd" -> scalapbToDate(evt.alarmEnd),
      "alarmDesc" -> evt.alarmDesc
    ).executeUpdate()
  
    if(rowsAffected == 0)
      SQL(s"""
        |INSERT INTO sales.alarm(
        |    alarm.alarm_end,
        |    alarm.alarm_desc
        |  ) VALUES (
        |    {alarmEnd},
        |    {alarmDesc}
        |  )
       """.stripMargin.trim)
      .on(
        "alarmEnd" -> scalapbToDate(evt.alarmEnd),
        "alarmDesc" -> evt.alarmDesc
      ).executeUpdate()
    else rowsAffected
  }

  private val selectAlarmSql =
    s"""
      |SELECT
      |    t.alarm_id,
      |    t.alarm_begin,
      |    t.alarm_end,
      |    t.alarm_desc
      |  FROM sales.alarm t
     """.stripMargin.trim

  private val fieldConverter: SymbolConverter = {
    case "alarmId" => "alarm_id"
    case "alarmBegin" => "alarm_begin"
    case "alarmEnd" => "alarm_end"
    case "alarmDesc" => "alarm_desc"
    case x: String => camelToC(x)
  }

  private val whereClause = WhereClauseWithNamedParams(fieldConverter)

  private def parseParam(fieldName: String, paramName:String, paramValue: scala.Any): NamedParameter = paramValue match {
    case x: String => parseParam(fieldName, paramName, x)
    case x: Array[String] => parseParam(fieldName, paramName, x.toSeq)
    case x: scala.Any => throw new RuntimeException(x.toString)
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
    case "alarmId" => paramName -> paramValue
    case "alarmBegin" => paramName -> DateParser.parse(paramValue)
    case "alarmEnd" => paramName -> DateParser.parse(paramValue)
    case "alarmDesc" => paramName -> paramValue
  }

  private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
    case "alarmId" => paramName -> paramValue
    case "alarmBegin" => paramName -> paramValue.map(DateParser.parse(_))
    case "alarmEnd" => paramName -> paramValue.map(DateParser.parse(_))
    case "alarmDesc" => paramName -> paramValue
  }

  private def alarmParser(implicit c: Connection): RowParser[AlarmVo] = {
    get[String]("alarm_id") ~ 
    get[Date]("alarm_begin") ~ 
    get[Option[Date]]("alarm_end") ~ 
    get[String]("alarm_desc") map {
      case alarmId ~ alarmBegin ~ alarmEnd ~ alarmDesc =>
        AlarmVo(
          alarmId,
          Some(toScalapbTimestamp(alarmBegin)),
          alarmEnd.map(toScalapbTimestamp(_)),
          alarmDesc
        )
    }
  }

  private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
    whereClause.toNamedParams(q.getPredicate, q.params)
      .map(x => parseParam(x._1, x._2, x._3))
      .asInstanceOf[Seq[NamedParameter]]
  }
}
