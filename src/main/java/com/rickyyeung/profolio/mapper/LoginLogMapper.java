package com.rickyyeung.profolio.mapper;

import com.rickyyeung.profolio.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginLogMapper {
    @Insert("INSERT INTO loginlog (loginLogUserId, loginLogIpAddress, loginLogLoginTime) " +
            "VALUES (#{userId}, #{ipAddress}, NOW())")
    int insertLoginLog(@Param("userId") Long userId, @Param("ipAddress") String ipAddress);
}
