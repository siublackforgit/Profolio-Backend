package com.rickyyeung.profolio.controller;

import com.rickyyeung.profolio.Dto.LoginRespondDto;
import com.rickyyeung.profolio.Dto.TokenDtos;
import com.rickyyeung.profolio.Dto.UserDto;
import com.rickyyeung.profolio.model.User;
import com.rickyyeung.profolio.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/email")
    public ResponseEntity<String> registerEmail(@RequestParam String email,
                                                @RequestParam String password,
                                                @RequestParam String displayName,
                                                @RequestParam(required = false) String avatarUrl
    ) {
        try {
            authService.registerEmail(email, password, displayName);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully, Please check your email to verify");
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid input: "+ e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid input: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.debug("Registration failed:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            logger.debug("Server error, Please check server error log, error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error, Please check server error log");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token){
        try{
            authService.verifyEmail(token);
            return ResponseEntity.ok("Email Verified Successfully");
        }catch (IllegalArgumentException e){
            logger.debug("verifyEmail failed, error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            logger.debug("Server Error, error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }

    @PostMapping("/loginEmail")
    public ResponseEntity<?> loginEmail(@RequestBody Map<String, Object> payload, HttpServletRequest request){
        try{
            LoginRespondDto loginRespondDto = authService.LoginEmail(payload,request);

            ResponseCookie accessTokencookie = ResponseCookie.from("accessToken", loginRespondDto.getAccessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15))
                    .sameSite("None")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginRespondDto.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("None")
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokencookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(loginRespondDto.getUserDto());
        }catch (IllegalArgumentException e){
            logger.debug("loginEmail failed, Bad request error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            logger.debug("loginEmail failed, internel error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }

    @PostMapping("/getUserFromToken")
    public ResponseEntity<?> getUserFromToken (@RequestBody Map<String, Object> payload){
        try{

            String tempToken = (String) payload.get("tempToken");
            User user = authService.GetUserFromToken(tempToken);

            return ResponseEntity.ok().body(user);
        }catch (IllegalArgumentException e){
            logger.debug("bad request, error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            logger.debug("bad request, error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }

    @PostMapping("/logOut")
    public ResponseEntity<?> logOut() {
        try{
            ResponseCookie accessTokenCookie = ResponseCookie.from("acessToken")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken")
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth/refresh")
                    .maxAge(0)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString())
                    .body("Succeed LogOut");
        }catch (Exception e){
            logger.debug("bad request, error message:"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {

        try {
            LoginRespondDto loginRespondDto = authService.validateAndRefreshToken(request);

            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", loginRespondDto.getAccessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15).toMillis())
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginRespondDto.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth/refresh")
                    .maxAge(Duration.ofMinutes(15).toMillis())
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString())
                    .body(loginRespondDto.getUserDto());
            }catch (RuntimeException e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error, please check server log");
            }
    }
}
