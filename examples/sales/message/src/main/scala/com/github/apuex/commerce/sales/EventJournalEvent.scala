/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales


trait EventJournalEvent extends Event {
  def offset: Long
  override def entityId: String = {
    s"eventJournal_${offset}"
  }
}
