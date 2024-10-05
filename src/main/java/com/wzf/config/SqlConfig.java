package com.wzf.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;


import javax.sql.DataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class SqlConfig {
    private static DataSource dataSource = null;

    private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    static {
        //初始化连接池对象

        InputStream ips = Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties");

        Properties properties = new Properties();
        try {
            properties.load(ips);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //对外提供获取连接的方法
    public static Connection getConnection() throws SQLException {
        //线程本地变量中是否存在
        Connection connection = threadLocal.get();
        //第一次没有
        if (connection == null) {
            //线程本地变量没有，连接池获取，返回本地变量的连接
            connection = dataSource.getConnection();
            threadLocal.set(connection);
        }

        return connection;
    }
    public static void freeConnection() throws SQLException {
        Connection connection = threadLocal.get();
        if (connection != null){
            threadLocal.remove();//清空线程本地变量数据
            connection.setAutoCommit(true);//事务状态回到默认状态
            connection.close();//回收到连接池
        }
    }
}
