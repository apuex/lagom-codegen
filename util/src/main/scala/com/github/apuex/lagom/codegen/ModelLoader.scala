package com.github.apuex.lagom.codegen

import java.io.{File, PrintWriter}

import com.github.apuex.springbootsolution.runtime.SymbolConverters._

import scala.xml._
import scala.xml.parsing._

object ModelLoader {
  def apply(fileName: String): ModelLoader = {
    val factory = new NoBindingFactoryAdapter
    ModelLoader(factory.load(fileName), fileName)
  }

  def fromClasspath(path: String): ModelLoader = {
    val factory = new NoBindingFactoryAdapter
    val xml: Node = factory.load(getClass.getClassLoader.getResourceAsStream(path))
    ModelLoader(xml, path)
  }

  def apply(xml: Node, modelFileName: String): ModelLoader = new ModelLoader(xml, modelFileName)

  case class Field(name: String, _type: String, length: Int, required: Boolean, keyType: String, valueType: String, aggregate: Boolean, transient: Boolean, comment: String)

  case class PrimaryKey(name: String, fields: Seq[Field])

  case class ForeignKeyField(name: String, refField: String, required: Boolean)

  case class ForeignKey(name: String, refEntity: String, fields: Seq[ForeignKeyField])

  case class Message(name: String, fields: Seq[Field], primaryKey: PrimaryKey, transient: Boolean)

  case class Aggregate(name: String, root: Boolean, fields: Seq[Field], aggregates: Seq[Aggregate], messages: Seq[Message], primaryKey: PrimaryKey, foreignKeys: Seq[ForeignKey], transient: Boolean)

  case class ValueObject(name: String, fields: Seq[Field], primaryKey: PrimaryKey, foreignKeys: Seq[ForeignKey], transient: Boolean)

  case class EnumOption(value: Int, name: String, label: String)

  case class Enumeration(name: String, options: Seq[EnumOption])

  val userField = Field("user_id", "string", 64, false, "", "", false, false, "用户ID")

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

  def toField(node: Node): Field = {
    val name = node.\@("name")
    val _type = node.\@("type")
    val length = if ("" == node.\@("length")) 0 else node.\@("length").toInt
    val required = if ("true" == node.\@("required")) true else false
    val keyType = node.\@("keyType")
    val valueType = node.\@("valueType")
    val aggregate = if ("true" == node.\@("aggregate")) true else false
    val transient = if ("true" == node.\@("transient")) true else false
    val comment = node.\@("comment")
    Field(name, _type, length, required, keyType, valueType, aggregate, transient, comment)
  }

  def getFieldNames(node: Node): Seq[String] = {
    node.child.filter(_.label == "field")
      .map(_.\@("name"))
  }

  def getFields(node: Node, root: Node): Seq[Field] = {
    val foreignKeys = getForeignKeys(node)
    val referenced = foreignKeys
      .flatMap(x => x.fields.map(f => getReferencedColumn(f.name, foreignKeys, root)))
      .map(_.get)

    val defined = node.child.filter(_.label == "field")
      .map(x => x.\@("type") match {
        case "" =>
          val name = x.\@("name")
          val refKey = x.\@("refKey")
          val refField = x.\@("refField")
          val refEntity = foreignKeys.filter(_.name == refKey)
            .map(_.refEntity)
          if (refEntity.isEmpty) {
            println(s"name = ${name}")
            println(s"refKey = ${refKey}")
            println(s"refField = ${refField}")
          }
          getReferencedColumn(name, refKey, refEntity.head, refField, root)
        case _ => Some(toField(x))
      })
      .map(_.get) // throws java.util.NoSuchElementException if the option is empty.

    val all = defined ++ referenced

    all
  }

