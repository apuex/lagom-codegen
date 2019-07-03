/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales
import com.google.protobuf.timestamp.Timestamp
import com.github.apuex.springbootsolution.runtime.DateFormat._

trait EventJournalCommand extends ShardingEntityCommand {
  def persistenceId: String
  def occurredTime: Option[Timestamp]
  override def entityId: String = {
    s"eventJournal_${persistenceId}_${occurredTime.map(x => formatTimestamp(x.seconds * 1000 + x.nanos / 1000000)).getOrElse("")}"
  }
}
