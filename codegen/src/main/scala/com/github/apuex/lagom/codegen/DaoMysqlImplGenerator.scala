package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import com.github.apuex.springbootsolution.runtime.SymbolConverters._
import com.github.apuex.springbootsolution.runtime.TextUtils._
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
    val calls = (
      defCrud(name, fields, primaryKey.fields, foreignKeys) ++
        defSelectByFks(name, fields, foreignKeys) ++
        defMessages(aggregate.messages) ++
        defEmbeddedAggregateMessages(aggregate.aggregates)
      )
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

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
         |class ${className}(${defDaoDependencies(fields)}) extends ${traitName} {
         |  ${indent(calls, 2)}
         |
         |  ${indentWithLeftMargin(selectSql(name, fields), 2)}
         |
         |  ${indent(fieldConverter(fields), 2)}
         |
         |  ${indent(whereClause(), 2)}
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
    val calls = (
      defCrud(name, fields, primaryKey.fields, foreignKeys) ++
        defSelectByFks(name, fields, foreignKeys)
      )
      .reduceOption((l, r) => s"${l}\n${r}")
      .getOrElse("")

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
         |class ${className}(${defDaoDependencies(fields)}) extends ${traitName} {
         |  ${indent(calls, 2)}
         |
         |  ${indentWithLeftMargin(selectSql(name, fields), 2)}
         |
         |  ${indent(fieldConverter(fields), 2)}
         |
         |  ${indent(whereClause(), 2)}
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

  def columnNames(fields: Seq[Field], alias: String = ""): String = {
    val t = if ("" == alias) "" else s"${alias}."
    fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => s"${t}${x.name}")
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")
  }

  def selectSql(name: String, fields: Seq[Field]): String = {
    val sql =
      s"""
         |SELECT
         |  ${indent(columnNames(fields, "t"), 2)}
         |FROM ${modelDbSchema}.${name} t
       """.stripMargin.trim
    s"""
       |private val sql =
       |  ${indentWithLeftMargin(blockQuote(sql, 2), 2)}
       |""".stripMargin.trim
  }

  def whereClause(): String = {
    "private val whereClause = WhereClauseWithNamedParams(fieldConverter)"
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
       |private val fieldConverter: SymbolConverter = {
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

  def defDaoDependencies(fields: Seq[Field]): String = {
    fields
      .filter(x => ("array" == x._type && isEntity(x.valueType)) || ("map" == x._type && isEntity(x.entity)))
      .map(x => if("array" == x._type) x.valueType else x.entity)
      .map(x => {
        s"""
           |${cToCamel(x)}Dao: ${cToPascal(x)}Dao
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"$l, $r")
      .getOrElse("")
  }

  def defCrud(name: String, fields: Seq[Field], pkFields: Seq[Field], fks: Seq[ForeignKey]): Seq[String] = Seq(
    s"""
       |def create${cToPascal(name)}(cmd: Create${cToPascal(name)}Cmd)(implicit conn: Connection): Int = {
       |}
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}(cmd: Retrieve${cToPascal(name)}Cmd)(implicit conn: Connection): ${cToPascal(name)}Vo = {
       |}
     """.stripMargin.trim,
    s"""
       |def update${cToPascal(name)}(cmd: Update${cToPascal(name)}Cmd)(implicit conn: Connection): Int = {
       |}
     """.stripMargin.trim,
    s"""
       |def delete${cToPascal(name)}(cmd: Delete${cToPascal(name)}Cmd)(implicit conn: Connection): Int = {
       |}
     """.stripMargin.trim,
    s"""
       |def query${cToPascal(name)}(cmd: QueryCommand)(implicit conn: Connection): Seq[${cToPascal(name)}Vo] = {
       |}
     """.stripMargin.trim,
    s"""
       |def retrieve${cToPascal(name)}ByRowid(cmd: RetrieveByRowidCmd)(implicit conn: Connection): Seq[${cToPascal(name)}Vo] = {
       |}
     """.stripMargin.trim
  )

  def defMessage(message: Message): String = {
    val returnType = if ("" == message.returnType) "Int"
    else {
      val baseName = message.returnType.replace("*", "")
      val multiple = message.returnType.endsWith("*")
      if (multiple) {
        if (isAggregateEntity(baseName)) s"Seq[${cToPascal(baseName)}Vo]" else s"${cToPascal(baseName)}Vo"
      } else {
        cToPascal(toJavaType(baseName))
      }
    }
    s"""
       |def ${cToCamel(message.name)}(cmd: ${cToPascal(message.name)}Cmd)(implicit conn: Connection): ${returnType} = {
       |}
     """.stripMargin.trim
  }

  def defMessages(messages: Seq[Message]): Seq[String] = {
    messages.map(defMessage(_))
  }

  def defEmbeddedAggregateMessage(aggregate: Aggregate): String = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))
    if (nonKeyFieldCount > 1)
      s"""
         |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo = {
         |}
         |
         |def update${cToPascal(aggregate.name)}(cmd: Update${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): Int = {
         |}
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type || "map" == field._type)
        s"""
           |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo = {
           |}
           |
           |def add${cToPascal(aggregate.name)}(cmd: Add${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): Int = {
           |}
           |
           |def remove${cToPascal(aggregate.name)}(cmd: Remove${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): Int = {
           |}
     """.stripMargin.trim
      else
        s"""
           |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo = {
           |}
           |
           |def change${cToPascal(aggregate.name)}(cmd: Change${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): Int = {
           |}
     """.stripMargin.trim
    } else { // this cannot be happen.
      s"""
         |
     """.stripMargin.trim
    }
  }

  def defEmbeddedAggregateMessages(aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates.map(defEmbeddedAggregateMessage(_))
  }

  def defSelectByFks(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
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
           |def selectBy${by}(${defMethodParams(fkFields)})(implicit conn: Connection): ${cToPascal(name)}Vo = {
           |}
         """.stripMargin.trim
      })
  }

  def defSelectByFk(name: String, keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def selectBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): ${cToPascal(name)}ListVo") = {
       |}
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