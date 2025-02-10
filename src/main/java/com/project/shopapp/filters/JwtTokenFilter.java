package com.project.shopapp.filters;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String apiPrefix;

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            if (isBypass(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authorizationHeader.substring(7);
            final String username = jwtTokenUtils.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtils.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        // Luôn gọi filterChain.doFilter để tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }

    private boolean isBypass(@NonNull HttpServletRequest request) {
        final List<Map.Entry<String, String>> bypassToken = Arrays.asList(
                new AbstractMap.SimpleEntry<>(String.format("%s/products", apiPrefix), "GET"),
                new AbstractMap.SimpleEntry<>(String.format("%s/categories", apiPrefix), "GET"),
                new AbstractMap.SimpleEntry<>(String.format("%s/users/register", apiPrefix), "POST"),
                new AbstractMap.SimpleEntry<>(String.format("%s/users/login", apiPrefix), "POST")
        );

        for (final Map.Entry<String, String> token : bypassToken) {
            if (request.getServletPath().contains(token.getKey()) &&
                    request.getMethod().equals(token.getValue())) {
                return true;
            }
        }
        return false;
    }
}
