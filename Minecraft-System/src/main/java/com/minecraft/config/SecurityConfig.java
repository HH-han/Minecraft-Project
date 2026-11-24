package com.minecraft.config;

import com.minecraft.handler.JwtAccessDeniedHandler;
import com.minecraft.handler.JwtAuthenticationEntryPoint;
import com.minecraft.handler.JwtAuthenticationFilter;
import com.minecraft.service.UserPermissionService;
import com.minecraft.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    private final UserPermissionService userPermissionService;

    public SecurityConfig(JwtUtil jwtUtil, UserPermissionService userPermissionService) {
        this.jwtUtil = jwtUtil;
        this.userPermissionService = userPermissionService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userPermissionService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 管理端与系统管理接口：默认拒绝，必须登录（具体权限由 @PreAuthorize 校验）
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/api/system/**").authenticated()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/attraction/**").permitAll()
                .requestMatchers("/hotel/**").permitAll()
                .requestMatchers("/food/**").permitAll()
                .requestMatchers("/product/**").permitAll()
                .requestMatchers("/ticket/**").permitAll()
                .requestMatchers("/news/**").permitAll()
                .requestMatchers("/community/list").permitAll()
                .requestMatchers("/community/**").permitAll()
                .requestMatchers("/group/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/apiDocument.html").permitAll()
                .requestMatchers("/im-api-documentation.html").permitAll()
                .requestMatchers("/logs/**").permitAll()
                .requestMatchers("/api/user/info").authenticated()
                .requestMatchers("/upload/**").permitAll()
                .requestMatchers("/image/**").permitAll()
                .anyRequest().permitAll()
            )
            // 未认证（token 缺失/过期/无效）时统一返回 401 JSON，
            // 已登录但无权限时返回 403 JSON，供前端识别并跳转
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDeniedHandler())
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
