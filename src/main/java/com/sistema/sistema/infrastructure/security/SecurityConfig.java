package com.sistema.sistema.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Permitir CORS completamente
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Desactivar CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ===============================
                        //  ARCHIVOS Y RUTAS PÚBLICAS
                        // ===============================
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico",
                                "/assets/**", "/static/**",
                                "/*.js", "/*.css", "/*.json", "/*.png", "/*.jpg"
                        ).permitAll()

                        // ===============================
                        //  PERMITIR TODOS LOS OPTIONS
                        // ===============================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ===============================
                        //  LOGIN PÚBLICO
                        // ===============================
                        .requestMatchers("/api/login").permitAll()

                        // ===============================
                        //  APIs protegidas
                        // ===============================
                        .requestMatchers("/api/**").authenticated()

                        // ===============================
                        //  Todo lo demás público
                        // ===============================
                        .anyRequest().permitAll()
                );

        // Filtro JWT SOLO para /api/**
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================
    //  CORS GLOBAL
    // ================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // Permite todo (Angular + Render)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
