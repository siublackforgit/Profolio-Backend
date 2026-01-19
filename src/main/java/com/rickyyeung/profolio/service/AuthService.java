package com.rickyyeung.profolio.service;

import com.rickyyeung.profolio.Dto.LoginRespondDto;
import com.rickyyeung.profolio.Dto.TokenDtos;
import com.rickyyeung.profolio.Dto.UserDto;
import com.rickyyeung.profolio.config.AppConfiguration;
import com.rickyyeung.profolio.enums.UserRole;
import com.rickyyeung.profolio.mapper.LoginLogMapper;
import com.rickyyeung.profolio.mapper.UserMapper;
import com.rickyyeung.profolio.model.User;
import com.rickyyeung.profolio.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;

/** @noinspection ALL*/
@Service
@Slf4j
public class AuthService {

    private final UserMapper userMapper;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtils jwtUtils;
    private final AppConfiguration appConfiguration;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final LoginLogMapper loginLogMapper;

    public AuthService(UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder, AppConfiguration appConfiguration
    ,EmailService emailService, StringRedisTemplate redisTemplate, JwtUtils jwtUtils, LoginLogMapper loginLogMapper
    ) {
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.appConfiguration = appConfiguration;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
        this.loginLogMapper = loginLogMapper;
    }

    @Transactional
    public User registerEmail (String email, String password, String displayName) {
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email is Null or Empty");
        }

        if(password == null || password.isBlank()){
            throw new IllegalArgumentException("Password is Null or Empty");
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
        user.setCreatedBy(1);
        user.setLastUpdatedBy(1);
        user.setUserRole(UserRole.USER.getCode());

        //Save Token into Redis for checking
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("EMAIL_VERIFY"+token, email , Duration.ofMinutes(15));

        String verficationUrl = appConfiguration.getBackendDomain() + "/auth/verify?token=" + token;


        try {
            userMapper.insertUser(user);
            emailService.send(
                    email,
                    "[Verification Email] Please verify your email",
                    "Hi " + displayName + ",\n\n This is a verification Email From Ricky Profolio, \n\n Please verify your email by clicking this link:\n" + verficationUrl
            );
            return user;
        } catch (Exception e) {
            redisTemplate.delete("EMAIL_VERIFY" + token);
            log.error("error - Email: {}, Failure reason: {}", email, e.getMessage(), e);
            throw new RuntimeException("Email sending failed, rolling back.");
        }

    }

    @Transactional
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
            Long userId = user.getUserId();
            if(userId == null){
                throw new IllegalArgumentException("User Id cannot be null");
            }
            try {
                userMapper.updateEmailVerifiedStatus(userId, true, 1);
            } catch (Exception e) {
                log.error("verifyEmail Failed - Email: {}, Failure reason: {}", email, e.getMessage(), e);
            }
        }else{
            throw new IllegalArgumentException("User is not found");
        }
    }

    @Transactional
    public User GetUserFromToken (String tempToken) {

        if(tempToken == null || tempToken.isBlank()){
            throw new IllegalArgumentException("tempToken is Null or Empty");
        }

        String email = redisTemplate.opsForValue().get(tempToken);

        if(email == null || email.isBlank() ){
            throw new IllegalArgumentException("email is null or token is expired");
        }

        Optional<User> userOpt = userMapper.findByEmail(email);
        if(userOpt.isEmpty()){
            throw new IllegalStateException("User is null");
        }

        User user = userOpt.get();

        return user;
    }

    public LoginRespondDto LoginEmail (Map<String, Object> payload, HttpServletRequest request) {

        String email = (String) payload.get("email");

        String password = (String) payload.get("password");

        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("email is Null or Empty");
        }

        if(password == null || password.isBlank()){
            throw new IllegalArgumentException("password is Null or Empty");
        }

        Optional<User> userOpt = userMapper.findByEmail(email);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            if(bCryptPasswordEncoder.matches(password, user.getPasswordHash())){
                //User account is not verified, send another email
                if(user.getIsEmailVerified() == false){
                    //Save Token into Redis for checking
                    String token = UUID.randomUUID().toString();
                    redisTemplate.opsForValue().set("EMAIL_VERIFY"+token, email , Duration.ofMinutes(15));
                    String verficationUrl = appConfiguration.getBackendDomain() + "/auth/verify?token=" + token;
                    try {
                        userMapper.insertUser(user);
                        emailService.send(
                                email,
                                "[Verification Email] Please verify your email",
                                "Hi " + user.getDisplayName() + ",\n\n This is a verification Email From Ricky Profolio, \n\n Please verify your email by clicking this link:\n" + verficationUrl
                        );
                        throw new IllegalStateException("The Account is unverified, a email has been sent to your email, please verify your account");
                    } catch (Exception e) {
                        redisTemplate.delete("EMAIL_VERIFY" + token);
                        log.error("LoginEmail Failed - Email: {}, Failure reason: {}", email, e.getMessage(), e);
                        throw new RuntimeException("A verification Email sent failed, rolling back.");
                    }
                }

                //User is verified
                UserDto userDto = new UserDto();
                BeanUtils.copyProperties(user,userDto);
                String accesstoken = jwtUtils.generateToken(user);
                String refreshToken = UUID.randomUUID().toString();

                //prefix:userId
                redisTemplate.opsForValue().set("refreshToken:"+user.getUserId(),refreshToken,Duration.ofDays(2));

                //LoginLog insert
                String ip = request.getRemoteAddr();
                loginLogMapper.insertLoginLog(userDto.getUserId(), ip);

                return new LoginRespondDto(userDto,accesstoken,refreshToken);
            }else{
                log.error("Password is not matched");
                throw new IllegalArgumentException("Password is not matched");
            }

        }else{
            throw new IllegalArgumentException("User is not found");
        }
    }

    public LoginRespondDto validateAndRefreshToken(HttpServletRequest request){
        Cookie[] cookies  = request.getCookies();
        if(cookies == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No cookies found");
        }

        String refreshTokenFromCookie = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow( () ->  new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token is missing")
       );

        String accessTokenFromCookie = Arrays.stream(cookies)
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "accessToken Token is missing")
                );

        Long userId = jwtUtils.getUserIdFromToken(accessTokenFromCookie);

        if(userId == null){
            log.error("user is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user is null");
        }

        String redisKey = "refreshToken:"+userId;
        String storedRefreshToken = (String) redisTemplate.opsForValue().get(redisKey);

        if(!redisTemplate.hasKey(redisKey)){
            log.error("Refresh Token has expired or session invalid");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token has expired or session invalid");
        }

        if(storedRefreshToken == null || !storedRefreshToken.equals(refreshTokenFromCookie)){
            log.error("Cannot Find RefreshToken");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot Find RefreshToken");

        }

        Optional<User> user = userMapper.findByUserId(userId);

        if(user.isEmpty()){
            log.error("user is null");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user is null");
        }


        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user.get(),userDto);


        String accessToken = jwtUtils.generateToken(user.get());
        String refreshToken = UUID.randomUUID().toString();

        //Insert Login Record
        String ip = request.getRemoteAddr();
        loginLogMapper.insertLoginLog(userDto.getUserId(), ip);

        return new LoginRespondDto(userDto,accessToken,refreshToken);
    }

}
