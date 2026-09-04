package com.routesync.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT ka actual implementation yahan hoga.
 *
 * Is class ka kaam:
 * - JWT generate karna
 * - JWT parse karna
 * - JWT validate karna
 * - JWT expiry check karna
 */
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;

    private final long accessTokenExpiration;

    private final long refreshTokenExpiration;

    public JwtServiceImpl(
            @Value("${routesync.jwt.secret}") String secret,
            @Value("${routesync.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${routesync.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {

        /*
         * Secret ko HMAC-SHA key mein convert kar rahe hain.
         *
         * UTF-8 string ko directly signing key ki tarah use karne ke bajay
         * Keys.hmacShaKeyFor() use karna safer approach hai.
         */
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {

        return generateAccessToken(
                Map.of(),
                userDetails
        );
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + refreshTokenExpiration
        );

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("tokenType", "refresh")
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("tokenType", "access")
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    @Override
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        try {

            String username = extractUsername(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (Exception exception) {

            /*
             * Invalid signature, malformed token,
             * expired token etc. yahan handle honge.
             */
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    @Override
    public long getAccessTokenExpiration() {

        return accessTokenExpiration;
    }

    /**
     * JWT se kisi bhi claim ko extract karne ke liye generic method.
     */
    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * JWT ka expiration claim extract karta hai.
     */
    private Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    /**
     * JWT ko secret key ke saath parse karta hai.
     *
     * Agar signature invalid hai ya token malformed hai,
     * JJWT exception throw karega.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}