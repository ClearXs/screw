package com.jw.screw.storage.initialize;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.storage.properties.DatabaseProperties;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractDatabaseRecoder;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.ByteArrayInputStream;

/**
 * 数据源初始化，实例化{@link SqlSessionFactory}
 * @author jiangw
 * @date 2021/7/22 17:28
 * @since 1.1
 */
class DatabaseInitializer implements RecordInitializer<SqlSessionFactory> {

    private static final String MYBATIS_CONFIG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">\n" +
                "<configuration>\n" +
                "    <typeAliases>\n" +
                "        ${typeAlias}\n" +
                "    </typeAliases>\n" +
                "    <environments default=\"development\">\n" +
                "        <environment id=\"development\">\n" +
                "            <transactionManager type=\"JDBC\"/>\n" +
                "            <dataSource type=\"POOLED\">\n" +
                "                <property name=\"driver\" value=\"${driverClassName}\" />\n" +
                "                <property name=\"url\" value=\"${jdbc_url}\" />\n" +
                "                <property name=\"username\" value=\"${jdbc_username}\" />\n" +
                "                <property name=\"password\" value=\"${jdbc_password}\" />\n" +
                "            </dataSource>\n" +
                "        </environment>\n" +
                "    </environments>\n" +
                "    <mappers>\n" +
                "        ${mapper}\n" +
                "    </mappers>\n" +
                "</configuration>";

    @Override
    public SqlSessionFactory init(StorageProperties properties, Object config) {
        if (config instanceof AbstractDatabaseRecoder.DatabaseConfig) {
            String mybatisConfig = MYBATIS_CONFIG;
            DatabaseProperties databaseProperties = properties.getDatabase();
            AbstractDatabaseRecoder.DatabaseConfig databaseConfig = (AbstractDatabaseRecoder.DatabaseConfig) config;
            mybatisConfig = mybatisConfig.replace("${typeAlias}", "<typeAlias type=\"" + databaseConfig.getEntity().getName() + "\" alias=\"" + databaseConfig.getEntity().getSimpleName() + "\" />");
            mybatisConfig = mybatisConfig.replace("${mapper}", "<mapper resource=\"" + databaseConfig.getMapper().getName().replace(StringPool.DOT, StringPool.SLASH) + ".xml\" />");
            mybatisConfig = mybatisConfig.replace("${driverClassName}", databaseProperties.getDriverClassName());
            mybatisConfig = mybatisConfig.replace("${jdbc_url}", databaseProperties.getUrl()
                    .replace(StringPool.AMPERSAND, "&amp;"));
            mybatisConfig = mybatisConfig.replace("${jdbc_username}", databaseProperties.getUserName());
            mybatisConfig = mybatisConfig.replace("${jdbc_password}", databaseProperties.getPassword());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mybatisConfig.getBytes());
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream);
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            return builder.build(parser.parse());
        }
        return null;
    }
}
