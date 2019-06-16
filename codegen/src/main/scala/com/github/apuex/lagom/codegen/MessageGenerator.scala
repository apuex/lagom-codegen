package com.github.apuex.lagom.codegen

object MessageGenerator {
  def apply(fileName: String): MessageGenerator = new MessageGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MessageGenerator = new MessageGenerator(modelLoader)
}

class MessageGenerator(modelLoader: ModelLoader) {

  def generate(): Unit = {}
}
