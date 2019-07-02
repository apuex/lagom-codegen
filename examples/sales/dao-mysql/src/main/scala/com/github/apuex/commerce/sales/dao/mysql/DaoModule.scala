/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales.dao.mysql

import com.softwaremill.macwire._

@Module
class DaoModule {
  lazy val alarmDao = wire[AlarmDaoImpl]

  lazy val paymentTypeDao = wire[PaymentTypeDaoImpl]

  lazy val productDao = wire[ProductDaoImpl]

  lazy val orderDao = wire[OrderDaoImpl]

  lazy val orderItemDao = wire[OrderItemDaoImpl]

  lazy val eventJournalDao = wire[EventJournalDaoImpl]
}
