package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

object JsonSerializerGenerator {
  def apply(fileName: String): JsonSerializerGenerator = new JsonSerializerGenerator(ModelLoader(fileName))

  def apply(mappingLoader: ModelLoader): JsonSerializerGenerator = new JsonSerializerGenerator(mappingLoader)
}

class JsonSerializerGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  val scalapbJson =
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${apiSrcPackage}
       |
       |import com.github.apuex.events.play.{EventEnvelope, EventEnvelopeProto}
       |import com.google.protobuf.ByteString
       |import com.google.protobuf.any.Any
       |import play.api.libs.json._
       |import scalapb.json4s.JsonFormat.GenericCompanion
       |import scalapb.json4s._
       |import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
       |
       |object ScalapbJson {
       |  // json parser and printer
       |  val messagesCompanions = MessagesProto.messagesCompanions ++ EventEnvelopeProto.messagesCompanions
       |  val registry: TypeRegistry = messagesCompanions
       |    .foldLeft(TypeRegistry())((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
       |
       |  val printer = new Printer().withTypeRegistry(registry)
       |
       |  val parser = new Parser().withTypeRegistry(registry)
       |
       |  // any packager for pack/unpack messages.
       |  def pack(msg: GeneratedMessage): Any = Any.of(s"type.googleapis.com/$${msg.getClass.getName}", msg.asInstanceOf[GeneratedMessage].toByteString)
       |  def pack(className: String, content: ByteString): Any = Any.of(s"type.googleapis.com/$${className}", content)
       |  def unpack(any: Any): GeneratedMessage = registry.findType(any.typeUrl)
       |    .map(_.parseFrom(any.value.newCodedInput())).get
       |
       |  def parseJson(json: String): EventEnvelope = {
       |    parser.fromJsonString[EventEnvelope](json)
       |  }
       |
       |  class MessageFormat[T <: GeneratedMessage with Message[T] : GeneratedMessageCompanion] extends OFormat[T] {
       |    override def reads(json: JsValue): JsResult[T] = {
       |      JsSuccess(parser.fromJsonString[T](json.toString()))
       |    }
       |
       |    override def writes(o: T): JsObject = Json.parse(
       |      printer.print(o)
       |    ).validate[JsObject].get
       |  }
       |
       |  def jsonFormat[T <: GeneratedMessage with Message[T] : GeneratedMessageCompanion]: OFormat[T] = new MessageFormat[T]
       |}
     """.stripMargin.trim

  def generate(): Unit = {
    new File(apiSrcDir).mkdirs()
    val printWriter = new PrintWriter(s"${apiSrcDir}/ScalapbJson.scala", "utf-8")
    printWriter.println(scalapbJson)
    printWriter.close()
  }
}
