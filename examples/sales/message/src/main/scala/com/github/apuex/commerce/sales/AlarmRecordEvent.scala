/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales
import com.google.protobuf.timestamp.Timestamp

trait AlarmRecordEvent extends Event {
  def alarmRecordId: String
  def alarmRecordBegin: Option[Timestamp]
}
