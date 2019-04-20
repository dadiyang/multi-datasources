# SpringBoot 多数据源配置实例

本项目用于演示在 SpringBoot 中配置多数据源，并根据注解选择数据源的用例。

目前仅演示分库分表场景的多数据源配置

# 起因

写这个实例是因为一个导数据的需求——将多个库中的所有指定一张表的全量数据从 Mysql 同步到 ES 中。

# 实现

1. 继承 `AbstractRoutingDataSource` 数据源，重写 `determineCurrentLookupKey` 方法，来定义我们的数据源选择规则。
2. 配置数据源，并且将我们写的 RoutingDataSource 定义为首选数据源，详情请查看 `DataSourceConfiguration` 中的代码和注释
3. 定义注解和拦截这个注解的切面，在切面中根据分库分表规则往 `RoutingDataSource` 中设置选中的数据源 key

# DEMO 说明

这里演示通过表号来做为分库分表扫描的依赖。

假设：

我们有 32 张表，序号为 0~32，放在 2 个库中，每个库 16 张表，即 0 号表在 0 库的 0 表，16 号表在 1 库的 0 表。

**用于演示的分库分表建表存储过程：**

注：若执行存储过程发生异常：Thread stack overrun: xxx bytes used of a xxxx byte stack, and……

需要在 Mysql 配置文件 /etc/my.cnf 中配置 thread_stack = 256K，然后重启 Mysql

```sql
CREATE DEFINER=`root`@`localhost` PROCEDURE `createTables`()
begin
    -- 声明参数
    DECLARE j int;
    DECLARE i int;
    DECLARE dbName VARCHAR(20);
    DECLARE tbName VARCHAR(30);
    DECLARE dbPre VARCHAR(15);
    DECLARE tablePre VARCHAR(15);
    DECLARE dbCount int;
    DECLARE tbCountPerDb int;
    DECLARE sql_text VARCHAR(2000);
    set j=0;
    set i=0;
    
    set dbPre='db_order_';
    set tablePre='t_order_';
    set dbCount = 2;
    set tbCountPerDb = 16;
    
    WHILE(i<dbCount) DO
        set dbName=CONCAT(dbPre, Convert(i, CHAR(10)));
        set sql_text = CONCAT('CREATE DATABASE ', dbName);
        SELECT sql_text;
        SET @sql_text1=sql_text;  
        PREPARE stmt FROM @sql_text1;  
        EXECUTE stmt;
    
        WHILE j < tbCountPerDb DO
            set tbName=CONCAT(dbName, '.', tablePre, Convert(j, CHAR(10)));
            SELECT tbName;
            SET sql_text=CONCAT('CREATE TABLE ', tbName, '(`id` BIGINT NOT NULL AUTO_INCREMENT,',
                    '`username` VARCHAR(40) NOT NULL COMMENT \'用户名\','
                    '`company_name` VARCHAR(30) NOT NULL DEFAULT \'\' COMMENT \'公司名\',',
                    '`amount` int NOT NULL COMMENT \'订单金额\',',
                    '`source` VARCHAR(5) NOT NULL DEFAULT \'\' COMMENT \'来源\',',
                    '`created_on` datetime NOT NULL COMMENT \'创建时间\',',
                    '`updated_on` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT \'更新时间(会自动更新，不需要刻意程序更新)\',',
                    'PRIMARY KEY (`id`)',
                ') ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT=\'订单表\'');
            SELECT sql_text;
            SET @sql_text=sql_text;  
            PREPARE stmt FROM @sql_text;  
            EXECUTE stmt;  
            DEALLOCATE PREPARE stmt;    
            SET j = j+1 ;
        END WHILE;
        SET i = i+1;
        SET j = 0;
    END WHILE;
end
```
