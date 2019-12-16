package com.github.apuex.lagom.codegen

import org.scalatest._
import com.github.apuex.lagom.codegen.ModelLoader._

class DaoMysqlImplGeneratorSpec extends FlatSpec with Matchers {
  val model = fromClasspath("sales_entities.xml")
  val daoImplGenerator = DaoMysqlImplGenerator(model)

  import daoImplGenerator._
  import model._

  "A DaoMysqlImplGenerator" should "generate dao dependencies" in {
    val daoDependencies = modelXml.child.filter(x => x.label == "entity" && x.\@("name") == "order")
      .map(x => toValueObject(x, "order", modelXml))
      .map(x => defDaoDependencies(x.fields))
      .reduceOption((l, r) => s"$l\n$r")
      .getOrElse("")

    daoDependencies should be("orderItemDao: OrderItemDao")
  }
}
