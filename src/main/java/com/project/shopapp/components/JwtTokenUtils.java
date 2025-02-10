package com.project.shopapp.components;

import com.project.shopapp.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {

    @Value("${jwt.expiration}")
    private int expirationTime;

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
//        this.generatePrivateKey();
        claims.put("phoneNumber", user.getPhoneNumber());
        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000L)) //nay de miliseccond
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Key getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey); //Keys.hmacShaKeyFor(bytes)
        return Keys.hmacShaKeyFor(bytes);
    }

    private String generatePrivateKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        String secretKey = Encoders.BASE64.encode(keyBytes);
        return secretKey;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey()) // Phải set signing key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //check token da het han chua
    private boolean isTokenExpired(String token) {
        Date expirationDate = (Date) this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = this.extractUsername(token);
        return (username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token));
    }

}
