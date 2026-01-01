package com.rickyyeung.profolio.controller;

import com.rickyyeung.profolio.Dto.LoginRespondDto;
import com.rickyyeung.profolio.Dto.UserDto;
import com.rickyyeung.profolio.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                    .body("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid input: " + e.getMessage());
        } catch (IllegalStateException e) {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }

    @PostMapping("/loginEmail")
    public ResponseEntity<?> loginEmail(@RequestParam String email, @RequestParam String password){
        try{
            LoginRespondDto loginRespondDto = authService.LoginEmail(email,password);
            return ResponseEntity.ok(loginRespondDto);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }
}
