package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.Node

object DaoGenerator {
  def apply(fileName: String): DaoGenerator = new DaoGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): DaoGenerator = new DaoGenerator(modelLoader)
}

class DaoGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    generateDaoContent(xml)
      .foreach(x => save(x._1, x._2, daoSrcDir))
  }

  def generateDaoContent(xml: Node): Seq[(String, String)] = {
    xml.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) generateDaoForAggregate(toAggregate(x, xml))
        else {
          val valueObject = toValueObject(x, aggregatesTo, xml)
          generateDaoForValueObject(valueObject)
        }
      })
  }

  def generateDaoForAggregate(aggregate: Aggregate): (String, String) = {
    import aggregate._
    val className = s"${cToPascal(aggregate.name)}Dao"
    val fileName = s"${className}.scala"
    val calls = (
      defCrud(name) ++
        defByForeignKeys(name, fields, foreignKeys)
      )
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    val content =
      s"""
         |package ${daoSrcPackage}
         |
         |import ${messageSrcPackage}._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.github.apuex.springbootsolution.runtime.SymbolConverters._
         |import com.google.protobuf.timestamp.Timestamp
         |import java.sql.Connection
         |
         |trait ${className} {
         |  ${indent(calls, 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def generateDaoForValueObject(valueObject: ValueObject): (String, String) = {
    import valueObject._
    val className = s"${cToPascal(valueObject.name)}Dao"
    val fileName = s"${className}.scala"
    val calls = (
      defCrud(name) ++
        defByForeignKeys(name, fields, foreignKeys)
      )
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
    val content =
      s"""
         |package ${daoSrcPackage}
         |
         |import ${messageSrcPackage}._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.github.apuex.springbootsolution.runtime.SymbolConverters._
         |import com.google.protobuf.timestamp.Timestamp
         |import java.sql.Connection
         |
         |trait ${className} {
         |  ${indent(calls, 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def defCrud(name: String): Seq[String] = Seq(
    s"""
       |def create${cToPascal(name)}(cmc: Create${cToPascal(name)}Cmd)(implicit conn: Connection): Int
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}(cmd: Retrieve${cToPascal(name)}Cmd)(implicit conn: Connection): ${cToPascal(name)}Vo
     """.stripMargin.trim,
    s"""
       |def update${cToPascal(name)}(cmd: Update${cToPascal(name)}Cmd)(implicit conn: Connection): Int
     """.stripMargin.trim,
    s"""
       |def delete${cToPascal(name)}(cmd: Delete${cToPascal(name)}Cmd)(implicit conn: Connection): Int
     """.stripMargin.trim,
    s"""
       |def query${cToPascal(name)}(cmd: QueryCommand)(implicit conn: Connection): Seq[${cToPascal(name)}Vo]
     """.stripMargin.trim
  )

  def defByForeignKeys(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)
          .toSet

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        defByForeignKey(name, fkFields)
      })
  }

  def defByForeignKey(name: String, keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def selectBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): Seq[${cToPascal(name)}Vo]
       |def deleteBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): Int
     """.stripMargin.trim
  }
}