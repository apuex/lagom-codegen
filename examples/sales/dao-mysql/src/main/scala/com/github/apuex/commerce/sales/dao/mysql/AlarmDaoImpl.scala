package com.github.apuex.commerce.sales.dao.mysql


import java.sql.Connection
import java.util.Date

import anorm.SqlParser._
import anorm._
import play._
import anorm.ParameterValue._
import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime.DateFormat.toScalapbTimestamp
import com.github.apuex.springbootsolution.runtime.Parser._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime._

class AlarmDaoImpl() extends AlarmDao {
  def createAlarm(cmc: CreateAlarmCmd)(implicit conn: Connection): Int = ???
  def retrieveAlarm(cmd: RetrieveAlarmCmd)(implicit conn: Connection): AlarmVo = ???
  def updateAlarm(cmd: UpdateAlarmCmd)(implicit conn: Connection): Int = ???
  def deleteAlarm(cmd: DeleteAlarmCmd)(implicit conn: Connection): Int = ???
  def queryAlarm(cmd: QueryCommand)(implicit conn: Connection): Seq[AlarmVo] = ???
  def retrieveAlarmByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): Seq[AlarmVo] = ???
  def beginAlarm(cmc: BeginAlarmCmd)(implicit conn: Connection): Int = ???
  def endAlarm(cmc: EndAlarmCmd)(implicit conn: Connection): Int = ???


  private val sql =
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

  private def rowParser(implicit c: Connection): RowParser[AlarmVo] = {
    get[String]("alarm_id") ~ 
    get[Date]("alarm_begin") ~ 
    get[Date]("alarm_end") ~ 
    get[String]("alarm_desc") map {
      case alarmId ~ alarmBegin ~ alarmEnd ~ alarmDesc =>
        AlarmVo(
          alarmId,
          Some(toScalapbTimestamp(alarmBegin)),
          Some(toScalapbTimestamp(alarmEnd)),
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
