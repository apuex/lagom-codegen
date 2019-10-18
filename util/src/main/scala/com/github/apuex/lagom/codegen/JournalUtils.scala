package com.github.apuex.lagom.codegen

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

object JournalUtils {

  def createJournalEvent(journalTable: String, alias: String, seq: String, uuid: String): String =
    s"""
       |Create${cToPascal(journalTable)}Event(${alias}.userId, ${seq}, ${alias}.entityId, ${uuid}, ${alias}.getClass.getName, ${alias}.toByteString)
     """.stripMargin.trim
}
