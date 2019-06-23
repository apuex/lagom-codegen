package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait AlarmDao {
  def createAlarm(cmc: CreateAlarmCmd)(implicit conn: Connection): Int
  def retrieveAlarm(cmd: RetrieveAlarmCmd)(implicit conn: Connection): AlarmVo
  def updateAlarm(cmd: UpdateAlarmCmd)(implicit conn: Connection): Int
  def deleteAlarm(cmd: DeleteAlarmCmd)(implicit conn: Connection): Int
  def queryAlarm(cmd: QueryCommand)(implicit conn: Connection): Seq[AlarmVo]
}
