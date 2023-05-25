package com.example.idcreserver.mapper;

import com.example.idcreserver.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/*
interface 代表接口
 */
//使用这个接口代表这是一个Mybatis的Mapper类
@Mapper
@Repository

public interface UserMapper {

    List<User> queryAll();

    int addUser(User user);

    int updateUser(User newUser);

    int deleteUser(int id);
}
