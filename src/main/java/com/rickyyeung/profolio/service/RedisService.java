package com.rickyyeung.profolio.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
/** @noinspection ALL*/
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeToken(String token, Long userId) {
        redisTemplate.opsForValue().set("TOKEN:" + token, userId.toString(), Duration.ofDays(1));
    }

    public Boolean isTokenValid(String token) {
        return redisTemplate.hasKey("TOKEN:"+token);
    }

    public void deleteToken(String token){
        redisTemplate.delete("TOKEN:"+token);
    }
}
