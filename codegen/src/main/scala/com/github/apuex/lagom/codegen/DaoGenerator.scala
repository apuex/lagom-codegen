package com.github.apuex.lagom.codegen

object DaoGenerator{
  def apply(fileName: String): DaoGenerator = new DaoGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): DaoGenerator = new DaoGenerator(modelLoader)
}

class DaoGenerator(modelLoader: ModelLoader) {
  def generate(): Unit = {}
}