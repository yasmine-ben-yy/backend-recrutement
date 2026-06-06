// com.example.recrutement.config.SecurityConfig.java
package com.example.recrutement.config;

import com.example.recrutement.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.example.recrutement.auth.service.CustomOAuth2UserService;
import com.example.recrutement.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, authException) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Non autorisé\"}");
            }))
            .authorizeHttpRequests(auth -> auth
                // 🟢 Public complet
                .requestMatchers("/api/auth/**", "/oauth2/**", "/login/**").permitAll()
                .requestMatchers("/api/public/**", "/api/test/**", "/api/email-status/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/candidats/**", "/api/marque-blanche/**").permitAll()
                .requestMatchers("/error", "/api/auth/error").permitAll()
                .requestMatchers("/uploads/**", "/uploads/logos/**").permitAll()

                // 🟢 Endpoints candidats (authentification requise)
                .requestMatchers("/api/candidat/**", "/api/candidatures/**").authenticated()
                
                // 🟢 Endpoints interviews pour candidats
                .requestMatchers("/api/interviews/me").hasRole("CANDIDAT")
                .requestMatchers("/api/interviews/candidat/**").hasRole("CANDIDAT")
                .requestMatchers("/api/interviews/candidature/*/my").hasRole("CANDIDAT")
                .requestMatchers("/api/interviews/*/confirm").hasRole("CANDIDAT")
                
                // 🔵 Endpoints RH et ADMIN
                .requestMatchers("/api/interviews/**").hasAnyRole("RH", "ADMIN_MB")
                .requestMatchers("/api/dashboard/**").hasAnyRole("RH")
                .requestMatchers("/api/rh/**", "/api/matching/**", "/api/evaluations/**", "/api/ai/**").hasAnyRole("RH", "ADMIN_MB")
                .requestMatchers("/api/admin/domaines/**", "/api/admin/types-contrat/**").hasAnyRole("ADMIN_MB", "RH")
                
                // 🔴 ADMIN only
                .requestMatchers("/api/admin/users/**").hasRole("ADMIN_MB")
                
                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}