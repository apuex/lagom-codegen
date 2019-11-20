/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/
package com.github.apuex.commerce.sales

import com.github.apuex.lagom.codegen.ModelLoader._
import org.scalatest._

class ModelAttributeSpec extends FlatSpec with Matchers {
  val model = fromClasspath("/home/wangxy/github/apuex/lagom-codegen/examples/sales/model/src/test/resources/sales_entities.xml")
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
}
