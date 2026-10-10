package com.barbergarage.security;

import com.barbergarage.entity.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;
    public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expirationMs){
        byte[] bytes=secret.getBytes(StandardCharsets.UTF_8);
        if(bytes.length<32) throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
        this.key=Keys.hmacShaKeyFor(bytes); this.expirationMs=expirationMs;
    }
    public String generate(UserAccount u){
        long now=System.currentTimeMillis();
        return Jwts.builder().subject(u.getUsername()).claim("uid",u.getId()).claim("role",u.getRole().name())
            .issuedAt(new Date(now)).expiration(new Date(now+expirationMs)).signWith(key).compact();
    }
    public String username(String token){ return parser(token).getPayload().getSubject(); }
    public boolean valid(String token){ try{ parser(token); return true; }catch(JwtException|IllegalArgumentException e){return false;} }
    private Jws<Claims> parser(String token){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(token); }
}
