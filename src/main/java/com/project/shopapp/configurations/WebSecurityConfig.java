package com.project.shopapp.configurations;

import com.project.shopapp.filters.JwtTokenFilter;
import com.project.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Value("${api.prefix}")
    private String prefix;

    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    // Allow all origins – adjust this in production for security
                    corsConfiguration.setAllowedOrigins(List.of("*"));
                    // Allow standard HTTP methods
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    // Allow specific headers
                    corsConfiguration.setAllowedHeaders(List.of("authorization", "content-type", "x-auth-token"));
                    // Expose headers to the client
                    corsConfiguration.setExposedHeaders(List.of("x-auth-token"));

                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", corsConfiguration);
                    cors.configurationSource(source);
                })

                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(authorize -> authorize
                        // Permit access to register and login endpoints
                        .requestMatchers(
                                String.format("%s/users/register", prefix),
                                String.format("%s/users/login", prefix)
                        ).permitAll()

                        .requestMatchers(POST, String.format("%s/orders/**", prefix))
                        .hasRole(Role.USER)

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
