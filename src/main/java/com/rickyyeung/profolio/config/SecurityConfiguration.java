package com.rickyyeung.profolio.config;

import com.rickyyeung.profolio.filter.JwtAuthenticationFilter;
import com.rickyyeung.profolio.util.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtUtils jwtUtils;
    public SecurityConfiguration(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
          http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable() )
                  .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**").permitAll().anyRequest().authenticated()).addFilterBefore(new JwtAuthenticationFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);

          return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000","https://www.rickyprofolio.com"));

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
}
