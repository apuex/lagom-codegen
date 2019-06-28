package com.github.apuex.lagom.codegen

import com.github.apuex.lagom.codegen.ModelLoader._
import org.scalatest._

class MessageGeneratorSpec extends FlatSpec with Matchers {
  val model = fromClasspath("sales_entities.xml")
  val messageGenerator = MessageGenerator(model)

  import messageGenerator._
  import model._

  "A MessageGenerator" should "generateMessagesForEmbeddedAggregate" in {
    val aggregate = xml.child.filter(_.label == "entity")
      .map(x => {
        if ("" == x.\@("aggregatesTo")) Some(toAggregate(x, xml))
        else None
      })
      .filter(_.isDefined)
      .map(_.get)
      .filter(_.name == "product").head

    val generated = aggregate.aggregates
      .map(generateMessagesForEmbeddedAggregate(_, aggregate.name, messageSrcPackage))
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
    generated should not be("")
    generated should be(
      s"""
         |message ProductSalesVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  google.protobuf.Timestamp recordTime = 2; // 销量最后更新时间
         |  double quantitySold = 3; // 销量
         |}
         |
         |message ProductSalesListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductSalesVo items = 1;
         |}
         |
         |message GetProductSalesCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message UpdateProductSalesCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  google.protobuf.Timestamp recordTime = 3; // 销量最后更新时间
         |  double quantitySold = 4; // 销量
         |}
         |
         |message UpdateProductSalesEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  google.protobuf.Timestamp recordTime = 3; // 销量最后更新时间
         |  double quantitySold = 4; // 销量
         |}
         |
         |message ProductNameVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  string productName = 2; // 商品名称
         |}
         |
         |message ProductNameListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductNameVo items = 1;
         |}
         |
         |message GetProductNameCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message ChangeProductNameCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |}
         |
         |message ChangeProductNameEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |}
         |
         |message ProductUnitVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  string productUnit = 2; // 计价单位
         |}
         |
         |message ProductUnitListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductUnitVo items = 1;
         |}
         |
         |message GetProductUnitCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message ChangeProductUnitCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productUnit = 3; // 计价单位
         |}
         |
         |message ChangeProductUnitEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productUnit = 3; // 计价单位
         |}
         |
         |message UnitPriceVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  double unitPrice = 2; // 单价
         |}
         |
         |message UnitPriceListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated UnitPriceVo items = 1;
         |}
         |
         |message GetUnitPriceCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message ChangeUnitPriceCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  double unitPrice = 3; // 单价
         |}
         |
         |message ChangeUnitPriceEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  double unitPrice = 3; // 单价
         |}
       """.stripMargin.trim)
  }

  it should "generateMessagesForAggregate" in {
    val aggregate = xml.child.filter(_.label == "entity")
      .map(x => {
        if ("" == x.\@("aggregatesTo")) Some(toAggregate(x, xml))
        else None
      })
      .filter(_.isDefined)
      .map(_.get)
      .filter(_.name == "product")

    val generated = aggregate
      .map(generateMessagesForAggregate(_, messageSrcPackage))
      .flatMap(x => x)
      .reduceOption((l, r) => s"${l}\n\n${r}")
      .getOrElse("")
    generated should be(
      s"""
         |message ProductSalesVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  google.protobuf.Timestamp recordTime = 2; // 销量最后更新时间
         |  double quantitySold = 3; // 销量
         |}
         |
         |message ProductSalesListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductSalesVo items = 1;
         |}
         |
         |message GetProductSalesCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message UpdateProductSalesCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  google.protobuf.Timestamp recordTime = 3; // 销量最后更新时间
         |  double quantitySold = 4; // 销量
         |}
         |
         |message UpdateProductSalesEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  google.protobuf.Timestamp recordTime = 3; // 销量最后更新时间
         |  double quantitySold = 4; // 销量
         |}
         |
         |message ProductNameVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  string productName = 2; // 商品名称
         |}
         |
         |message ProductNameListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductNameVo items = 1;
         |}
         |
         |message GetProductNameCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message ChangeProductNameCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |}
         |
         |message ChangeProductNameEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |}
         |
         |message ProductUnitVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  string productUnit = 2; // 计价单位
         |}
         |
         |message ProductUnitListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductUnitVo items = 1;
         |}
         |
         |message GetProductUnitCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message ChangeProductUnitCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productUnit = 3; // 计价单位
         |}
         |
         |message ChangeProductUnitEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productUnit = 3; // 计价单位
         |}
         |
         |message UnitPriceVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  double unitPrice = 2; // 单价
         |}
         |
         |message UnitPriceListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated UnitPriceVo items = 1;
         |}
         |
         |message GetUnitPriceCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message ChangeUnitPriceCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  double unitPrice = 3; // 单价
         |}
         |
         |message ChangeUnitPriceEvent{
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  double unitPrice = 3; // 单价
         |}
         |
         |message ProductVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  string productId = 1; // 商品编号
         |  string productName = 2; // 商品名称
         |  string productUnit = 3; // 计价单位
         |  double unitPrice = 4; // 单价
         |  google.protobuf.Timestamp recordTime = 5; // 销量最后更新时间
         |  double quantitySold = 6; // 销量
         |}
         |
         |message ProductListVo {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ValueObject";
         |  repeated ProductVo items = 1;
         |}
         |
         |message CreateProductCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |  string productUnit = 4; // 计价单位
         |  double unitPrice = 5; // 单价
         |  google.protobuf.Timestamp recordTime = 6; // 销量最后更新时间
         |  double quantitySold = 7; // 销量
         |}
         |
         |message RetrieveProductCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message UpdateProductCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |  string productUnit = 4; // 计价单位
         |  double unitPrice = 5; // 单价
         |  google.protobuf.Timestamp recordTime = 6; // 销量最后更新时间
         |  double quantitySold = 7; // 销量
         |}
         |
         |message DeleteProductCmd {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductCommand";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
         |
         |message CreateProductEvent {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |  string productUnit = 4; // 计价单位
         |  double unitPrice = 5; // 单价
         |  google.protobuf.Timestamp recordTime = 6; // 销量最后更新时间
         |  double quantitySold = 7; // 销量
         |}
         |
         |message UpdateProductEvent {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |  string productName = 3; // 商品名称
         |  string productUnit = 4; // 计价单位
         |  double unitPrice = 5; // 单价
         |  google.protobuf.Timestamp recordTime = 6; // 销量最后更新时间
         |  double quantitySold = 7; // 销量
         |}
         |
         |message DeleteProductEvent {
         |  option (scalapb.message).extends = "com.github.apuex.commerce.sales.ProductEvent";
         |  string userId = 1; // 用户ID
         |  string productId = 2; // 商品编号
         |}
       """.stripMargin.trim)
  }

  it should "generateMessage" in {

  }

  it should "generateEvent" in {

  }

  it should "generateMessages" in {

  }

  it should "generateEvents" in {

  }

  it should "generateCrudCmd" in {

  }

  it should "generateValueObject" in {

  }

  it should "generateField" in {

  }

  it should "generateFields" in {

  }
}
