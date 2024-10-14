package com.wzf.utils;




import com.wzf.config.SqlConfig;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;

import java.util.List;


/**
 * @author WuZhongfei
 * @Description: jdbc封装工具类
 * @Version: 1.0
 * @date 2024年09月29日 17:17
 */

@Slf4j
public class JdbcUtil {
    /*
     * 封装简化非DQL语句
     * sql 带占位符的sql语句
     * params 占位符的值
     * return 执行影响的行数*/
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        Connection connection = SqlConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        //可变参数可以当数组使用
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        int rows = preparedStatement.executeUpdate();
        preparedStatement.close();
        if (connection.getAutoCommit()) {
            //没有开启事务，正常回收连接
            SqlConfig.freeConnection();
        }
        return rows;
    }

    public static <T> List<T> executeQuery(Class<T> clazz, String sql, Object... params) throws Exception {

        List<T> list = new ArrayList<>();
        //获取连接
        Connection connection = SqlConfig.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        //结果集解析
        ResultSet resultSet = preparedStatement.executeQuery();

        //获取列信息的对象
        ResultSetMetaData metaData = resultSet.getMetaData();
        //获取列信息的数量
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {

            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
            T t = declaredConstructor.newInstance();

            for (int i = 1; i <= columnCount; i++) {
                //列的属性值
                Object value = resultSet.getObject(i);
                //列的属性名
                String propertyName = metaData.getColumnLabel(i);
                //反射给对象的属性值赋值
                Field fileld = clazz.getDeclaredField(propertyName);
                fileld.setAccessible(true);
                fileld.set(t, value);
            }
            list.add(t);
        }
        resultSet.close();
        preparedStatement.close();
        if (connection.getAutoCommit()) {
            SqlConfig.freeConnection();
        }
        return list;
    }


}
