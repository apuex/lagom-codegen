package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._

class ModelTestGenerator(modelLoader: ModelLoader) {
  import modelLoader._

  def generate(): Unit = {
    save(
      "ModelAttributeSpec.scala",
      modelAttributeSpec(),
      modelTestSrcDir
    )
  }

  def modelAttributeSpec(): String =
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${modelPackage}
       |
       |import com.github.apuex.lagom.codegen.ModelLoader._
       |import org.scalatest._
       |
       |class ModelAttributeSpec extends FlatSpec with Matchers {
       |  val model = fromClasspath("sales_entities.xml")
       |  import model._
       |
       |  "A Model" should "have modelName == \\\"${modelName}\\\"" in {
       |    modelName should not be("")
       |    modelName should be("${modelName}")
       |  }
       |
       |  it should "have modelVersion == \\\"${modelVersion}\\\"" in {
       |    modelVersion should not be("")
       |    modelVersion should be("${modelVersion}")
       |  }
       |
       |  it should "have modelPackage == \\\"${modelPackage}\\\"" in {
       |    modelPackage should not be("")
       |    modelPackage should be("${modelPackage}")
       |  }
       |
       |  it should "have modelMaintainer == \\\"${modelMaintainer}\\\"" in {
       |    modelMaintainer should not be("")
       |    modelMaintainer should be("${modelMaintainer}")
       |  }
       |
       |  it should "have modelDbSchema == \\\"${modelDbSchema}\\\"" in {
       |    modelDbSchema should not be("")
       |    modelDbSchema should be("${modelDbSchema}")
       |  }
       |}
     """.stripMargin.trim
}

object ModelTestGenerator {
  def apply(modelFile: String): ModelTestGenerator = ModelTestGenerator(ModelLoader(modelFile))
  def apply(modelLoader: ModelLoader): ModelTestGenerator = new ModelTestGenerator(modelLoader)
}
