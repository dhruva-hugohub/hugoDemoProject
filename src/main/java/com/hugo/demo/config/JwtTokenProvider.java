package com.hugo.demo.config;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long expirationTime;

    @Value("${security.jwt.authorities-key}")
    private String authoritiesKey;

    @Value("${security.jwt.ipAddress-key}")
    private String ipAddressKey;

    public String generateToken(Authentication authentication, String ipAddress) {

        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationTime);

        return Jwts.builder().subject(username).claim(ipAddressKey, ipAddress).claim(authoritiesKey, authorities).issuedAt(new Date()).expiration(expireDate)
            .signWith(secretKey(secretKey)).compact();
    }


    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getIpAddressFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get(ipAddressKey, String.class));
    }


    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey(secretKey)).build().parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public SecretKey secretKey(String secretKey) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public boolean validateToken(String token, String email, String clientIpAddress) {
        final String username = getUsernameFromToken(token);
        final String ipAddressFromToken = getIpAddressFromToken(token);
        return (username.equals(email) && !isTokenExpired(token) && clientIpAddress.equals(ipAddressFromToken));
    }

    public String generateSecretKey() {
        int length = 32;
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[length];
        secureRandom.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}
