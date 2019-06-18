package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.MessageGenerator._
import com.github.apuex.lagom.codegen.ModelLoader._
import org.scalatest._

import scala.xml.Node
import scala.xml.parsing.NoBindingFactoryAdapter

class MessageGeneratorSpec extends FlatSpec with Matchers {
  val factory = new NoBindingFactoryAdapter
  val xml: Node = factory.load(getClass.getClassLoader.getResourceAsStream("sales_entities.xml"))

  val model = ModelLoader(xml)

  "A MessageGenerator" should "generate" in {

  }

  it should "getPrimaryKey(s)" in {
    val primaryKeys = xml.child.filter(_.label == "entity")
      .map(x => {
        val primaryKey = getPrimaryKey(x, xml)
        x.child.filter(_.label == "aggregate")
          .map(x => (x.\@("name") -> primaryKey)) :+ (x.\@("name") -> primaryKey)
      })
      .flatMap(x => x)

    primaryKeys should be(
      Seq(
        "product_sales" -> PrimaryKey("product_pk", List(Field("product_id", "string", 64, false, "", "", false, ""))),
        "product" -> PrimaryKey("product_pk", List(Field("product_id", "string", 64, false, "", "", false, ""))),
        "order" -> PrimaryKey("order_pk", List(Field("order_id", "string", 64, false, "", "", false, ""))),
        "order_item" -> PrimaryKey("order_item_pk", List(Field("order_id", "string", 64, false, "", "", false, ""), Field("product_id", "string", 64, false, "", "", false, "")))
      )
    )
  }

  it should "getAggregates" in {

  }
}
