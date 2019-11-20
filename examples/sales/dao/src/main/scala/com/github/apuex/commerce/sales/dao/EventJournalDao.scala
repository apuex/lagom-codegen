/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection
import java.util.UUID

trait EventJournalDao {
  def selectCurrentOffset()(implicit conn: Connection): EventJournalVo

  def createEventJournal(evt: CreateEventJournalEvent)(implicit conn: Connection): Int

  def retrieveEventJournal(cmd: RetrieveEventJournalCmd)(implicit conn: Connection): EventJournalVo

  def updateEventJournal(evt: UpdateEventJournalEvent)(implicit conn: Connection): Int

  def deleteEventJournal(evt: DeleteEventJournalEvent)(implicit conn: Connection): Int

  def queryEventJournal(cmd: QueryCommand)(implicit conn: Connection): Seq[EventJournalVo]

  def retrieveEventJournalByRowid(rowid: String)(implicit conn: Connection): EventJournalVo
}
