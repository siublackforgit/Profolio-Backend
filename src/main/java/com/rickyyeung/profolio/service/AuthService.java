package com.rickyyeung.profolio.service;

import com.rickyyeung.profolio.Dto.LoginRespondDto;
import com.rickyyeung.profolio.Dto.UserDto;
import com.rickyyeung.profolio.config.AppConfiguration;
import com.rickyyeung.profolio.mapper.UserMapper;
import com.rickyyeung.profolio.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/** @noinspection ALL*/
@Service
public class AuthService {

    private final UserMapper userMapper;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppConfiguration appConfiguration;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    public AuthService(UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder, AppConfiguration appConfiguration
    ,EmailService emailService, StringRedisTemplate redisTemplate
    ) {
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.appConfiguration = appConfiguration;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    public User registerEmail (String email, String password, String displayName, String avatarUrl ) {
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email is Null or Empty");
        }

        if(password == null || password.isBlank()){
            throw new IllegalArgumentException("Email is Null or Empty");
        }

        if(displayName == null || displayName.isBlank()){
            throw new IllegalArgumentException("Display Name is Null  or Empty");
        }

        Optional<User> exitng = userMapper.findByEmail(email);
        if(exitng.isPresent()){
            throw new IllegalStateException("User Email Already Exited");
        }

        User user = new User();
        user.setEmail(email);
        String passwordHashed = bCryptPasswordEncoder.encode(password);
        user.setPasswordHash(passwordHashed);
        user.setDisplayName(displayName);
        user.setIsEmailVerified(false);

        //Save Token into Redis for checking
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("EMAIL_VERIFY"+token, email , Duration.ofMinutes(15));

        String verficationUrl = appConfiguration.getBackendDomain() + "/auth/verify?token=" + token;

        emailService.send(
                email,
                "[Verification Email] Please verify your email",
                "Hi " + displayName + ",\n\n This is a verification Email From Ricky Profolio, \n\n Please verify your email by clicking this link:\n" + verficationUrl
        );

        return user;
    }

    public void verifyEmail(String token) {
        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("Token is Null or Empty");
        }

        String email = redisTemplate.opsForValue().get("EMAIL_VERIFY"+token);

        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Invalid or Expired Token");
        }

        Optional<User> userOpt = userMapper.findByEmail(email);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            user.setIsEmailVerified(true);
            userMapper.updateUser(user);
        }else{
            throw new IllegalArgumentException("User is not found");
        }
    }

    public LoginRespondDto LoginEmail (String email, String password) {
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("email is Null or Empty");
        }

        if(password == null || password.isBlank()){
            throw new IllegalArgumentException("password is Null or Empty");
        }

        Optional<User> userOpt = userMapper.findByEmail(email);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            String passwordHashed = bCryptPasswordEncoder.encode(password);
            if(passwordHashed.equals(user.getPasswordHash())){
                UserDto userDto = new UserDto();
                BeanUtils.copyProperties(user,userDto);

                String token = "ttt";

                return new LoginRespondDto(userDto,token);
            }else{
                throw new IllegalArgumentException("Password is not matched");
            }

        }else{
            throw new IllegalArgumentException("User is not found");
        }
    }



}
