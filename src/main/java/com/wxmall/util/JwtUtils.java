package com.wxmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT token
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * 密钥
     */
    @Value("${jwt.secret:wxmall_secret}")
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    /**
     * 签名算法
     */
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    
    /**
     * 安全密钥
     */
    private Key key;
    
    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        // 使用安全的密钥生成方法，确保密钥长度足够
        this.key = Keys.secretKeyFor(signatureAlgorithm);
        log.info("JWT密钥初始化完成");
    }

    /**
     * 生成token
     * @param id 用户ID
     * @return token字符串
     */
    public String generateToken(Long id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", id);
        return generateToken(claims);
    }

    /**
     * 从token中获取用户ID
     * @param token token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 验证token是否有效
     * @param token token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断token是否过期
     * @param token token字符串
     * @return 是否过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 从token中获取过期时间
     * @param token token字符串
     * @return 过期时间
     */
    private Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 从token中获取Claims
     * @param token token字符串
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成token
     * @param claims 数据声明
     * @return token字符串
     */
    private String generateToken(Map<String, Object> claims) {
        Date createdDate = new Date();
        Date expirationDate = new Date(createdDate.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }
}