  def getPrimaryKey(node: Node, root: Node): PrimaryKey = {
    val pks = node.child.filter(_.label == "primaryKey")
    if (pks.isEmpty) {
      val aggregatesTo = node.\@("aggregatesTo")
      getPrimaryKey(root.child.filter(_.label == aggregatesTo).head, root)
    } else {
      val pk = pks.head
      val pkName = pk.\@("name")
      val pkColumnNames = pk.child.filter(_.label == "field")
        .map(_.\@("name"))

      val foreignKeys = getForeignKeys(node)

      val fields = getFields(node, root)
        .map(x => (x.name -> x))
        .toMap

      val pkColumns = pkColumnNames
        .map(x => {
          fields.getOrElse(x, getReferencedColumn(x, foreignKeys, root).get)
        })

      PrimaryKey(pkName, pkColumns)
    }
  }

  def getReferencedColumn(name: String, foreignKeys: Seq[ForeignKey], root: Node): Option[Field] = {
    //println(s"getReferencedColumn: ${name}, ${foreignKeys}")
    val (fkField, refField) = foreignKeys
      .map(k => (k, k.fields.filter(_.name == name)))
      .filter(x => !x._2.isEmpty)
      .map(x => (x._1, x._2.head))
      .map(x => (x, getReferencedColumn(x._2.name, x._1.name, x._1.refEntity, x._2.refField, root)))
      .map(x => (x._1._2, x._2))
      .head

    // disable aggregate attribute on referenced columns
    refField
      .map(x => Field(fkField.name, x._type, x.length, fkField.required, x.keyType, x.valueType, false, x.transient, x.comment))
  }

  def getReferencedColumn(name: String, refKey: String, refEntity: String, refField: String, root: Node): Option[Field] = {
    val node = root.child.filter(x => x.label == "entity" && x.\@("name") == refEntity).head
    Some(getFields(node, root)
      .filter(_.name == refField).head)
      // disable aggregate attribute on referenced columns
      .map(x => Field(name, x._type, x.length, false, x.keyType, x.valueType, false, x.transient, x.comment))
  }

  def getForeignKeys(node: Node): Seq[ForeignKey] = {
    node.child.filter(_.label == "foreignKey")
      .map(x => {
        ForeignKey(
          x.\@("name"),
          x.\@("refEntity"),
          x.child.filter(_.label == "field")
            .map(f => ForeignKeyField(f.\@("name"), f.\@("refField"), if ("true" == f.\@("required")) true else false))
        )
      })
  }

  def shuffleFields(fields: Seq[Field], pkFields: Seq[Field]): Seq[Field] = {
    val pkNames = pkFields.map(_.name).toSet
    pkFields ++ fields.filter(x => !pkNames.contains(x.name))
  }

  def toAggregate(node: Node, parentFields: Seq[Field], primaryKey: PrimaryKey, root: Node): Aggregate = {
    val transient = if ("true" == node.\@("transient")) true else false
    val fieldNames = getFieldNames(node).toSet
    val fields = parentFields.filter(x => fieldNames.contains(x.name))
    Aggregate(
      node.\@("name"),
      false,
      shuffleFields(primaryKey.fields ++ fields, primaryKey.fields),
      Seq(),
      Seq(),
      primaryKey,
      Seq(),
      transient
    )
  }

  def toAggregate(field: Field, primaryKey: PrimaryKey, root: Node): Aggregate = {
    Aggregate(
      field.name,
      false,
      shuffleFields(primaryKey.fields :+ field, primaryKey.fields),
      Seq(),
      Seq(),
      primaryKey,
      Seq(),
      field.transient
    )
  }

  def toMessage(node: Node, primaryKey: PrimaryKey, root: Node): Message = {
    val transient = if ("true" == node.\@("transient")) true else false
    Message(
      node.\@("name"),
      shuffleFields(primaryKey.fields ++ getFields(node, root), primaryKey.fields),
      primaryKey,
      transient
    )
  }

