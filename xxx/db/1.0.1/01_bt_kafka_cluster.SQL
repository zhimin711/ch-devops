alter TABLE `bt_kafka_cluster` add column `tenant_id` VARCHAR(50) NULL COMMENT '租户ID',
 add column `SECURITY_PROTOCOL` VARCHAR(200) NULL COMMENT '安全协议',
 add column `SASL_MECHANISM` VARCHAR(200) NULL COMMENT '授权协议',
 add column `AUTH_USERNAME` VARCHAR(200) NULL COMMENT '授权用户名',
 add column `AUTH_PASSWORD` VARCHAR(200) NULL COMMENT '授权密码';
