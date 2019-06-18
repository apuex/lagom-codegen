package com.github.apuex.lagom.codegen

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml.parsing._
import scala.xml._

object ModelLoader {
  def apply(fileName: String): ModelLoader = {
    val factory = new NoBindingFactoryAdapter
    new ModelLoader(factory.load(fileName))
  }

  def apply(xml: Node): ModelLoader = new ModelLoader(xml)


  case class Field(name: String, _type: String, length: Int, required: Boolean, keyType: String, valueType: String, aggregate: Boolean, comment: String)
  case class PrimaryKey(name: String, fields: Seq[Field])
  case class ForeignKeyField(name: String, refField:String, required: Boolean)
  case class ForeignKey(name: String, refEntity: String, fields: Seq[ForeignKeyField])
  case class Aggregate(name: String, root:Boolean, fields: Seq[Field], aggregates: Seq[Aggregate], primaryKey: PrimaryKey, foreignKeys: Seq[ForeignKey])
  case class ValueObject(name: String, fields: Seq[Field], primaryKey: PrimaryKey, foreignKeys: Seq[ForeignKey])
  case class Message(name: String, fields: Seq[Field], primaryKey: PrimaryKey)

  def importPackagesForService(model: Node, service: Node): String = {
    s"""
       |${importPackages(service)}
       |${importPackages(model)}
     """.stripMargin
      .trim
  }

  def importPackages(node: Node): String = {
    node.child.filter(x => x.label == "imports")
      .flatMap(x => x.child.filter(c => c.label == "import"))
      .map(x => x.text.trim)
      .map(x => x.replace("*", "_"))
      .map(x => x.replace("static", ""))
      .map(x => s"import ${x}")
      .foldLeft("")((l, r) => s"${l}\n${r}")
      .trim
  }
}

class ModelLoader(val xml: Node) {
  val mapping = "mapping"
  val message = "message"
  val api = "api"
  val dao = "dao"
  val impl = "impl"
  val srcSystem = xml.\@("from")
  val destSystem = xml.\@("to")
  val modelName = if("" == xml.\@("name").trim) s"${srcSystem}_${destSystem}_${mapping}" else xml.\@("name")
  val modelPackage = xml.\@("package")
  val modelVersion = xml.\@("version")
  val modelMaintainer = xml.\@("maintainer")
  val outputDir = s"${System.getProperty("output.dir", "target/generated")}"
  val rootProjectName = s"${cToShell(modelName)}"
  val rootProjectDir = s"${outputDir}/${rootProjectName}"
  val messageProjectName = s"${cToShell(modelName)}-${message}"
  val messageProjectDir = s"${rootProjectDir}/${message}"
  val messageSrcPackage = s"${modelPackage}"
  val messageSrcDir = s"${messageProjectDir}/src/main/scala/${modelPackage.replace('.', '/')}"
  val apiProjectName = s"${cToShell(modelName)}-${api}"
  val apiProjectDir = s"${rootProjectDir}/${api}"
  val apiSrcPackage = s"${modelPackage}"
  val apiSrcDir = s"${apiProjectDir}/src/main/scala/${modelPackage.replace('.', '/')}"
  val daoProjectName = s"${cToShell(modelName)}-${dao}"
  val daoProjectDir = s"${rootProjectDir}/${dao}"
  val daoSrcPackage = s"${modelPackage}"
  val daoSrcDir = s"${daoProjectDir}/src/main/scala/${modelPackage.replace('.', '/')}"
  val implProjectName = s"${cToShell(modelName)}-${impl}"
  val implProjectDir = s"${rootProjectDir}/${impl}"
  val implSrcPackage = s"${modelPackage}.${impl}"
  val implSrcDir = s"${implProjectDir}/src/main/scala/${implSrcPackage.replace('.', '/')}"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"
}
