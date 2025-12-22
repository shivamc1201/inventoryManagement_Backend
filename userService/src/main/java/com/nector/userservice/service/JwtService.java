package com.nector.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret:mySecretKey}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours
    private Long expiration;
    
    public String extractUsername(String token) {
        log.debug("Entering extractUsername()");
        String username = extractClaim(token, Claims::getSubject);
        log.debug("Exiting extractUsername() for user: {}", username);
        return username;
    }
    
    public Date extractExpiration(String token) {
        log.debug("Entering extractExpiration()");
        Date expiration = extractClaim(token, Claims::getExpiration);
        log.debug("Exiting extractExpiration() with date: {}", expiration);
        return expiration;
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Entering extractClaim()");
        final Claims claims = extractAllClaims(token);
        T result = claimsResolver.apply(claims);
        log.debug("Exiting extractClaim()");
        return result;
    }
    
    private Claims extractAllClaims(String token) {
        log.debug("Entering extractAllClaims()");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        log.debug("Exiting extractAllClaims()");
        return claims;
    }
    
    private Boolean isTokenExpired(String token) {
        log.debug("Entering isTokenExpired()");
        Boolean expired = extractExpiration(token).before(new Date());
        log.debug("Exiting isTokenExpired() with result: {}", expired);
        return expired;
    }
    
    public String generateToken(UserDetails userDetails) {
        log.info("Entering generateToken() for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername());
        log.info("Exiting generateToken() - Token generated for user: {}", userDetails.getUsername());
        return token;
    }
    
    public String generateToken(String username) {
        log.info("Entering generateToken() for username: {}", username);
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, username);
        log.info("Exiting generateToken() - Token generated for username: {}", username);
        return token;
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        log.debug("Entering createToken() for subject: {}", subject);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
        log.debug("Exiting createToken() for subject: {}", subject);
        return token;
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        log.info("Entering validateToken() for user: {}", userDetails.getUsername());
        final String username = extractUsername(token);
        Boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        log.info("Exiting validateToken() with result: {} for user: {}", isValid, userDetails.getUsername());
        return isValid;
    }
    
    private Key getSignKey() {
        log.debug("Entering getSignKey()");
        byte[] keyBytes = secret.getBytes();
        Key key = Keys.hmacShaKeyFor(keyBytes);
        log.debug("Exiting getSignKey()");
        return key;
    }
}