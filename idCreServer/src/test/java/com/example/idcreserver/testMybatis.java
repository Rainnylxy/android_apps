package com.example.idcreserver;

import com.example.idcreserver.entity.User;
import com.example.idcreserver.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest(classes = SpringBootApplication.class)
@RunWith(SpringRunner.class)
public class testMybatis {
    DataSource dataSource ;
    @Autowired
    @Test
    public void contextLoads() throws SQLException {
        System.out.println(dataSource.getClass());
        Connection connection = dataSource.getConnection();
        System.out.println(connection);

        //template模板，拿来即用
        connection.close();
    }
    @Autowired
    UserMapper userMapper;
    @Test
    public void toTest(){
        List<User> userLogins = userMapper.queryAll();
        userLogins.forEach(e-> System.out.println(e));
    }
}