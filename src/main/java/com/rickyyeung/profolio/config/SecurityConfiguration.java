package com.rickyyeung.profolio.config;

import com.rickyyeung.profolio.filter.JwtAuthenticationFilter;
import com.rickyyeung.profolio.mapper.UserMapper;
import com.rickyyeung.profolio.model.User;
import com.rickyyeung.profolio.util.JwtUtils;
import com.rickyyeung.profolio.util.RandomTokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${app.backend.domain}")
    private String backendDomain;

    @Value("${app.frontend.domain}")
    private String frontendDomain;

    private final JwtUtils jwtUtils;
    private final RandomTokenUtils randomTokenUtils;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public SecurityConfiguration(JwtUtils jwtUtils,
                                 UserMapper userMapper,
                                 RandomTokenUtils randomTokenUtils,
                                 StringRedisTemplate redisTemplate
                                 ) {
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.randomTokenUtils = randomTokenUtils;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
          http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable() )
                  .authorizeHttpRequests(auth -> auth
                          .requestMatchers("/auth/**", "/oauth2/**","/auth/refresh").permitAll()
                          .anyRequest().authenticated()
                  )
                  .oauth2Login(oauth2 -> oauth2
                          .authorizationEndpoint(authorization -> authorization
                                  .baseUri("/oauth2/authorization")
                                  .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                          )
                          .redirectionEndpoint(redirection -> redirection
                                  .baseUri("/login/oauth2/code/*")
                          )
                          .successHandler(oAuth2AuthenticationSuccessHandler())
                  ).addFilterBefore(new JwtAuthenticationFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);

          return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000","https://www.rickyprofolio.com","http://127.0.0.1:3000"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public BCryptPasswordEncoder  passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(){
        return (request, response, authentication) -> {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();

            String email = oidcUser.getEmail();
            String googleName = oidcUser.getFullName();
            String googleId = oidcUser.getSubject();
            String avaterUrl = oidcUser.getPicture();

            Optional<User> exiting = userMapper.findByEmail(email);
            User user = userMapper.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                newUser.setDisplayName(googleName);
                newUser.setAvatarUrl(avaterUrl);
                newUser.setIsEmailVerified(true);
                newUser.setCreatedBy(1);
                newUser.setLastUpdatedBy(1);
                userMapper.insertUser(newUser);
                return newUser;
            });

            String accesstoken = jwtUtils.generateToken(user);
            String refreshtoken = UUID.randomUUID().toString();


            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accesstoken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(3600)
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshtoken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth/refresh")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Lax")
                    .build();


            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            String tempToken = randomTokenUtils.generateSecureToken();

            redisTemplate.opsForValue().set(tempToken,user.getEmail(), Duration.ofMinutes(1));

            response.sendRedirect(frontendDomain + "/auth?tempToken=" + tempToken);
        };

    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }
}
