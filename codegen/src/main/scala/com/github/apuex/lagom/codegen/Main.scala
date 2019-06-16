package com.github.apuex.lagom.codegen


object Main extends App {
  if (args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "generate-message" => MessageGenerator(args.drop(1)(0)).generate()
      case "generate-entity" => EntityGenerator(args.drop(1)(0)).generate()
      case "generate-json-serializer" => JsonSerializerGenerator(args.drop(1)(0)).generate()
      case "generate-service" => ServiceGenerator(args.drop(1)(0)).generate()
      case "generate-dao" => DaoGenerator(args.drop(1)(0)).generate()
      case "generate-project-settings" => ProjectGenerator(args.drop(1)(0)).generate()
      case "generate-all" => generateAll(args.drop(1)(0))
      case c =>
        println(s"unknown command '${c}'")
    }
  }

  def generateAll(fileName: String): Unit = {
    MessageGenerator(fileName).generate()
    EntityGenerator(fileName).generate()
    JsonSerializerGenerator(fileName).generate()
    ServiceGenerator(fileName).generate()
    DaoGenerator(fileName).generate()
    ProjectGenerator(fileName).generate()
  }
}
