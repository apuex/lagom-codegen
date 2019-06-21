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
    val aggregates = xml.child.filter(_.label == "entity")
      .map(x => {
        if ("" == x.\@("aggregatesTo")) Some(toAggregate(x, xml))
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
            Field("alarm_id", "string", 64, false, "", "", false, false, ""),
            Field("alarm_begin", "timestamp", 0, false, "", "", true, false, ""),
            Field("alarm_end", "timestamp", 0, false, "", "", true, false, ""),
            Field("alarm_desc", "string", 64, false, "", "", false, false, "")
          ),
          List(
            Aggregate(
              "alarm_begin",
              false,
              List(
                Field("alarm_id", "string", 64, false, "", "", false, false, ""),
                Field("alarm_begin", "timestamp", 0, false, "", "", true, false, ""),
              ),
              List(),
              List(),
              PrimaryKey(
                "alarm_pk",
                List(
                  Field("alarm_id", "string", 64, false, "", "", false, false, ""),
                  Field("alarm_begin", "timestamp", 0, false, "", "", true, false, "")
                )
              ),
              List(),
              false
            ),
            Aggregate(
              "alarm_end",
              false,
              List(
                Field("alarm_id", "string", 64, false, "", "", false, false, ""),
                Field("alarm_begin", "timestamp", 0, false, "", "", true, false, ""),
                Field("alarm_end", "timestamp", 0, false, "", "", true, false, "")
              ),
              List(),
              List(),
              PrimaryKey(
                "alarm_pk",
                List(
                  Field("alarm_id", "string", 64, false, "", "", false, false, ""),
                  Field("alarm_begin", "timestamp", 0, false, "", "", true, false, "")
                )
              ),
              List(),
              false)
          ),
          List(),
          PrimaryKey(
            "alarm_pk",
            List(
              Field("alarm_id", "string", 64, false, "", "", false, false, ""),
              Field("alarm_begin", "timestamp", 0, false, "", "", true, false, "")
            )
          ),
          List(),
          false
        ),
        Aggregate(
          "payment_type",
          false,
          List(
            Field(
              "payment_type_id",
              "int",
              0,
              false,
              "",
              "",
              false,
              false,
              "支付方式代码"
            ),
            Field(
              "payment_type_name",
              "string",
              64,
              false,
              "",
              "",
              false,
              false,
              "支付方式常量符号"
            ),
            Field(
              "payment_type_label",
              "string",
              64,
              false,
              "",
              "",
              false,
              false,
              "支付方式文字描述"
            )
          ),
          List(),
          List(),
          PrimaryKey(
            "payment_type_pk",
            List(
              Field(
                "payment_type_id",
                "int",
                0,
                false,
                "",
                "",
                false,
                false,
                "支付方式代码"
              )
            )
          ),
          List(),
          false
        ),
        Aggregate(
          "product",
          false,
          List(
            Field("product_id", "string", 64, false, "", "", false, false, "商品编号"),
            Field("product_name", "string", 64, false, "", "", true, false, "商品名称"),
            Field("product_unit", "string", 64, false, "", "", true, false, "计价单位"),
            Field("unit_price", "double", 0, false, "", "", true, false, "单价"),
            Field("record_time", "timestamp", 0, false, "", "", false, true, "销量最后更新时间"),
            Field("quantity_sold", "double", 0, false, "", "", false, true, "销量")
          ),
          List(
            Aggregate(
              "product_sales",
              false,
              List(
                Field("product_id", "string", 64, false, "", "", false, false, "商品编号"),
                Field("record_time", "timestamp", 0, false, "", "", false, true, "销量最后更新时间"),
                Field("quantity_sold", "double", 0, false, "", "", false, true, "销量")
              ),
              List(),
              List(),
              PrimaryKey(
                "product_pk",
                List(
                  Field("product_id", "string", 64, false, "", "", false, false, "商品编号")
                )
              ),
              List(),
              false
            ),
            Aggregate(
              "product_name",
              false,
              List(
                Field("product_id", "string", 64, false, "", "", false, false, "商品编号"),
                Field("product_name", "string", 64, false, "", "", true, false, "商品名称")
              ),
              List(),
              List(),
              PrimaryKey(
                "product_pk",
                List(
                  Field("product_id", "string", 64, false, "", "", false, false, "商品编号")
                )
              ),
              List(),
              false
            ),
            Aggregate(
              "product_unit",
              false,
              List(
                Field("product_id", "string", 64, false, "", "", false, false, "商品编号"),
                Field("product_unit", "string", 64, false, "", "", true, false, "计价单位")
              ),
              List(),
              List(),
              PrimaryKey(
                "product_pk",
                List(
                  Field("product_id", "string", 64, false, "", "", false, false, "商品编号")
                )
              ),
              List(),
              false
            ),
            Aggregate(
              "unit_price",
              false,
              List(
                Field("product_id", "string", 64, false, "", "", false, false, "商品编号"),
                Field("unit_price", "double", 0, false, "", "", true, false, "单价")
              ),
              List(),
              List(),
              PrimaryKey(
                "product_pk",
                List(
                  Field("product_id", "string", 64, false, "", "", false, false, "商品编号")
                )
              ),
              List(),
              false
            )
          ),
          List(),
          PrimaryKey(
            "product_pk",
            List(
              Field("product_id", "string", 64, false, "", "", false, false, "商品编号")
            )
          ),
          List(),
          false
        ),
        Aggregate(
          "order",
          false,
          List(
            Field("order_id", "string", 64, false, "", "", false, false, "订单编号"),
            Field("order_time", "timestamp", 0, false, "", "", false, false, "下单时间"),
            Field("order_lines", "array", 0, false, "", "order_item", true, false, "购买清单"),
            Field("order_payment_type", "payment_type", 0, false, "", "", true, false, "支付方式")
          ),
          List(
            Aggregate(
              "order_lines",
              false,
              List(
                Field("order_id", "string", 64, false, "", "", false, false, "订单编号"),
                Field("order_lines", "array", 0, false, "", "order_item", true, false, "购买清单")
              ),
              List(),
              List(),
              PrimaryKey(
                "order_pk",
                List(
                  Field("order_id", "string", 64, false, "", "", false, false, "订单编号")
                )
              ),
              List(),
              false
            ),
            Aggregate(
              "order_payment_type",
              false,
              List(
                Field("order_id", "string", 64, false, "", "", false, false, "订单编号"),
                Field("order_payment_type", "payment_type", 0, false, "", "", true, false, "支付方式")
              ),
              List(),
              List(),
              PrimaryKey(
                "order_pk",
                List(
                  Field("order_id", "string", 64, false, "", "", false, false, "订单编号")
                )
              ),
              List(),
              false
            )
          ),
          List(),
          PrimaryKey(
            "order_pk",
            List(
              Field("order_id", "string", 64, false, "", "", false, false, "订单编号")
            )
          ),
          List(),
          false
        )
      )
    )
  }

  it should "getPrimaryKey" in {
    val primaryKeys = xml.child.filter(_.label == "entity")
      .map(x => {
        val primaryKey = getPrimaryKey(x, xml)
        x.child.filter(_.label == "aggregate")
          .map(x => (x.\@("name") -> primaryKey)) :+ (x.\@("name") -> primaryKey)
      })
      .flatMap(x => x)

    primaryKeys should be(
      Seq(
        "alarm" -> PrimaryKey(
          "alarm_pk",
          List(
            Field("alarm_id", "string", 64, false, "", "", false, false, ""),
            Field("alarm_begin", "timestamp", 0, false, "", "", true, false, "")
          )
        ),
        "payment_type" -> PrimaryKey("payment_type_pk", List(Field("payment_type_id", "int", 0, false, "", "", false, false, "支付方式代码"))),
        "product_sales" -> PrimaryKey("product_pk", List(Field("product_id", "string", 64, false, "", "", false, false, "商品编号"))),
        "product" -> PrimaryKey("product_pk", List(Field("product_id", "string", 64, false, "", "", false, false, "商品编号"))),
        "order" -> PrimaryKey("order_pk", List(Field("order_id", "string", 64, false, "", "", false, false, "订单编号"))),
        "order_item" -> PrimaryKey("order_item_pk", List(Field("order_id", "string", 64, false, "", "", false, false, "订单编号"), Field("product_id", "string", 64, false, "", "", false, false, "商品编号")))
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
    defFieldType("blob") should be("Bytes")
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
    val selectByPkDefs = xml.child.filter(_.label == "entity")
      .map(x => (x.\@("name"), getPrimaryKey(x, xml)))
      .map(x => s"def selectBy${cToPascal(x._2.name)}(${defMethodParams(x._2.fields)}): ${cToPascal(x._1)}Vo")
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    println(selectByPkDefs)
    selectByPkDefs should be(
      s"""
         |def selectByAlarmPk(alarmId: String, alarmBegin: Timestamp): AlarmVo
         |def selectByPaymentTypePk(paymentTypeId: Int): PaymentTypeVo
         |def selectByProductPk(productId: String): ProductVo
         |def selectByOrderPk(orderId: String): OrderVo
         |def selectByOrderItemPk(orderId: String, productId: String): OrderItemVo
       """.stripMargin.trim
    )
  }


}
