package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.Node

object MysqlSchemaGenerator {
  def apply(fileName: String): MysqlSchemaGenerator = new MysqlSchemaGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): MysqlSchemaGenerator = new MysqlSchemaGenerator(modelLoader)
}

class MysqlSchemaGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    save(s"${cToShell(modelDbSchema)}-db-mysql-schema.ddl",
      generateDaoContent(xml),
      daoMysqlResDir)
  }

  def generateDaoContent(xml: Node): String = {
    val entities = xml.child.filter(_.label == "entity")
      .sortWith((x, y) => depends(x, y))
      .map(x => {
        val name = x.\@("name")
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo)
          toValueObject(x, name, xml)
        else
          toValueObject(x, aggregatesTo, xml)
      })

    val tables = entities
      .map(x => {
        s"""
           |CREATE TABLE ${modelDbSchema}.${x.name} (
           |  ${indent(defTableFields(x.fields), 2)}
           |) ENGINE=InnoDB DEFAULT CHARSET=utf8;
         """.stripMargin.trim
      })

    val primaryKeys = entities
      .map(x => {
        val keyFieldNames = x.primaryKey.fields
          .map(_.name)
          .reduceOption((l, r) => s"${l}, ${r}")
          .getOrElse("")
        s"""
           |ALTER TABLE ${modelDbSchema}.${x.name} ADD CONSTRAINT ${x.primaryKey.name} PRIMARY KEY(${keyFieldNames});
         """.stripMargin.trim
      })

    val foreignKeys = entities
      .map(x => {
        x.foreignKeys.map(k => {
          val keyFieldNames = k.fields
            .map(_.name)
            .reduceOption((l, r) => s"${l}, ${r}")
            .getOrElse("")
          val refFieldNames = k.fields
            .map(_.refField)
            .reduceOption((l, r) => s"${l}, ${r}")
            .getOrElse("")

          s"""
             |ALTER TABLE ${modelDbSchema}.${x.name} ADD CONSTRAINT ${k.name} FOREIGN KEY(${keyFieldNames}) REFERENCES ${modelDbSchema}.${k.refEntity}(${refFieldNames});
         """.stripMargin.trim
        })
      })
      .flatMap(x => x)

    s"""
       |${tables}
       |
       |${primaryKeys}
       |
       |${foreignKeys}
     """.stripMargin.trim
  }

  def depends(x: Node, y: Node): Boolean = {
    !x.child.filter(_.label == "foreignKey")
      .filter(p => p.\@("refEntity") == y.\@("name"))
      .isEmpty
  }

  def defTableFields(fields: Seq[ModelLoader.Field]): String = {
    fields
      .filter(x => "array" != x._type && "map" != x._type)
      .map(x => {
        val fieldType = toMysqlType(x.name, x.length)
        val nullOpt = if(x.required) "NOT NULL" else ""
        s"""
           |${x.name} ${fieldType} ${nullOpt}
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")
  }

  def toMysqlType(typeName: String, length: Int): String = typeName match {
    case "bool" => "TINYINT"
    case "short" => "SHORT"
    case "byte" => if(length == 0) "CHAR" else if(length > 0 && length < 256) s"CHAR(${length})" else "BLOB"
    case "int" => "INT"
    case "long" => "BIGINT"
    case "decimal" => "DECIMAL"
    case "string" => if(length < 256) s"VARCHAR(${length})" else "TEXT"
    case "text" => "TEXT"
    case "timestamp" => "DATETIME"
    case "float" => "FLOAT"
    case "double" => "DOUBLE"
    case "blob" => "BLOB"
    case _ => "int" // enum type
  }
}
