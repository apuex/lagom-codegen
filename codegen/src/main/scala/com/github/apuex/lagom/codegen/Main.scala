package com.github.apuex.lagom.codegen


object Main extends App {
  if (args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "generate-application-conf" => ApplicationConfGenerator(args.drop(1)(0)).generate()
      case "generate-application-loader" => ApplicationLoaderGenerator(args.drop(1)(0)).generate()
      case "generate-model-test" => ModelTestGenerator(args.drop(1)(0)).generate()
      case "generate-message" => MessageGenerator(args.drop(1)(0)).generate()
      case "generate-entity" => EntityGenerator(args.drop(1)(0)).generate()
      case "generate-json-serializer" => JsonSerializerGenerator(args.drop(1)(0)).generate()
      case "generate-service" => ServiceGenerator(args.drop(1)(0)).generate()
      case "generate-dao" => DaoGenerator(args.drop(1)(0)).generate()
      case "generate-dao-mysql" => DaoMysqlImplGenerator(args.drop(1)(0)).generate()
      case "generate-project-settings" => ProjectGenerator(args.drop(1)(0)).generate()
      case "generate-all" => generateAll(args.drop(1)(0))
      case c =>
        println(s"unknown command '${c}'")
    }
  }

  def generateAll(fileName: String): Unit = {
    val modelLoader = ModelLoader(fileName)
    ApplicationConfGenerator(modelLoader).generate()
    ApplicationLoaderGenerator(modelLoader).generate()
    ModelTestGenerator(modelLoader).generate()
    MessageGenerator(modelLoader).generate()
    EntityGenerator(modelLoader).generate()
    JsonSerializerGenerator(modelLoader).generate()
    ServiceGenerator(modelLoader).generate()
    DaoGenerator(modelLoader).generate()
    DaoMysqlImplGenerator(modelLoader).generate()
    ProjectGenerator(modelLoader).generate()
  }
}
