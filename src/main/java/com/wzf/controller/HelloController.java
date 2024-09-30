package com.wzf.controller;

import com.wzf.domain.InDB;
import com.wzf.utils.JdbcUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author WuZhongfei
 * @date 2024年09月29日 16:20
 */
@RestController
@RequestMapping("/api")
@ComponentScan("com.wzf")
public class HelloController {
    @GetMapping("/say")
    public String sayHello() throws SQLException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
//        final Connection connection = JdbcUtil.getConnection();
//        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
//        context.refresh();
//        JdbcUtil jdbcUtil = context.register(JdbcUtil.class);
        JdbcUtil jdbcUtil = new JdbcUtil();
        List<InDB> inDBList = jdbcUtil.selectSql(InDB.class, "select * from indb where id = ?", "1");

        System.out.println(inDBList);
        return "Hello World!";

    }

    @PostMapping("/call")
    public String call(){
        return "please call me!";
    }
}
