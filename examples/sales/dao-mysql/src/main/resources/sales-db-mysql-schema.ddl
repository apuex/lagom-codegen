/*****************************************************
 ** This file is 100% ***GENERATED***, DO NOT EDIT! **
 *****************************************************/

DROP USER IF EXISTS 'sales';
DROP DATABASE IF EXISTS sales;

CREATE DATABASE sales DEFAULT CHARACTER SET 'UTF8' DEFAULT COLLATE utf8_unicode_ci;

CREATE USER 'sales' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON *.* TO 'sales' WITH GRANT OPTION;

USE sales;

CREATE TABLE sales.alarm (
  alarm_id VARCHAR(64),
  alarm_begin DATETIME,
  alarm_end DATETIME,
  alarm_desc VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE sales.payment_type (
  payment_type_id INT,
  payment_type_name VARCHAR(64),
  payment_type_label VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE sales.order_item (
  order_id VARCHAR(64),
  product_id VARCHAR(64),
  product_name VARCHAR(64),
  item_unit VARCHAR(64),
  unit_price DOUBLE,
  order_quantity DOUBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE sales.product (
  product_id VARCHAR(64),
  product_name VARCHAR(64),
  product_unit VARCHAR(64),
  unit_price DOUBLE,
  record_time DATETIME,
  quantity_sold DOUBLE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE sales.order (
  order_id VARCHAR(64),
  order_time DATETIME,
  order_payment_type INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE sales.alarm ADD CONSTRAINT alarm_pk PRIMARY KEY(alarm_id, alarm_begin);

ALTER TABLE sales.payment_type ADD CONSTRAINT payment_type_pk PRIMARY KEY(payment_type_id);

ALTER TABLE sales.order_item ADD CONSTRAINT order_item_pk PRIMARY KEY(order_id, product_id);

ALTER TABLE sales.product ADD CONSTRAINT product_pk PRIMARY KEY(product_id);

ALTER TABLE sales.order ADD CONSTRAINT order_pk PRIMARY KEY(order_id);

ALTER TABLE sales.order_item ADD CONSTRAINT order_item_order_fk FOREIGN KEY(order_id) REFERENCES sales.order(order_id);

ALTER TABLE sales.order_item ADD CONSTRAINT order_item_product_fk FOREIGN KEY(product_id) REFERENCES sales.product(product_id);
