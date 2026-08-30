DROP TABLE IF EXISTS tb_voucher_order;
DROP TABLE IF EXISTS tb_seckill_voucher;
DROP TABLE IF EXISTS tb_voucher;
DROP TABLE IF EXISTS tb_blog_comments;
DROP TABLE IF EXISTS tb_blog;
DROP TABLE IF EXISTS tb_follow;
DROP TABLE IF EXISTS tb_shop;
DROP TABLE IF EXISTS tb_shop_type;
DROP TABLE IF EXISTS tb_user_info;
DROP TABLE IF EXISTS tb_user;

CREATE TABLE tb_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(128),
  nick_name VARCHAR(64) NOT NULL,
  icon VARCHAR(255),
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_user_info (
  user_id BIGINT PRIMARY KEY,
  city VARCHAR(64),
  introduce VARCHAR(128),
  fans INT,
  followee INT,
  gender BOOLEAN,
  birthday DATE,
  credits INT,
  level BOOLEAN,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_shop_type (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  icon VARCHAR(255),
  sort INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_shop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  type_id BIGINT NOT NULL,
  images VARCHAR(1024),
  area VARCHAR(64),
  address VARCHAR(255),
  x DOUBLE,
  y DOUBLE,
  avg_price BIGINT,
  sold INT,
  comments INT,
  score INT,
  open_hours VARCHAR(64),
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_follow (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  follow_user_id BIGINT NOT NULL,
  create_time TIMESTAMP,
  UNIQUE(user_id, follow_user_id)
);

CREATE TABLE tb_blog (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  shop_id BIGINT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  images VARCHAR(1024),
  content CLOB,
  liked INT,
  comments INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_blog_comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  blog_id BIGINT NOT NULL,
  parent_id BIGINT,
  answer_id BIGINT,
  content VARCHAR(512),
  liked INT,
  status BOOLEAN,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_voucher (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  shop_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  sub_title VARCHAR(255),
  rules VARCHAR(1024),
  pay_value BIGINT NOT NULL,
  actual_value BIGINT NOT NULL,
  type INT,
  status INT,
  create_time TIMESTAMP,
  update_time TIMESTAMP
);

CREATE TABLE tb_seckill_voucher (
  voucher_id BIGINT PRIMARY KEY,
  stock INT NOT NULL,
  create_time TIMESTAMP,
  begin_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
  update_time TIMESTAMP
);

CREATE TABLE tb_voucher_order (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  pay_type INT,
  status INT,
  create_time TIMESTAMP,
  pay_time TIMESTAMP,
  use_time TIMESTAMP,
  refund_time TIMESTAMP,
  update_time TIMESTAMP,
  UNIQUE(user_id, voucher_id)
);
