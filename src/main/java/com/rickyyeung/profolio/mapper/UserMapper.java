package com.rickyyeung.profolio.mapper;

import com.rickyyeung.profolio.model.User;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (email, passwordHash, googleId, displayName, avatarUrl, createdDate, createdBy, lastUpdatedDate, lastUpdatedBy)" +
            "VALUES (#{email}, #{passwordHash}, #{googleId}, #{displayName}, #{avatarUrl}, NOW(), #{createdBy} , NOW() , #{lastUpdatedBy} )")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insertUser(User user);

    @Select("SELECT * FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Select("SELECT * FROM users WHERE google_id = #{googleId}")
    Optional<User> findByGoogleId(String googleId);

    @Update("UPDATE users SET " +
            "email = #{email}, " +
            "passwordHash = #{passwordHash}, " +
            "googleId = #{googleId}, " +
            "displayName = #{displayName}, " +
            "avatarUrl = #{avatarUrl}, " +
            "isEmailVerified = #{isEmailVerified}, " +
            "lastUpdatedBy = #{lastUpdatedBy}, " +
            "lastUpdatedDate = NOW() " +
            "WHERE userId = #{userId}")
    void updateUser(User user);
}
