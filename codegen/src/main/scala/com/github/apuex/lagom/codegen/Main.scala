package com.github.apuex.lagom.codegen


object Main extends App {
  if (args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    args(0) match {
      case "generate-app-conf" => AppConfGenerator(args.drop(1)(0)).generate()
      case "generate-crud-app-conf" => CrudAppConfGenerator(args.drop(1)(0)).generate()
      case "generate-app-loader" => AppLoaderGenerator(args.drop(1)(0)).generate()
      case "generate-model-test" => ModelTestGenerator(args.drop(1)(0)).generate()
      case "generate-message" => MessageGenerator(args.drop(1)(0)).generate()
      case "generate-entity" => EntityGenerator(args.drop(1)(0)).generate()
      case "generate-json-serializer" => JsonSerializerGenerator(args.drop(1)(0)).generate()
      case "generate-crud-service" => CrudServiceGenerator(args.drop(1)(0)).generate()
      case "generate-cqrs-service" => CqrsServiceGenerator(args.drop(1)(0)).generate()
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
    AppConfGenerator(modelLoader).generate()
    CrudAppConfGenerator(modelLoader).generate()
    AppLoaderGenerator(modelLoader).generate()
    ModelTestGenerator(modelLoader).generate()
    MessageGenerator(modelLoader).generate()
    EntityGenerator(modelLoader).generate()
    JsonSerializerGenerator(modelLoader).generate()
    ServiceGenerator(modelLoader).generate()
    CqrsServiceGenerator(modelLoader).generate()
    CrudServiceGenerator(modelLoader).generate()
    DaoGenerator(modelLoader).generate()
    DaoMysqlImplGenerator(modelLoader).generate()
    ProjectGenerator(modelLoader).generate()
  }
}
