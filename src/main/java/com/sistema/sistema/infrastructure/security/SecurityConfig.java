package com.sistema.sistema.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sales/*/cancel").hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers("/api/reports/**").hasAuthority("ROLE_SUPER_ADMIN")
                        // NOTIFICATIONS
                        .requestMatchers("/api/notifications/**").hasAuthority("ROLE_SUPER_ADMIN")
                        //PARAMETER
                        .requestMatchers(HttpMethod.POST, "/api/parameter/create").hasAuthority("CREATE_PARAMETER")
                        .requestMatchers(HttpMethod.PUT, "/api/parameter/update").hasAuthority("EDIT_PARAMETER")
                        .requestMatchers(HttpMethod.PUT, "/api/parameter/update-status").hasAuthority("EDIT_PARAMETER")
                        .requestMatchers(HttpMethod.DELETE, "/api/parameter/delete/**").hasAuthority("DELETE_PARAMETER")
                        .requestMatchers(HttpMethod.POST, "/api/parameter/init").hasAuthority("VIEW_PARAMETER")
                        .requestMatchers(HttpMethod.POST, "/api/parameter/init-form").hasAnyAuthority("CREATE_PARAMETER", "EDIT_PARAMETER")
                        .requestMatchers(HttpMethod.POST, "/api/parameter/export-pdf").hasAuthority("EXPORT_PARAMETER")
                        .requestMatchers(HttpMethod.POST, "/api/parameter/export-excel").hasAuthority("EXPORT_PARAMETER")
                        //USER
                        .requestMatchers(HttpMethod.POST, "/api/user/create").hasAuthority("CREATE_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/user/update").hasAuthority("EDIT_USER")
                        .requestMatchers(HttpMethod.PUT, "/api/user/update-status").hasAuthority("EDIT_USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/user/delete/**").hasAuthority("DELETE_USER")
                        .requestMatchers(HttpMethod.POST, "/api/user/init").hasAuthority("VIEW_USER")
                        .requestMatchers(HttpMethod.POST, "/api/user/init-form").hasAnyAuthority("CREATE_USER", "EDIT_USER")
                        .requestMatchers(HttpMethod.POST, "/api/user/export-pdf").hasAuthority("EXPORT_USER")
                        .requestMatchers(HttpMethod.POST, "/api/user/export-excel").hasAuthority("EXPORT_USER")
                        // PRODUCT
                        .requestMatchers(HttpMethod.POST, "/api/product/init").hasAuthority("VIEW_PRODUCT")
                        .requestMatchers(HttpMethod.POST, "/api/product/init-form").hasAnyAuthority("CREATE_PRODUCT", "EDIT_PRODUCT")
                        .requestMatchers(HttpMethod.POST, "/api/product/create").hasAuthority("CREATE_PRODUCT")
                        .requestMatchers(HttpMethod.PUT, "/api/product/update").hasAuthority("EDIT_PRODUCT")
                        .requestMatchers(HttpMethod.PUT, "/api/product/update-status").hasAuthority("EDIT_PRODUCT")
                        .requestMatchers(HttpMethod.DELETE, "/api/product/delete/**").hasAuthority("DELETE_PRODUCT")
                        .requestMatchers(HttpMethod.POST, "/api/product/export-pdf").hasAuthority("EXPORT_PRODUCT")
                        .requestMatchers(HttpMethod.POST, "/api/product/export-excel").hasAuthority("EXPORT_PRODUCT")
                        //ROLE
                        .requestMatchers(HttpMethod.POST, "/api/role/create").hasAuthority("CREATE_ROLE")
                        .requestMatchers(HttpMethod.PUT, "/api/role/update").hasAuthority("EDIT_ROLE")
                        .requestMatchers(HttpMethod.PUT, "/api/role/update-status").hasAuthority("EDIT_ROLE")
                        .requestMatchers(HttpMethod.DELETE, "/api/role/delete/**").hasAuthority("DELETE_ROLE")
                        .requestMatchers(HttpMethod.POST, "/api/role/init").hasAuthority("VIEW_ROLE")
                        .requestMatchers(HttpMethod.POST, "/api/role/export-pdf").hasAuthority("EXPORT_ROLE")
                        .requestMatchers(HttpMethod.POST, "/api/role/export-excel").hasAuthority("EXPORT_ROLE")
                        //PERMISSION
                        .requestMatchers(HttpMethod.GET, "/api/permission/get-by-role/**").hasAuthority("VIEW_PERMISSION")
                        .requestMatchers(HttpMethod.PUT, "/api/permission/update-permission-by-role").hasAuthority("ASSIGN_PERMISSION")
                        //PROFILE
                        .requestMatchers(HttpMethod.GET, "/api/profile/init").hasAuthority("VIEW_PROFILE")
                        .requestMatchers(HttpMethod.GET, "/api/profile/init-form").hasAuthority("UPDATE_PROFILE")
                        .requestMatchers(HttpMethod.PUT, "/api/profile/update").hasAuthority("UPDATE_PROFILE")
                        .requestMatchers(HttpMethod.PUT, "/api/profile/update-password").hasAuthority("UPDATE_PROFILE")
                        .requestMatchers(HttpMethod.PUT, "/api/profile/disable-account").hasAuthority("UPDATE_PROFILE")
                        .requestMatchers(HttpMethod.PUT, "/api/profile/upload-avatar").hasAuthority("UPDATE_PROFILE")
                        .requestMatchers(HttpMethod.PUT, "/api/profile/delete-avatar").hasAuthority("UPDATE_PROFILE")
                        .requestMatchers(HttpMethod.GET, "/api/profile/permissions").hasAuthority("PERMISSION_PROFILE")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter(),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost:5173",
                "http://127.0.0.1:4200",
                "http://192.168.1.216:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
