/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales
import com.google.protobuf.timestamp.Timestamp

trait EventJournalEvent extends Event {
  def persistenceId: String
  def occurredTime: Option[Timestamp]
}
