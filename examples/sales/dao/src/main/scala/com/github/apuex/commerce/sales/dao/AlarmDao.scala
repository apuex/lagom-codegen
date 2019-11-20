/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection
import java.util.UUID

trait AlarmDao {
  def createAlarm(evt: CreateAlarmEvent)(implicit conn: Connection): Int

  def retrieveAlarm(cmd: RetrieveAlarmCmd)(implicit conn: Connection): AlarmVo

  def updateAlarm(evt: UpdateAlarmEvent)(implicit conn: Connection): Int

  def deleteAlarm(evt: DeleteAlarmEvent)(implicit conn: Connection): Int

  def queryAlarm(cmd: QueryCommand)(implicit conn: Connection): Seq[AlarmVo]

  def retrieveAlarmByRowid(rowid: String)(implicit conn: Connection): AlarmVo

  def beginAlarm(evt: BeginAlarmEvent)(implicit conn: Connection): Int

  def endAlarm(evt: EndAlarmEvent)(implicit conn: Connection): Int
}
