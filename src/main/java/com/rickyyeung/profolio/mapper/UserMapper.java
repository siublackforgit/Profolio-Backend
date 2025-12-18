package com.rickyyeung.profolio.mapper;

import com.rickyyeung.profolio.model.User;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (email, password_hash, googleId, display_name, avatarUrl, createdDate, createdBy, lastUpdatedDate, lastUpdatedBy)" +
            "VALUES (#{email}, #{passwordHash}, #{googleId}, #{displayName}, #{avatarUrl}, #{createdDate}, #{createdBy} , #{lastUpdatedDate}, #{lastUpdatedBy} )")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insertUser(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Select("SELECT * FROM users WHERE google_id = #{googleId}")
    Optional<User> findByGoogleId(String googleId);

    @Update("UPDATE users SET displayName = #{displayName}, avatarUrl = #{avatarUrl},  lastUpdatedBy = #{lastUpdatedBy}, lastUpdatedDate = Now() " +
            "WHERE userId = #{userId}")
    void updateUser(User user);
}
