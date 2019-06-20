package com.github.apuex.commerce.sales
import com.google.protobuf.timestamp.Timestamp
import com.github.apuex.springbootsolution.runtime.DateFormat._

trait AlarmRecordCommand extends ShardingEntityCommand {
  def alarmRecordId: String
  def alarmRecordBegin: Option[Timestamp]
  override def entityId: String = {
    s"alarmRecord_${alarmRecordId}_${alarmRecordBegin.map(x => formatTimestamp(x.seconds * 1000 + x.nanos / 1000000))}"
  }
}
