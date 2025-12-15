package com.rickyyeung.profolio.mapper;

import com.rickyyeung.profolio.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE userId = #{id}")
    User findById(Long id);
}
