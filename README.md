# lagom-codegen
A code generator for Lagom scala microservices.

## Usage

Example:

```
$ java -Doutput.dir=examples -jar codegen/target/scala-2.12/lagom-codegen-1.0.0.jar generate-all codegen/src/test/resources/sales_entities.xml
```

## Build crud-app

```
$ cd examples/sales
```
and
```
$ sbt crud-app/assembly
java -jar crud-app/target/scala-2.12/sales-crud-app-assembly-1.0.0.jar
```
or
```
$ sbt crud-app/universal:packageBin
```
and upack `crud-app/target/universal/sales-crud-app-1.0.0.zip` then run `sales-crud-app-1.0.0/bin/sales-crud-app`.

