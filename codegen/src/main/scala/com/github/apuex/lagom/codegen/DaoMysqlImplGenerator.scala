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
    generateDaoContent(xml)
      .foreach(x => save(x._1, x._2, daoMysqlSrcDir))
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
    val traitName = s"${cToPascal(aggregate.name)}Dao"
    val className = s"${traitName}Impl"
    val fileName = s"${className}.scala"
    val content =
      s"""
         |package ${daoMysqlSrcPackage}
         |
         |
         |import java.sql.Connection
         |import java.util.Date
         |
         |import anorm.SqlParser._
         |import anorm._
         |import play._
         |import anorm.ParameterValue._
         |import ${messageSrcPackage}._
         |import ${daoSrcPackage}._
         |import com.github.apuex.springbootsolution.runtime.DateFormat.toScalapbTimestamp
         |import com.github.apuex.springbootsolution.runtime.Parser._
         |import com.github.apuex.springbootsolution.runtime.SymbolConverters._
         |import com.github.apuex.springbootsolution.runtime._
         |
         |class ${className} extends ${traitName} {
         |  ${indent(defSelectByFks(name, fields, foreignKeys), 2)}
         |
         |  ${indent(selectSql(name, fields), 2)}
         |  ${indent(whereClause(), 2)}
         |  ${indent(fieldConverter(fields), 2)}
         |
         |  ${indent(paramParser(fields), 2)}
         |
         |  ${indent(rowParser(name, fields, primaryKey.fields), 2)}
         |
         |  ${indent(namedParams(), 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def generateDaoForValueObject(valueObject: ValueObject): (String, String) = {
    import valueObject._
    val traitName = s"${cToPascal(name)}Dao"
    val className = s"${traitName}Impl"
    val fileName = s"${className}.scala"

    val content =
      s"""
         |package ${daoMysqlSrcPackage}
         |
         |import java.sql.Connection
         |import java.util.Date
         |
         |import anorm.SqlParser._
         |import anorm._
         |import play._
         |import anorm.ParameterValue._
         |import ${messageSrcPackage}._
         |import ${daoSrcPackage}._
         |import com.github.apuex.springbootsolution.runtime.DateFormat.toScalapbTimestamp
         |import com.github.apuex.springbootsolution.runtime.Parser._
         |import com.github.apuex.springbootsolution.runtime.SymbolConverters._
         |import com.github.apuex.springbootsolution.runtime._
         |
         |class ${className} extends ${traitName} {
         |  ${indent(defSelectByFks(name, fields, foreignKeys), 2)}
         |
         |  ${indent(selectSql(name, fields), 2)}
         |  ${indent(whereClause(), 2)}
         |  ${indent(fieldConverter(fields), 2)}
         |
         |  ${indent(paramParser(fields), 2)}
         |
         |  ${indent(rowParser(name, fields, primaryKey.fields), 2)}
         |
         |  ${indent(namedParams(), 2)}
         |}
     """.stripMargin.trim

    (fileName, content)
  }

  def generateCrud(name: String, fields: Seq[Field], pkFields: Seq[Field], fkFields: Seq[Field]): String = {
    s"""
       |def create(c: Create${cToPascal(name)}Cmd)(implicit conn: Connection): Int = {
       |
       |}
       |
       |def retrieve(c: Retrieve${cToPascal(name)}Cmd)(implicit conn: Connection): ${cToPascal(name)}Vo = {
       |
       |}
       |
       |def update(c: Update${cToPascal(name)}Cmd)(implicit conn: Connection): Int = {
       |
       |}
       |
       |def delete(c: Delete${cToPascal(name)}Cmd)(implicit conn: Connection): Int = {
       |
       |}
       |
       |def query${cToPascal(name)}(c: QueryCommand)(implicit conn: Connection): ${cToPascal(name)}ListVo = {
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
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
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
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => {
        val javaType = cToPascal(toJavaType(x._type))
        if (isEnum(x._type))
          s"""
             |case "${cToCamel(x.name)}" => paramName -> EnumParser(${cToPascal(x._type)}).parse(paramValue).value
         """.stripMargin.trim
        else if ("String" == javaType)
          s"""
             |case "${cToCamel(x.name)}" => paramName -> paramValue
         """.stripMargin.trim
        else if ("Timestamp" == javaType)
          s"""
             |case "${cToCamel(x.name)}" => paramName -> DateParser.parse(paramValue)
         """.stripMargin.trim
        else
          s"""
             |case "${cToCamel(x.name)}" => paramName -> ${javaType}Parser.parse(paramValue)
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    val arrayCases = fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => {
        val javaType = cToPascal(toJavaType(x._type))
        if (isEnum(x._type))
          s"""
             |case "${cToCamel(x.name)}" => paramName -> paramValue.map(EnumParser(${cToPascal(x._type)}).parse(_).value)
         """.stripMargin.trim
        else if ("String" == javaType)
          s"""
             |case "${cToCamel(x.name)}" => paramName -> paramValue
         """.stripMargin.trim
        else if ("Timestamp" == javaType)
          s"""
             |case "${cToCamel(x.name)}" => paramName -> paramValue.map(DateParser.parse(_))
         """.stripMargin.trim
        else
          s"""
             |case "${cToCamel(x.name)}" => paramName -> paramValue.map(${javaType}Parser.parse(_))
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

    s"""
       |private def parseParam(fieldName: String, paramName:String, paramValue: scala.Any): NamedParameter = paramValue match {
       |  case x: String => parseParam(fieldName, paramName, x)
       |  case x: Array[String] => parseParam(fieldName, paramName, x.toSeq)
       |  case x: scala.Any => throw new RuntimeException(x.toString)
       |}
       |
       |private def parseParam(fieldName: String, paramName:String, paramValue: String): NamedParameter = fieldName match {
       |  ${indent(cases, 2)}
       |}
       |
       |private def parseParam(fieldName: String, paramName:String, paramValue: Seq[String]): NamedParameter = fieldName match {
       |  ${indent(arrayCases, 2)}
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
      if ("timestamp" == x._type)
        s"""
           |get[Date]("${x.name}")
         """.stripMargin.trim
      else
        s"""
           |get[${cToPascal(toJdbcType(x._type))}]("${x.name}")
         """.stripMargin.trim
    )
      .reduceOption((l, r) => s"${l} ~ \n${r}")
      .getOrElse("")

    val pattern = fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => cToCamel(x.name))
      .reduceOption((l, r) => s"${l} ~ ${r}")
      .getOrElse("")

    val constructorParam = fields
      .map(x =>
        if (isJdbcType(x._type)) {
          if ("timestamp" == x._type)
            s"Some(toScalapbTimestamp(${cToCamel(x.name)}))"
          else
            cToCamel(x.name)
        }
        else if (isEnum(x._type)) s"${cToPascal(x._type)}.fromValue(${cToCamel(x.name)})"
        else { // array, map or value object type,
          s"""
             |${selectComposite(x, keyFields)}
           """.stripMargin.trim
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
     """.stripMargin.trim
  }

  def namedParams(): String = {
    s"""
       |private def namedParams(q: QueryCommand): Seq[NamedParameter] = {
       |  whereClause.toNamedParams(q.getPredicate, q.params)
       |    .map(x => parseParam(x._1, x._2, x._3))
       |    .asInstanceOf[Seq[NamedParameter]]
       |}
     """.stripMargin.trim
  }


  def defSelectByFks(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): String = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)
        val by = fieldNames
          .map(cToPascal(_))
          .reduceOption((l, r) => s"${l}${r}")
          .getOrElse("")

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        s"""
           |def selectBy${by}(${defMethodParams(fkFields)}): ${cToPascal(name)}Vo
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")
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