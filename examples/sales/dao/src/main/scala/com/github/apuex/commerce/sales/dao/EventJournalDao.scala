/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao

import com.github.apuex.commerce.sales._
import com.github.apuex.springbootsolution.runtime._
import com.google.protobuf.timestamp.Timestamp
import java.sql.Connection

trait EventJournalDao {
  def createEventJournal(cmd: CreateEventJournalCmd)(implicit conn: Connection): Int

  def retrieveEventJournal(cmd: RetrieveEventJournalCmd)(implicit conn: Connection): EventJournalVo

  def updateEventJournal(cmd: UpdateEventJournalCmd)(implicit conn: Connection): Int

  def deleteEventJournal(cmd: DeleteEventJournalCmd)(implicit conn: Connection): Int

  def queryEventJournal(cmd: QueryCommand)(implicit conn: Connection): Seq[EventJournalVo]

  def retrieveEventJournalByRowid(rowid: String)(implicit conn: Connection): EventJournalVo
}
