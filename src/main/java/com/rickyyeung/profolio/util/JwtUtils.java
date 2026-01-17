package com.rickyyeung.profolio.util;

import com.rickyyeung.profolio.enums.UserRole;
import com.rickyyeung.profolio.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String secretString;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.accessTokenTtlSeconds}")
    private Long ttlSeconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();


        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuer(issuer)
                .claim("role",UserRole.fromCode(user.getUserRole()))
                .issuedAt(new Date(now))
                .expiration(new Date(now + Duration.ofMinutes(15).toMillis()))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}
