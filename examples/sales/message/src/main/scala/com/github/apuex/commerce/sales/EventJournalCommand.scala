/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales


trait EventJournalCommand extends Command {
  def offset: Long
  override def entityId: String = {
    s"eventJournal_${offset}"
  }
}
