package com.github.apuex.commerce.sales
import com.google.protobuf.timestamp.Timestamp
import com.github.apuex.springbootsolution.runtime.DateFormat._

trait AlarmCommand extends ShardingEntityCommand {
  def alarmId: String
  def alarmBegin: Option[Timestamp]
  override def entityId: String = {
    s"alarm_${alarmId}_${alarmBegin.map(x => formatTimestamp(x.seconds * 1000 + x.nanos / 1000000))}"
  }
}
