package com.github.apuex.lagom.codegen


object Main extends App {
  if (args.length == 0) {
    println("Usage:\n" +
      "\tjava -jar <this jar> <arg list>")
  } else {
    val fileName = args.drop(1)(0)
    val modelLoader = ModelLoader(fileName)
    args(0) match {
      case "generate-app-conf" => AppConfGenerator(modelLoader).generate()
      case "generate-cq-app-conf" => CqAppConfGenerator(modelLoader).generate()
      case "generate-crud-app-conf" => CrudAppConfGenerator(modelLoader).generate()
      case "generate-app-loader" => AppLoaderGenerator(modelLoader).generate()
      case "generate-model-test" => ModelTestGenerator(modelLoader).generate()
      case "generate-message" => MessageGenerator(modelLoader).generate()
      case "generate-entity" => EntityGenerator(modelLoader).generate()
      case "generate-json-serializer" => JsonSerializerGenerator(modelLoader).generate()
      case "generate-crud-service" => CrudServiceGenerator(modelLoader).generate()
      case "generate-crud-event-apply" => CrudEventsAppGenerator(modelLoader).generate()
      case "generate-actor" => ActorGenerator(modelLoader).generate()
      case "generate-cluster-shard-actor" => ClusterShardActorGenerator(modelLoader).generate()
      case "generate-cqrs-service" => CqrsServiceGenerator(modelLoader).generate()
      case "generate-cqrs-event-apply" => CqrsEventsAppGenerator(modelLoader).generate()
      case "generate-service" => ServiceGenerator(modelLoader).generate()
      case "generate-dao" => DaoGenerator(modelLoader).generate()
      case "generate-dao-mysql" => DaoMysqlImplGenerator(modelLoader).generate()
      case "generate-dbschema-mysql" => MysqlSchemaGenerator(modelLoader).generate()
      case "generate-project-settings" => ProjectGenerator(modelLoader).generate()
      case "generate-frontend-message" => FrontendMessageGenerator(modelLoader).generate()
      case "generate-frontend-service" => FrontendServiceGenerator(modelLoader).generate()
      case "generate-all" => generateAll(modelLoader)
      case "generate-backend" => generateBackend(modelLoader)
      case "generate-frontend" => generateFrontend(modelLoader)
      case c =>
        println(s"unknown command '${c}'")
    }
  }

  def generateAll(modelLoader: ModelLoader): Unit = {
    generateBackend(modelLoader)
    generateFrontend(modelLoader)
  }

  def generateFrontend(modelLoader: ModelLoader): Unit = {
    FrontendMessageGenerator(modelLoader).generate()
    FrontendServiceGenerator(modelLoader).generate()
  }

  def generateBackend(modelLoader: ModelLoader): Unit = {
    AppConfGenerator(modelLoader).generate()
    CqAppConfGenerator(modelLoader).generate()
    CrudAppConfGenerator(modelLoader).generate()
    AppLoaderGenerator(modelLoader).generate()
    ModelTestGenerator(modelLoader).generate()
    MessageGenerator(modelLoader).generate()
    EntityGenerator(modelLoader).generate()
    JsonSerializerGenerator(modelLoader).generate()
    ServiceGenerator(modelLoader).generate()
    ActorGenerator(modelLoader).generate()
    ClusterShardActorGenerator(modelLoader).generate()
    // TODO: NOT implemented.
    CqrsServiceGenerator(modelLoader).generate()
    CqrsEventsAppGenerator(modelLoader).generate()
    CrudServiceGenerator(modelLoader).generate()
    CrudEventsAppGenerator(modelLoader).generate()
    DaoGenerator(modelLoader).generate()
    DaoMysqlImplGenerator(modelLoader).generate()
    MysqlSchemaGenerator(modelLoader).generate()
    ProjectGenerator(modelLoader).generate()
  }
}
