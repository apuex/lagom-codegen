package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import org.scalatest._

import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter

class ModelLoaderSpec extends FlatSpec with Matchers {
  val factory = new NoBindingFactoryAdapter
  val xml: Node = factory.load(getClass.getClassLoader.getResourceAsStream("sales_entities.xml"))

  val model = ModelLoader(xml)
  import model._

  "A ModelLoader" should "load modelName" in {
    modelName should be("sales")
  }

  it should "load modelVersion" in {
    modelVersion should be("1.0.0")
  }

  it should "load modelPackage" in {
    modelPackage should be("com.github.apuex.commerce.sales")
  }

  it should "load modelMaintainer" in {
    modelMaintainer should be("xtwxy@hotmail.com")
  }

  it should "generate aggregate roots" in {
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
            Field("record_time", "double", 0, false, "", "", false, true, "销量最后更新时间"),
            Field("quantity_sold", "double", 0, false, "", "", false, true, "销量")
          ),
          List(
            Aggregate(
              "product_sales",
              false,
              List(
                Field("product_id", "string", 64, false, "", "", false, false, "商品编号"),
                Field("record_time", "double", 0, false, "", "", false, true, "销量最后更新时间"),
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
            Field("order_lines", "array", 0, false, "", "order_item", true, false, "购买清单")
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
}
