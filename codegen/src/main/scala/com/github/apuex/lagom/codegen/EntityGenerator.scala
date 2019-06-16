package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

object EntityGenerator{
  def apply(fileName: String): EntityGenerator = new EntityGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): EntityGenerator = new EntityGenerator(modelLoader)
}

class EntityGenerator(modelLoader: ModelLoader) {
  def generate(): Unit = {}
}