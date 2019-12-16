package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters.cToPascal
import org.scalatest._

class ModelLoaderSpec extends FlatSpec with Matchers {
  val model = fromClasspath("sales_entities.xml")
  import model._

  "A Model" should "have modelName == \"sales\"" in {
    modelName should not be("")
    modelName should be("sales")
  }

  it should "have modelVersion == \"1.0.0\"" in {
    modelVersion should not be("")
    modelVersion should be("1.0.0")
  }

  it should "have modelPackage == \"com.github.apuex.commerce.sales\"" in {
    modelPackage should not be("")
    modelPackage should be("com.github.apuex.commerce.sales")
  }

  it should "have modelMaintainer == \"xtwxy@hotmail.com\"" in {
    modelMaintainer should not be("")
    modelMaintainer should be("xtwxy@hotmail.com")
  }

  it should "have modelDbSchema == \"sales\"" in {
    modelDbSchema should not be("")
    modelDbSchema should be("sales")
  }

  "A ModelLoader" should "load aggregate roots" in {
    val aggregates = modelXml.child.filter(x => x.label == "entity" && x.\@("name") == "alarm")
      .map(x => {
        if ("" == x.\@("aggregatesTo")) Some(toAggregate(x, modelXml))
        else None
      })
      .filter(_.isDefined)
      .map(_.get)

    aggregates should be(
      List(
        Aggregate(
          "alarm",
          false,
          List(
            Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
            Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间"),
            Field("alarm_end", "timestamp", 0, 0, false, "", "", "", "", "", false, false, "告警结束时间"),
            Field("alarm_desc", "string", 64, 0, true, "", "", "", "", "", false, false, "告警描述")
          ),
          List(),
          List(
            Message(
              "begin_alarm",
              List(
                Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
                Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间"),
                Field("alarm_desc", "string", 64, 0, true, "", "", "", "", "", false, false, "告警描述")
              ),
              PrimaryKey(
                "alarm_pk",
                List(
                  Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
                  Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间")
                ),
                false
              ),
              false,
              ""
            ),
            Message(
              "end_alarm",
              List(
                Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
                Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间"),
                Field("alarm_end", "timestamp", 0, 0, false, "", "", "", "", "", false, false, "告警结束时间"),
                Field("alarm_desc", "string", 64, 0, true, "", "", "", "", "", false, false, "告警描述")
              ),
              PrimaryKey(
                "alarm_pk",
                List(
                  Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
                  Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间")
                ),
                false
              ),
              false,
              ""
            )
          ),
          PrimaryKey(
            "alarm_pk",
            List(
              Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
              Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间")
            ),
            false
          ),
          List(),
          false
        )
      )
    )
  }

  it should "getPrimaryKey" in {
    val primaryKeys = modelXml.child.filter(_.label == "entity")
      .map(x => {
        val primaryKey = getPrimaryKey(x, modelXml)
        x.child.filter(_.label == "aggregate")
          .map(x => (x.\@("name") -> primaryKey)) :+ (x.\@("name") -> primaryKey)
      })
      .flatMap(x => x)

    primaryKeys should be(
      Seq(
        "alarm" -> PrimaryKey(
          "alarm_pk",
          List(
            Field("alarm_id", "string", 64, 0, true, "", "", "", "", "", false, false, "告警对象ID"),
            Field("alarm_begin", "timestamp", 0, 0, true, "", "", "", "", "", false, false, "告警发生时间")
          ),
          false
        ),
        "payment_type" -> PrimaryKey("payment_type_pk", List(Field("payment_type_id", "int", 0, 0, true, "", "", "", "", "", false, false, "支付方式代码")), false),
        "product_sales" -> PrimaryKey("product_pk", List(Field("product_id", "string", 64, 0, true, "", "", "", "", "", false, false, "商品编号")), false),
        "product" -> PrimaryKey("product_pk", List(Field("product_id", "string", 64, 0, true, "", "", "", "", "", false, false, "商品编号")), false),
        "order" -> PrimaryKey("order_pk", List(Field("order_id", "string", 64, 0, true, "", "", "", "", "", false, false, "订单编号")), false),
        "order_item" -> PrimaryKey(
          "order_item_pk",
          List(
            Field("order_id", "string", 64, 0, true, "", "", "", "", "", false, false, "订单编号"),
            Field("product_id", "string", 64, 0, true, "", "", "", "", "", false, false, "商品编号")
          ),
          false
        ),
        "event_journal" -> PrimaryKey("event_pk",List(Field("offset","long",0,0,true,"","","","","",false,false,"事件顺序号")),true)
      )
    )
  }

  it should "getAggregates" in {

  }

  it should "define field type by name" in {
    defFieldType("bool") should be("Boolean")
    defFieldType("short") should be("Short")
    defFieldType("byte") should be("Byte")
    defFieldType("int") should be("Int")
    defFieldType("long") should be("Long")
    defFieldType("decimal") should be("BigDecimal")
    defFieldType("string") should be("String")
    defFieldType("timestamp") should be("Timestamp")
    defFieldType("float") should be("Float")
    defFieldType("double") should be("Double")
    defFieldType("blob") should be("ByteString")
    defFieldType("payment_type") should be("PaymentType")
    defFieldType("product") should be("ProductVo")
    defFieldType("product_name") should be("ProductNameVo")
    defFieldType("product_unit") should be("ProductUnitVo")
    defFieldType("unit_price") should be("UnitPriceVo")
    defFieldType("product_sales") should be("ProductSalesVo")
    defFieldType("order") should be("OrderVo")
    defFieldType("order_item") should be("OrderItemVo")
    defFieldType("order_payment_type") should be("OrderPaymentTypeVo")
  }

  it should "define field type by field descriptor" in {
    defFieldType(userField) should be ("String")
  }

  it should "define method params by field descriptors" in {
    val selectByPkDefs = modelXml.child.filter(_.label == "entity")
      .map(x => (x.\@("name"), getPrimaryKey(x, modelXml)))
      .map(x => s"def selectBy${cToPascal(x._2.name)}(${defMethodParams(x._2.fields)}): ${cToPascal(x._1)}Vo")
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    selectByPkDefs should be(
      s"""
         |def selectByAlarmPk(alarmId: String, alarmBegin: Option[Timestamp]): AlarmVo
         |def selectByPaymentTypePk(paymentTypeId: Int): PaymentTypeVo
         |def selectByProductPk(productId: String): ProductVo
         |def selectByOrderPk(orderId: String): OrderVo
         |def selectByOrderItemPk(orderId: String, productId: String): OrderItemVo
         |def selectByEventPk(offset: Long): EventJournalVo
       """.stripMargin.trim
    )
  }


}
