/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait AlarmDao {
  def createAlarm(cmd: CreateAlarmCmd)(implicit conn: Connection): Int

  def retrieveAlarm(cmd: RetrieveAlarmCmd)(implicit conn: Connection): AlarmVo

  def updateAlarm(cmd: UpdateAlarmCmd)(implicit conn: Connection): Int

  def deleteAlarm(cmd: DeleteAlarmCmd)(implicit conn: Connection): Int

  def queryAlarm(cmd: QueryCommand)(implicit conn: Connection): Seq[AlarmVo]

  def retrieveAlarmByRowid(rowid: String)(implicit conn: Connection): AlarmVo

  def beginAlarm(cmd: BeginAlarmCmd)(implicit conn: Connection): Int

  def endAlarm(cmd: EndAlarmCmd)(implicit conn: Connection): Int
}
