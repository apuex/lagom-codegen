package com.github.apuex.commerce.sales.dao.mysql

import com.github.apuex.commerce.sales._
import com.github.apuex.commerce.sales.dao._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection
import play._
import anorm.SqlParser._
import anorm.ParameterValue._
import anorm._

class AlarmDaoImpl extends AlarmDao {
  def createAlarm(cmc: CreateAlarmCmd)(implicit conn: Connection): Int = ???
  def retrieveAlarm(cmd: RetrieveAlarmCmd)(implicit conn: Connection): AlarmVo = ???
  def updateAlarm(cmd: UpdateAlarmCmd)(implicit conn: Connection): Int = ???
  def deleteAlarm(cmd: DeleteAlarmCmd)(implicit conn: Connection): Int = ???
  def queryAlarm(cmd: QueryCommand)(implicit conn: Connection): Seq[AlarmVo] = ???
  def beginAlarm(cmc: BeginAlarmCmd)(implicit conn: Connection): Int = ???
  def endAlarm(cmc: EndAlarmCmd)(implicit conn: Connection): Int = ???
}
