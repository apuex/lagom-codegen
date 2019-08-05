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
    save("DaoModule.scala", wireModule(xml), daoMysqlSrcDir)
  }

  def wireModule(root: Node): String = {
    s"""
       |/*****************************************************
       | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
       | *****************************************************/
       |package ${daoMysqlSrcPackage}
       |
       |import com.softwaremill.macwire._
       |
       |@Module
       |class DaoModule {
       |  ${indent(wireDaos(root), 2)}
       |}
     """.stripMargin.trim
  }

  def wireDaos(root: Node): String = {
    root.child.filter(_.label == "entity")
      .map(_.\@("name"))
      .map(x => {
        s"""
           |lazy val ${cToCamel(x)}Dao = wire[${cToPascal(x)}DaoImpl]
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
  }

  def generateDaoContent(xml: Node): Seq[(String, String)] = {
    xml.child.filter(_.label == "entity")
      .filter(x => ("true" != x.\@("transient")))
      .map(x => {
        val aggregatesTo = x.\@("aggregatesTo")
        val enum = if ("true" == x.\@("enum")) true else false

        if (!enum && "" == aggregatesTo) {
          generateDaoForAggregate(toAggregate(x, xml))
        } else {
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
    val persistFields = fields.filter(!_.transient)

    val calls = (
      defCrud(name, fields, primaryKey, foreignKeys) ++
        defSelectByFks(name, persistFields, foreignKeys) ++
        defDeleteByFks(name, persistFields, foreignKeys) ++
        defMessages(name, persistFields, primaryKey, aggregate.messages) ++
        defEmbeddedAggregateMessages(name, aggregate.aggregates)
      )
      .filter("" != _)
      .map(indentWithLeftMargin(_, 2))
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")

    val content =
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |package ${daoMysqlSrcPackage}
         |
         |import java.io.InputStream
         |import java.sql.Connection
         |import java.util.Date
         |
         |import anorm.ParameterValue._
         |import anorm.SqlParser._
         |import anorm._
         |import com.datastax.driver.core.utils.UUIDs
         |import play._
         |import com.github.apuex.springbootsolution.runtime.DateFormat.{scalapbToDate, toScalapbTimestamp}
         |import com.github.apuex.springbootsolution.runtime.EnumConvert._
         |import com.github.apuex.springbootsolution.runtime.Parser._
         |import com.github.apuex.springbootsolution.runtime.SymbolConverters._
         |import com.github.apuex.springbootsolution.runtime.TextUtils._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.google.protobuf.ByteString
         |import ${messageSrcPackage}._
         |import ${daoSrcPackage}._
         |
         |class ${className}(${defDaoDependencies(fields)}) extends ${traitName} {
         |  val log = Logger.of(getClass)
         |
         |  ${indent(calls, 2)}
         |
         |  ${indentWithLeftMargin(defSelectSql(name, persistFields), 2)}
         |
         |  ${indent(fieldConverter(persistFields), 2)}
         |
         |  ${indent(whereClause(), 2)}
         |
         |  ${indent(paramParser(persistFields), 2)}
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
      defCrud(name, fields, primaryKey, foreignKeys) ++
        defSelectByFks(name, fields, foreignKeys) ++
        defDeleteByFks(name, fields, foreignKeys)
      )
      .filter("" != _)
      .map(indentWithLeftMargin(_, 2))
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")

    val content =
      s"""
         |/*****************************************************
         | ** This file is 100% ***GENERATED***, DO NOT EDIT! **
         | *****************************************************/
         |package ${daoMysqlSrcPackage}
         |
         |import java.io.InputStream
         |import java.sql.Connection
         |import java.util.Date
         |
         |import anorm.ParameterValue._
         |import anorm.SqlParser._
         |import anorm._
         |import play._
         |import com.github.apuex.springbootsolution.runtime.DateFormat.{scalapbToDate, toScalapbTimestamp}
         |import com.github.apuex.springbootsolution.runtime.EnumConvert._
         |import com.github.apuex.springbootsolution.runtime.Parser._
         |import com.github.apuex.springbootsolution.runtime.SymbolConverters._
         |import com.github.apuex.springbootsolution.runtime.TextUtils._
         |import com.github.apuex.springbootsolution.runtime._
         |import com.google.protobuf.ByteString
         |import ${messageSrcPackage}._
         |import ${daoSrcPackage}._
         |
         |class ${className}(${defDaoDependencies(fields)}) extends ${traitName} {
         |  val log = Logger.of(getClass)
         |
         |  ${indent(calls, 2)}
         |
         |  ${indentWithLeftMargin(defSelectSql(name, fields), 2)}
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
      .filter(!_.transient)
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .filter(x => "text" != x._type && "blob" != x._type && "clob" != x._type)
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
      .filter(!_.transient)
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .filter(x => "text" != x._type && "blob" != x._type && "clob" != x._type)
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
    val persistFields = fields.filter(!_.transient)
    val keyFieldNames = keyFields.map(_.name).toSet
    val gets = persistFields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => {
      val required = x.required || keyFieldNames.contains(x.name)
      if ("timestamp" == x._type)
        s"""
           |get[${wrapOption("Date", required)}]("${x.name}")
         """.stripMargin.trim
      else if ("blob" == x._type)
        s"""
           |get[${wrapOption("InputStream", required)}]("${x.name}")
         """.stripMargin.trim
      else
        s"""
           |get[${wrapOption(cToPascal(toJdbcType(x._type)), required)}]("${x.name}")
         """.stripMargin.trim
    })
      .reduceOption((l, r) => s"${l} ~ \n${r}")
      .getOrElse("")

    val pattern = persistFields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => cToCamel(x.name))
      .reduceOption((l, r) => s"${l} ~ ${r}")
      .getOrElse("")

    val constructorParam = fields
      .map(x => {
        val required = x.required || keyFieldNames.contains(x.name)
        if (x.transient) {
          if (isJdbcType(x._type)) {
            defaultValue(x._type)
          }
          else if (isEnum(x._type)) s"${cToPascal(x._type)}.fromValue(0)"
          else { // array, map or value object type,
            if ("array" == x._type)
              s"""
                 |Seq()
           """.stripMargin.trim
            else if ("array" == x._type)
              s"""
                 |Map()
           """.stripMargin.trim
            else
              s"""
                 |
           """.stripMargin.trim
          }
        } else {
          if (isJdbcType(x._type)) {
            wrapOptionValue(x._type, s"${cToCamel(x.name)}", required)
          }
          else if (isEnum(x._type)) if (required) s"${cToPascal(x._type)}.fromValue(${cToCamel(x.name)})" else s"${cToCamel(x.name)}.map(x => ${cToPascal(x._type)}.fromValue(x)).getOrElse(${cToPascal(x._type)}.fromValue(0))"
          else { // array, map or value object type,
            s"""
               |${selectComposite(x, keyFields)}
           """.stripMargin.trim
          }
        }
      })
      .reduceOption((l, r) => s"${l},\n${r}")
      .getOrElse("")

    s"""
       |private def ${cToCamel(name)}Parser(implicit c: Connection): RowParser[${cToPascal(name)}Vo] = {
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
      .map(x => if ("array" == x._type) x.valueType else x.entity)
      .map(x => {
        s"""
           |${cToCamel(x)}Dao: ${cToPascal(x)}Dao
         """.stripMargin.trim
      })
      .reduceOption((l, r) => s"$l, $r")
      .getOrElse("")
  }

  def defQuery(name: String): String = {
    val logSql =
      s"""[SQL statement] =>
         |  $${indent(sqlStr, 2)}
         |  [params for substitution] =>
         |    {}
       """.stripMargin.trim
    val select =
      s"""$${select${cToPascal(name)}Sql}
         |$${whereClause.toWhereClause(cmd, 4)}
         |ORDER BY $${indent(if(!cmd.orderBy.isEmpty) whereClause.orderBy(cmd.orderBy, "t") else "", 4)}
       """.stripMargin.trim

    s"""
       |def query${cToPascal(name)}(cmd: QueryCommand)(implicit conn: Connection): Seq[${cToPascal(name)}Vo] = {
       |  val sqlStr = ${indentWithLeftMargin(blockQuote(select, 2), 2)}
       |  val stmt = SQL(sqlStr)
       |  val params = namedParams(cmd)
       |
       |  if(log.isDebugEnabled) log.debug(
       |    ${indentWithLeftMargin(blockQuote(logSql, 0), 2)},
       |    params
       |  )
       |
       |  if (params.isEmpty) {
       |    stmt.as(${cToCamel(name)}Parser.*)
       |  } else {
       |    stmt.on(
       |      params: _*
       |    ).as(${cToCamel(name)}Parser.*)
       |  }
       |}
     """.stripMargin.trim
  }

  def defSelectSql(name: String, fields: Seq[Field]): String = {
    val sql =
      s"""
         |SELECT
         |  ${indent(columnNames(fields, "t"), 2)}
         |FROM ${modelDbSchema}.${name} t
       """.stripMargin.trim
    s"""
       |private val select${cToPascal(name)}Sql =
       |  ${indentWithLeftMargin(blockQuote(sql, 2), 2)}
       |""".stripMargin.trim
  }

  def defSqlFields(name: String, fields: Seq[Field]): String = {
    fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => s"${name}.${x.name}")
      .reduceOption((l, r) => s"$l,\n$r")
      .getOrElse("")

  }

  def defInsetValues(fields: Seq[Field]): String = {
    fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => s"{${cToCamel(x.name)}}")
      .reduceOption((l, r) => s"$l,\n$r")
      .getOrElse("")

  }

  def defFieldSubstitution(name: String, fields: Seq[Field], alias: String): String = {
    val t = if ("" == alias) "" else s"${alias}."
    fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x =>
      if (isEnum(x._type))
        s"""
           |"${cToCamel(x.name)}" -> toValue(${t}${cToCamel(x.name)})
           |""".stripMargin.trim
      else if ("timestamp" == x._type)
        s"""
           |"${cToCamel(x.name)}" -> scalapbToDate(${t}${cToCamel(x.name)})
           |""".stripMargin.trim
      else if ("blob" == x._type)
        s"""
           |"${cToCamel(x.name)}" -> ${t}${cToCamel(x.name)}.toByteArray
           |""".stripMargin.trim
      else
        s"""
           |"${cToCamel(x.name)}" -> ${t}${cToCamel(x.name)}
           |""".stripMargin.trim)
      .reduceOption((l, r) => s"$l,\n$r")
      .getOrElse("")
  }

  def insertSql(name: String, fields: Seq[Field]): String = {
    s"""
       |INSERT INTO ${modelDbSchema}.${name}(
       |  ${indent(defSqlFields(name, fields), 2)}
       |) VALUES (
       |  ${indent(defInsetValues(fields), 2)}
       |)
     """.stripMargin.trim
  }

  def defSetFieldValues(name: String, fields: Seq[Field]): String = {
    fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => s"${name}.${x.name} = {${cToCamel(x.name)}}")
      .reduceOption((l, r) => s"$l,\n$r")
      .getOrElse("")
  }

  def defFilterByFieldValues(name: String, fields: Seq[Field]): String = {
    fields
      .filter(x => isJdbcType(x._type) || isEnum(x._type)) // enums treated as ints
      .map(x => s"${x.name} = {${cToCamel(x.name)}}")
      .reduceOption((l, r) => s"$l\nAND $r")
      .getOrElse("")
  }

  def updateSql(name: String, fields: Seq[Field], pkFields: Seq[Field]): String = {
    val pkFieldNames = pkFields.map(_.name).toSet
    val nonKeyFields = fields.filter(x => !pkFieldNames.contains(x.name))
    s"""
       |UPDATE ${modelDbSchema}.${name}
       |SET
       |  ${indent(defSetFieldValues(name, nonKeyFields), 2)}
       |WHERE ${indent(defFilterByFieldValues(name, pkFields), 2)}
     """.stripMargin.trim
  }

  def deleteSql(name: String, pkFields: Seq[Field]): String = {
    s"""
       |DELETE
       |FROM ${modelDbSchema}.${name}
       |WHERE ${indent(defFilterByFieldValues(name, pkFields), 2)}
     """.stripMargin.trim
  }

  def retrieveSql(name: String, fields: Seq[Field], pkFields: Seq[Field]): String = {
    s"""
       |SELECT
       |  ${indent(defSqlFields(name, fields), 2)}
       |FROM ${modelDbSchema}.${name}
       |WHERE ${indent(defFilterByFieldValues(name, pkFields), 2)}
     """.stripMargin.trim
  }

  def filterGenerated(fields: Seq[Field], primaryKey: PrimaryKey): Seq[Field] = {
    val generated = if (primaryKey.generated) primaryKey.fields.map(_.name).toSet else Set[String]()
    fields.filter(x => !generated.contains(x.name))
  }

  def offsetParser(offsetType: String): String = {
    s"""
       |private def offsetParser(implicit c: Connection): RowParser[${offsetType}] = {
       |  get[${offsetType}]("offset") map {
       |    case offset => offset
       |  }
       |}
     """.stripMargin.trim
  }

  def defCrud(name: String, fields: Seq[Field], primaryKey: PrimaryKey, fks: Seq[ForeignKey]): Seq[String] = {
    val keyFieldNames = primaryKey.fields.map(_.name).toSet
    val persistFields = fields.filter(!_.transient)
    val nonKeyPersistFields = persistFields.filter(x => !keyFieldNames.contains(x.name))
    if (persistFields.isEmpty) {
      Seq()
    } else {
      val offset = if (journalTable == name) {
        fields.filter("offset" == _.name)
          .map(x => {
            val offsetType = if("long" == x._type) cToPascal(x._type) else x._type.toUpperCase
            val defaultValue = if("long" == x._type)
              "0"
            else
              s"""
                 |UUIDs.startOf(0)
              """".stripMargin.trim
            s"""
               |${offsetParser(offsetType)}
               |
               |def selectCurrentOffset()(implicit conn: Connection): ${offsetType} = {
               |  try {
               |    val max = SQL("SELECT max(${name}.offset) as offset FROM ${modelDbSchema}.${name}").as(offsetParser.*)
               |    if (max.isEmpty) 0 else max.head
               |  } catch {
               |    case _: Throwable => ${defaultValue}
               |  }
               |}
     """.stripMargin.trim
          })
          .reduceOption((l, r) => s"${l}\n${r}")
      } else None

      Seq(
        offset.getOrElse(""),
        s"""
           |def create${cToPascal(name)}(evt: Create${cToPascal(name)}Event)(implicit conn: Connection): Int = {
           |  val rowsAffected = SQL(${indentWithLeftMargin(blockQuote(updateSql(name, persistFields, primaryKey.fields), 2), 2)})
           |  .on(
           |    ${indent(defFieldSubstitution(name, persistFields, "evt"), 4)}
           |  ).executeUpdate()
           |
           |  if(rowsAffected == 0)
           |    SQL(${indentWithLeftMargin(blockQuote(insertSql(name, filterGenerated(persistFields, primaryKey)), 2), 4)})
           |    .on(
           |      ${indent(defFieldSubstitution(name, persistFields, "evt"), 6)}
           |    ).executeUpdate()
           |  else rowsAffected
           |}
     """.stripMargin.trim,
        s"""
           |def retrieve${cToPascal(name)}(cmd: Retrieve${cToPascal(name)}Cmd)(implicit conn: Connection): ${cToPascal(name)}Vo = {
           |  SQL(${indentWithLeftMargin(blockQuote(retrieveSql(name, persistFields, primaryKey.fields), 2), 2)})
           |  .on(
           |    ${indent(defFieldSubstitution(name, primaryKey.fields, "cmd"), 4)}
           |  ).as(${cToCamel(name)}Parser.single)
           |}
     """.stripMargin.trim,
        if (nonKeyPersistFields.isEmpty)
          ""
        else
          s"""
             |def update${cToPascal(name)}(evt: Update${cToPascal(name)}Event)(implicit conn: Connection): Int = {
             |  SQL(${indentWithLeftMargin(blockQuote(updateSql(name, persistFields, primaryKey.fields), 2), 2)})
             |  .on(
             |    ${indent(defFieldSubstitution(name, persistFields, "evt"), 4)}
             |  ).executeUpdate()
             |}
     """.stripMargin.trim,
        s"""
           |def delete${cToPascal(name)}(evt: Delete${cToPascal(name)}Event)(implicit conn: Connection): Int = {
           |  SQL(${indentWithLeftMargin(blockQuote(deleteSql(name, primaryKey.fields), 2), 2)})
           |  .on(
           |    ${indent(defFieldSubstitution(name, primaryKey.fields, "evt"), 4)}
           |  ).executeUpdate()
           |}
     """.stripMargin.trim,
        defQuery(name),
        s"""
           |def retrieve${cToPascal(name)}ByRowid(rowid: String)(implicit conn: Connection): ${cToPascal(name)}Vo = {
           |  SQL(${indentWithLeftMargin(blockQuote(retrieveSql(name, persistFields, Seq(rowidField)), 2), 2)})
           |  .on(
           |    ${indent(defFieldSubstitution(name, Seq(rowidField), ""), 4)}
           |  ).as(${cToCamel(name)}Parser.single)
           |}
     """.stripMargin.trim
      )
    }
  }

  def defMessage(root: String, rootFields: Seq[Field], primaryKey: PrimaryKey, message: Message): String = {
    val derived = rootFields.map(_.name).toSet
    val fields = message.fields.filter(x => derived.contains(x.name))

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
       |def ${cToCamel(message.name)}(evt: ${cToPascal(message.name)}Event)(implicit conn: Connection): ${returnType} = {
       |  val rowsAffected = SQL(${indentWithLeftMargin(blockQuote(updateSql(root, fields, primaryKey.fields), 2), 2)})
       |  .on(
       |    ${indent(defFieldSubstitution(root, fields, "evt"), 4)}
       |  ).executeUpdate()
       |
       |  if(rowsAffected == 0)
       |    SQL(${indentWithLeftMargin(blockQuote(insertSql(root, fields), 2), 4)})
       |    .on(
       |      ${indent(defFieldSubstitution(root, fields, "evt"), 6)}
       |    ).executeUpdate()
       |  else rowsAffected
       |}
     """.stripMargin.trim
  }

  def defMessages(root: String, rootFields: Seq[Field], primaryKey: PrimaryKey, messages: Seq[Message]): Seq[String] = {
    val key = primaryKey.fields.map(_.name).toSet
    val derivedNonKey = rootFields.map(_.name).filter(!key.contains(_)).toSet

    messages
      .filter(x => !x.transient
        && !x.fields
        .filter(!_.transient)
        .filter(f => derivedNonKey.contains(f.name))
        .isEmpty)
      .map(defMessage(root, rootFields, primaryKey, _))
  }

  def defEmbeddedAggregateMessage(aggregateRoot: String, aggregate: Aggregate): String = {
    val nonKeyFieldCount = aggregate.fields.length - aggregate.primaryKey.fields.length
    val keyFieldNames = aggregate.primaryKey.fields.map(_.name).toSet
    val nonKeyFields = aggregate.fields.filter(x => !keyFieldNames.contains(x.name))
    val persistFields = aggregate.fields.filter(!_.transient)
    val nonKeyPersistFields = persistFields.filter(x => !keyFieldNames.contains(x.name))

    import aggregate._
    val parser = rowParser(name, persistFields, primaryKey.fields)

    val get = if (nonKeyFieldCount > 1)
      s"""
         |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo = {
         |  SQL(${indentWithLeftMargin(blockQuote(retrieveSql(aggregateRoot, persistFields, primaryKey.fields), 2), 2)})
         |  .on(
         |    ${indent(defFieldSubstitution(aggregateRoot, primaryKey.fields, "cmd"), 4)}
         |  ).as(${cToCamel(name)}Parser.single)
         |}
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type) {
        val embedded = toValueObject(getEntity(field.valueType, xml), name, xml)
        val otherKeyFields = embedded.primaryKey.fields.filter(x => !keyFieldNames.contains(x.name))
        val embeddedFields = embedded.fields.filter(x => !keyFieldNames.contains(x.name))
        s"""
           |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo = {
           |  ${cToPascal(aggregate.name)}Vo(${substituteMethodParams(primaryKey.fields, "cmd")}, ${cToCamel(field.valueType)}Dao.${callSelectByFk(primaryKey.fields, "cmd")})
           |}
     """.stripMargin.trim
      } else if ("map" == field._type) {
        // FIXME: not properly implemented
        s"""
           |
         """.stripMargin.trim
      } else {
        s"""
           |def get${cToPascal(aggregate.name)}(cmd: Get${cToPascal(aggregate.name)}Cmd)(implicit conn: Connection): ${cToPascal(aggregate.name)}Vo = {
           |  SQL(${indentWithLeftMargin(blockQuote(retrieveSql(aggregateRoot, persistFields, primaryKey.fields), 2), 2)})
           |  .on(
           |    ${indent(defFieldSubstitution(aggregateRoot, primaryKey.fields, "cmd"), 4)}
           |  ).as(${cToCamel(name)}Parser.single)
           |}
     """.stripMargin.trim
      }
    } else { // this cannot be happen.
      s"""
         |
     """.stripMargin.trim
    }

    val update = if (nonKeyFieldCount > 1)
      s"""
         |def update${cToPascal(aggregate.name)}(evt: Update${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int = {
         |  SQL(${indentWithLeftMargin(blockQuote(updateSql(aggregateRoot, persistFields, primaryKey.fields), 2), 2)})
         |  .on(
         |    ${indent(defFieldSubstitution(aggregateRoot, persistFields, "evt"), 4)}
         |  ).executeUpdate()
         |}
     """.stripMargin.trim
    else if (nonKeyFieldCount == 1) {
      val field = nonKeyFields.head
      if ("array" == field._type) {
        val embedded = toValueObject(getEntity(field.valueType, xml), name, xml)
        val otherKeyFields = embedded.primaryKey.fields.filter(x => !keyFieldNames.contains(x.name))
        val embeddedFields = embedded.fields.filter(x => !keyFieldNames.contains(x.name))
        s"""
           |def add${cToPascal(aggregate.name)}(evt: Add${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int = {
           |  evt.${cToCamel(aggregate.name)}
           |    .map(x => Create${cToPascal(field.valueType)}Event(
           |        ${substituteMethodParams(Seq(userField), "evt")}, ${substituteMethodParams(primaryKey.fields, "evt")}, ${substituteMethodParams(embeddedFields, "x")}
           |      )
           |     )
           |    .map(${cToCamel(field.valueType)}Dao.create${cToPascal(field.valueType)}(_))
           |    .foldLeft(0)((t, u) => t + u)
           |}
           |
           |def remove${cToPascal(aggregate.name)}(evt: Remove${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int = {
           |  evt.${cToCamel(aggregate.name)}
           |    .map(x => Delete${cToPascal(field.valueType)}Event(
           |        ${substituteMethodParams(Seq(userField), "evt")}, ${substituteMethodParams(primaryKey.fields, "evt")}, ${substituteMethodParams(otherKeyFields, "x")}
           |      )
           |     )
           |    .map(${cToCamel(field.valueType)}Dao.delete${cToPascal(field.valueType)}(_))
           |    .foldLeft(0)((t, u) => t + u)
           |}
     """.stripMargin.trim
      } else if ("map" == field._type) {
        // FIXME: not properly implemented
        s"""
           |// FIXME: not properly implemented
           |def add${cToPascal(aggregate.name)}(evt: Add${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int = {
           |  evt.${cToCamel(aggregate.name)}
           |    .map(x => Create${cToPascal(field.valueType)}Event(
           |        ${substituteMethodParams(Seq(userField), "evt")}, ${substituteMethodParams(primaryKey.fields, "evt")}, x._1, x._2
           |      )
           |     )
           |    .map(${cToCamel(field.valueType)}Dao.create${cToPascal(field.valueType)}(_))
           |    .foldLeft(0)((t, u) => t + u)
           |}
           |
           |// FIXME: not properly implemented
           |def remove${cToPascal(aggregate.name)}(evt: Remove${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int = {
           |  evt.${cToCamel(aggregate.name)}
           |    .map(x => Delete${cToPascal(field.valueType)}Event(
           |        ${substituteMethodParams(Seq(userField), "evt")}, ${substituteMethodParams(primaryKey.fields, "evt")}, x._1
           |      )
           |     )
           |    .map(${cToCamel(field.valueType)}Dao.delete${cToPascal(field.valueType)}(_))
           |    .foldLeft(0)((t, u) => t + u)
           |}
     """.stripMargin.trim
      } else // one-to-one relationship is not supported. simple JDBC fields only.
        s"""
           |def change${cToPascal(aggregate.name)}(evt: Change${cToPascal(aggregate.name)}Event)(implicit conn: Connection): Int = {
           |  SQL(${indentWithLeftMargin(blockQuote(updateSql(aggregateRoot, persistFields, primaryKey.fields), 2), 2)})
           |  .on(
           |    ${indent(defFieldSubstitution(aggregateRoot, persistFields, "evt"), 4)}
           |  ).executeUpdate()
           |}
     """.stripMargin.trim
    } else { // this cannot be happen.
      s"""
         |
     """.stripMargin.trim
    }

    s"""
       |${indentWithLeftMargin(parser, 0)}
       |
       |${indentWithLeftMargin(get, 0)}
       |
       |${indentWithLeftMargin(update, 0)}
     """.stripMargin.trim
  }

  def defEmbeddedAggregateMessages(aggregateRoot: String, aggregates: Seq[Aggregate]): Seq[String] = {
    aggregates
      .filter(!_.transient)
      .filter(x => {
        val keyFieldNames = x.primaryKey.fields.map(_.name).toSet
        !x.fields.filter(!_.transient)
          .filter(x => !keyFieldNames.contains(x.name))
          .isEmpty
      })
      .map(defEmbeddedAggregateMessage(aggregateRoot, _))
  }

  def defSelectByFks(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        defSelectByFk(name, fields, fkFields)
      })
  }

  def defSelectByFk(name: String, fields: Seq[Field], keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def selectBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): Seq[${cToPascal(name)}Vo] = {
       |  SQL(${indentWithLeftMargin(blockQuote(retrieveSql(name, fields, keyFields), 2), 2)})
       |  .on(
       |    ${indent(defFieldSubstitution(name, keyFields, ""), 4)}
       |  ).as(${cToCamel(name)}Parser.*)
       |}
     """.stripMargin.trim
  }

  def defDeleteByFks(name: String, fields: Seq[Field], foreignKeys: Seq[ForeignKey]): Seq[String] = {
    foreignKeys
      .map(x => {
        val fieldNames = x.fields
          .map(_.name)

        val fkFields = fields
          .filter(x => fieldNames.contains(x.name))
        defDeleteByFk(name, fields, fkFields)
      })
  }

  def defDeleteByFk(name: String, fields: Seq[Field], keyFields: Seq[Field]): String = {
    val by = keyFields
      .map(x => cToPascal(x.name))
      .reduceOption((x, y) => s"${x}${y}")
      .getOrElse("")

    s"""
       |def deleteBy${by}(${defMethodParams(keyFields)})(implicit conn: Connection): Int = {
       |  SQL(${indentWithLeftMargin(blockQuote(deleteSql(name, keyFields), 2), 2)})
       |  .on(
       |    ${indent(defFieldSubstitution(name, keyFields, ""), 4)}
       |  ).executeUpdate()
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
