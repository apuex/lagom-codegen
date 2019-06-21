package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils.indent
import com.github.apuex.springbootsolution.runtime.TypeConverters._

import scala.xml.Node


object DaoMysqlImplGenerator {
  def apply(fileName: String): DaoMysqlImplGenerator = new DaoMysqlImplGenerator(ModelLoader(fileName))

  def apply(modelLoader: ModelLoader): DaoMysqlImplGenerator = new DaoMysqlImplGenerator(modelLoader)
}

class DaoMysqlImplGenerator(modelLoader: ModelLoader) {

  import modelLoader._

  def generate(): Unit = {
    generateDaoContent(xml, daoMysqlSrcPackage)
      .foreach(x => save(x._1, x._2, daoMysqlSrcDir))
  }

  def generateDaoContent(xml: Node, messageSrcPackage: String): Seq[(String, String)] = {
    xml.child.filter(_.label == "entity")
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false
        if (!enum && "" == aggregatesTo) generateDaoForAggregate(toAggregate(x, xml), messageSrcPackage)
        else {
          val valueObject = toValueObject(x, aggregatesTo, xml)
          generateDaoForValueObject(valueObject, messageSrcPackage)
        }
      })
  }

  def generateDaoForAggregate(aggregate: Aggregate, messageSrcPackage: String): (String, String) = {
    val className = s"${cToPascal(aggregate.name)}Dao"
    val fileName = s"${className}.scala"
    val content =
      s"""
         |class ${className} {
         |
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def generateDaoForValueObject(valueObject: ValueObject, messageSrcPackage: String): (String, String) = {
    val className = s"${cToPascal(valueObject.name)}Dao"
    val fileName = s"${className}.scala"
    val selectByForeignKey = valueObject.foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)
        val by = fieldNames
          .map(cToPascal(_))
          .reduceOption((l, r) => s"${l}${r}")
          .getOrElse("")

        val fkFields = valueObject.fields
          .filter(x => fieldNames.contains(x.name))
        s"""
           |def selectBy${by}(${defMethodParams(fkFields)}): ${cToPascal(valueObject.name)}Vo
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    val content =
      s"""
         |class ${className}Impl extends ${className} {
         |  ${indent(selectByForeignKey, 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def generateCrud(name: String, fields: Seq[Field], pkFields: Seq[Field], fkFields: Seq[Field]): String = {
    s"""
       |def create(c: Create${cToPascal(name)}Cmd): Int = {
       |
       |}
       |
       |def retrieve(c: Retrieve${cToPascal(name)}Cmd): ${cToPascal(name)}Vo = {
       |
       |}
       |
       |def update(c: Update${cToPascal(name)}Cmd): Int = {
       |
       |}
       |
       |def delete(c: Delete${cToPascal(name)}Cmd): Int = {
       |
       |}
       |
       |def query${cToPascal(name)}(c: QueryCommand): ${cToPascal(name)}ListVo = {
       |
       |}
     """.stripMargin.trim
  }

  def selectSql(name: String, fields: Seq[Field]): String = {
    s"""
       |
     """.stripMargin.trim
  }

  def whereClause(): String = {
    "val whereClause = WhereClauseWithNamedParams(fieldConverter)"
  }

  def fieldConverter(fields: Seq[Field]): String = {
    val cases = fields
      .map(x =>
        s"""
           |case "${cToCamel(x.name)}" => "${x.name}"
         """.stripMargin.trim
      )
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    s"""
       |val fieldConverter: SymbolConverter = {
       |  ${indent(cases, 2)}
       |  case x: String => camelToC(x)
       |}
     """.stripMargin.trim
  }

  def paramParser(fields: Seq[Field]): String = {
    val cases = fields
      .map(x => {
        val javaType = cToPascal(toJavaType(x._type))
        if ("String" == javaType)
          s"""
             |case "${cToCamel(x.name)}" => paramName -> paramValue
         """.stripMargin.trim
        else
          s"""
             |case "${cToCamel(x.name)}" => paramName -> ${javaType}Parser.parse(paramValue)
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    s"""
       |def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
       |  ${indent(cases, 2)}
       |}
     """.stripMargin.trim
  }

  def selectComposite(field: Field, keyFields: Seq[Field]): String = {
    if ("array" == field._type) {
      s"""
         |${cToCamel(field.valueType)}Dao.${callSelectByFk(keyFields)}
      """.stripMargin.trim
    } else if ("map" == field._type) {
      s"""
         |${cToCamel(field.entity)}Dao.${callSelectByFk(keyFields)}
         |  .map(x => (x.${cToCamel(field.keyField)} -> x.${cToCamel(field.valueField)}}
         |  .toMap
      """.stripMargin.trim
    } else {
      throw new RuntimeException(
        s"""
           |"${field._type}" is not accepted.
           |Accepted types are `array` or `map`.
         """.stripMargin.trim)
    }
  }

  def rowParser(name: String, fields: Seq[Field], keyFields: Seq[Field]): String = {
    val gets = fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x =>
      s"""
         |get[${cToPascal(toJdbcType(x._type))}]("${x.name}")
         """.stripMargin.trim
    )
      .reduceOption((l, r) => s"${l} ~ \n${r}")
      .getOrElse("")

    val pattern = fields
      .filter(x => isJdbcType(x._type))
      .map(x => cToCamel(x.name))
      .reduceOption((l, r) => s"${l} ~ ${r}")
      .getOrElse("")

    val constructorParam = fields
      .map(x =>
        if (isJdbcType(x._type)) cToCamel(x.name)
        else if (isEnum(x._type)) s"${cToPascal(x.name)}.fromValue(${cToCamel(x.name)})"
        else { // array, map or value object type,
          s"""
             |${selectComposite(x, keyFields)}
           """.stripMargin
        }
      )
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    s"""
       |private def rowParser(implicit c: Connection): RowParser[${cToPascal(name)}Vo] = {
       |  ${indent(gets, 2)} map {
       |    case ${pattern} =>
       |      ${cToPascal(name)}Vo(
       |        ${indent(constructorParam, 8)}
       |      )
       |  }
       |}
     """.stripMargin
  }

  def namedParams(): String = {
    s"""
       |private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
       |  whereClause.toNamedParams(q.getPredicate, toImmutableScalaMap(q.getParamsMap))
       |    .map(x => parseParam(x._1, x._2, x._3))
       |    .asInstanceOf[Seq[NamedParameter]]
       |}
     """.stripMargin
  }


  def defSelectByFk(name: String, keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def selectBy${by}(${defMethodParams(keyFields)}): ${cToPascal(name)}ListVo")
     """.stripMargin.trim
  }

  def callSelectByFk(keyFields: Seq[Field], alias: String = ""): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |selectBy${by}(${substituteMethodParams(keyFields, alias)})
     """.stripMargin.trim
  }
}