  def toAggregate(node: Node, root: Node): Aggregate = {
    val primaryKey = getPrimaryKey(node, root)
    val fields = shuffleFields(getFields(node, root), primaryKey.fields)
    val aggregates = node.child.filter(_.label == "aggregate").map(toAggregate(_, fields, primaryKey, root)) ++
      fields.filter(_.aggregate)
        .map(x => toAggregate(x, primaryKey, root))
    Aggregate(
      node.\@("name"),
      if ("true" == node.\@("root")) true else false,
      fields,
      aggregates,
      node.child.filter(_.label == "message").map(toMessage(_, primaryKey, root)),
      primaryKey,
      getForeignKeys(node),
      if ("true" == node.\@("transient")) true else false
    )
  }

  def toValueObject(node: Node, aggregatesTo: String, root: Node): ValueObject = {
    val primaryKey = getPrimaryKey(node, root)
    ValueObject(
      node.\@("name"),
      shuffleFields(getFields(node, root), primaryKey.fields),
      primaryKey,
      getForeignKeys(node),
      if ("true" == node.\@("transient")) true else false
    )
  }

  def toEnumeration(node: Node, aggregatesTo: String, root: Node): Enumeration = {
    val enumDef = node.child.filter(_.label == "enum").head
    val valueField = enumDef.\@("valueField")
    val nameField = enumDef.\@("nameField")
    val labelField = enumDef.\@("labelField")
    val options = node.child.filter(_.label == "row")
      .map(x => {
        EnumOption(
          x.\@(valueField).toInt,
          x.\@(nameField),
          x.\@(labelField)
        )
      })
    Enumeration(
      node.\@("name"),
      options
    )
  }

  def save(fileName: String, content: String, dir: String): Unit = {
    new File(dir).mkdirs()
    val pw = new PrintWriter(new File(dir, fileName), "utf-8")
    pw.println(content)
    pw.close()
  }
}

class ModelLoader(val xml: Node, val modelFileName: String) {
  val model = "model"
  val message = "message"
  val api = "api"
  val dao = "dao"
  val service = "service"
  val impl = "impl"
  val app: String = "app"
  val loader: String = "loader"
  val modelName = xml.\@("name")
  val modelPackage = xml.\@("package")
  val modelVersion = xml.\@("version")
  val modelMaintainer = xml.\@("maintainer")
  val modelDbSchema = xml.\@("dbSchema")
  val outputDir = s"${System.getProperty("output.dir", "target/generated")}"
  val rootProjectName = s"${cToShell(modelName)}"
  val rootProjectDir = s"${outputDir}/${rootProjectName}"
  val modelProjectName = s"${cToShell(modelName)}-${model}"
  val modelProjectDir = s"${rootProjectDir}/${model}"
  val modelSrcPackage = s"${modelPackage}"
  val modelTestSrcDir = s"${modelProjectDir}/src/test/scala/${modelPackage.replace('.', '/')}"
  val messageProjectName = s"${cToShell(modelName)}-${message}"
  val messageProjectDir = s"${rootProjectDir}/${message}"
  val messageSrcPackage = s"${modelPackage}"
  val messageSrcDir = s"${messageProjectDir}/src/main/scala/${modelPackage.replace('.', '/')}"
  val messageProtoDir = s"${messageProjectDir}/src/main/protobuf"
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
  val appProjectName = s"${cToShell(modelName)}-${cToShell(app)}"
  val appProjectDir = s"${rootProjectDir}/${app}"
  val applicationConfDir = s"${appProjectDir}/conf"
  val hyphen = if ("microsoft" == s"${System.getProperty("symbol.naming", "microsoft")}") "" else "-"
  val nonEnumNames = xml.child.filter(x => x.label == "entity" && "true" != x.\@("enum"))
    .map(_.\@("name"))
    .toSet
  val enumNames = xml.child.filter(x => x.label == "entity" && "true" == x.\@("enum"))
    .map(_.\@("name"))
    .toSet

  def isEntity(name: String): Boolean = nonEnumNames.contains(name)
  def isEnum(name: String): Boolean = !nonEnumNames.contains(name) && enumNames.contains(name)
}
