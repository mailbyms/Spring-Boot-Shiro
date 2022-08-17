package com.gyjian.shiro.shiro;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {



    /**
     * JWT认证过期时间 EXPIRE_TIME ，单位毫秒
     * 本项目不检查 jwt 的过期时间。Redis 控制 token 的过期
     */
    private static final long EXPIRE_TIME = 24 * 3600 * 1000L;

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, String username, String secret) {
        try {
            //根据密码生成JWT效验器
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("username", username)
                    .build();
            //效验TOKEN
            DecodedJWT jwt = verifier.verify(token);
            System.out.println("登录认证成功!");
            return true;
        } catch (Exception exception) {
            System.out.println("JwtUtil登录认证失败!");
            return false;
        }
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 生成token签名EXPIRE_TIME 分钟后过期
     *
     * @param username 用户名(电话号码)
     * @param secret   用户的密码
     * @return 加密的token
     */
    public static String sign(String username, String secret) {
        long nowTime = System.currentTimeMillis();
        Date date = new Date(nowTime + EXPIRE_TIME);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 附带username信息
        return JWT.create()
                .withClaim("username", username)
                .withClaim("currentTimeMillis", String.valueOf(nowTime))
                .withExpiresAt(date)
                .sign(algorithm);
    }

    public static String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(claim).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}
