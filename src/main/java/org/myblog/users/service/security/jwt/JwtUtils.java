package org.myblog.users.service.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.Logger;
import org.myblog.users.model.JwtInfo;
import org.myblog.users.model.JwtUserInfo;
import org.myblog.users.service.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    @Autowired
    private Logger logger;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${auth.app.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication, List<String> roles) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String userPrincipalString = "";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            userPrincipalString = objectMapper.writeValueAsString(new JwtUserInfo(userPrincipal));
        } catch (Exception e) {
            return "";
        }

        Claims claims = Jwts.claims().setSubject(userPrincipal.getUsername());
        claims.put("user", userPrincipalString);

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public JwtInfo getUserDataFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();

        JwtInfo jwtInfo = new JwtInfo();
        try {
            jwtInfo.setUserInfo(objectMapper.readValue(claims.get("user", String.class), JwtUserInfo.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        jwtInfo.setExpiration(claims.getExpiration());

        return jwtInfo;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
