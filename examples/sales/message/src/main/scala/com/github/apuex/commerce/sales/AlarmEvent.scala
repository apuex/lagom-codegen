/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales
import com.google.protobuf.timestamp.Timestamp

trait AlarmEvent extends Event {
  def alarmId: String
  def alarmBegin: Option[Timestamp]
}
