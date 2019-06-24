package com.github.apuex.commerce.sales.dao.mysql

import com.softwaremill.macwire._

@Module
class DaoModule {
  lazy val alarmDao = wire[AlarmDaoImpl]
  lazy val productDao = wire[ProductDaoImpl]
  lazy val paymentTypeDao = wire[PaymentTypeDaoImpl]
  lazy val orderDao = wire[OrderDaoImpl]
  lazy val orderItemDao = wire[OrderItemDaoImpl]
}
