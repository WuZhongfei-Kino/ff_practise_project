package com.wzf.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author WuZhongfei
 * @date 2024年09月29日 17:17
 */
@Component
@Slf4j
public class JdbcUtil {
    private static String driverClass;
    private static String dbUrl;
    private static String dbUserName;
    private static String dbPassword;

    //静态代码块，静态代码块在类文件加载阶段一定执行！！！并且有且只执行一次，用于程序初始化，预处理操作
    static {
        //1.获取properties文件
        //JdbcUtils.class   反射知识，获得Class实例，即JdbcUtils类
        //getClassLoader()    获得类加载器
        InputStream resourceAsStream=JdbcUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
        //2.创建Properties实例，底层是Map键值对存储
        Properties properties = new Properties();
        //3.加载properties文件
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //4.将properties中的有效值提取出来
        driverClass = properties.getProperty("driverClass");
        dbUrl = properties.getProperty("url");
        dbUserName = properties.getProperty("username");
        dbPassword = properties.getProperty("password");
        //5.加载驱动
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //6.释放流资源
        try {
            if(resourceAsStream != null){
                resourceAsStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //静态方法获得连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
    }

    //Object... parameters   意思是多个Object类型数据
    public static PreparedStatement getPreparedStatement(String sql, Connection connection, Object... parameters) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        //获得元数据
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        //获得字符串需要的参数个数
        int count = parameterMetaData.getParameterCount();
        //判断什么情况才需要给preparedStatement赋值
        if(count != 0 && parameters != null && parameters.length == count){
            for (int i = 0; i < count; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
        }
        return preparedStatement;
    }

    public static void close(Connection connection, Statement statement) {
        close(statement, connection);
    }


    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        close(resultSet, statement, connection);
    }

    //AutoCloseable... resources：多个AutoCloseable实例，通过源码我们可以知道collection，statement，resultSet都是           AutoCloseable的子类
    static void close(AutoCloseable... resources) {
        if (resources != null && resources.length > 0) {
            Arrays.stream(resources).forEach(source -> {
                try {
                    if (source != null) {
                        source.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public int insertSql(String sql, Object... parameters)  {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int s = 0;
        try {
            //调用方法获得连接
            connection = JdbcUtil.getConnection();
            //开启事务
            connection.setAutoCommit(false);
            //预加载
            preparedStatement = connection.prepareStatement(sql);
            //获得需要的字符串参数个数
            int num = preparedStatement.getParameterMetaData().getParameterCount();
            //验证是否需要遍历给？赋值
            if (num != 0 && parameters != null && parameters.length == num) {
                //遍历参数
                for (int i = 0; i < num; i++) {
                    preparedStatement.setObject(i+1,parameters[i]);
                }}
//注：上面的代码可以替换成JdbcUtils.getPreparedStatement()方法，同样可以的到preparedStatement
            //开始操作，并返回影响行数，
            s = preparedStatement.executeUpdate();
            //提交事务
            if (s > 0) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException e){


        } finally {
            if ( connection != null  && preparedStatement != preparedStatement){
                //关闭资源
                JdbcUtil.close(connection,preparedStatement );
            }
        }

        //返回影响数
        return s ;
    }

    public <T> List<T> selectSql(Class<T> clazz, String sql, Object... parameters) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {

        List<T> list = new ArrayList<>();
        //调用方法获得连接
        Connection connection = JdbcUtil.getConnection();
        //开启事务
        connection.setAutoCommit(false);
        //预加载
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        //获得需要的字符串参数个数
        int num = preparedStatement.getParameterMetaData().getParameterCount();
        //验证是否需要遍历给？赋值
        if (num != 0 && parameters != null && parameters.length == num) {
            //遍历参数
            for (int i = 0; i < num; i++) {
                preparedStatement.setObject(i+1,parameters[i]);
            }}
//注：上面的代码可以替换成JdbcUtils.getPreparedStatement()方法，同样可以的到preparedStatement
        //开始操作，并返回影响行数，
        ResultSet resultSet = preparedStatement.executeQuery(sql);
        //获取列信息的对象
        ResultSetMetaData metaData = resultSet.getMetaData();
        //获取列信息的数量
        int columnCount = metaData.getColumnCount();
        //处理结果集
        while (resultSet.next()){
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
        //提交事务
        connection.commit();
        //关闭资源
        JdbcUtil.close(connection,preparedStatement );
        //返回结果
        return list ;
    }





}
