package com.foodtrace.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.foodtrace.vo.User;

import java.util.Date;


/**
 * token 工具类
 * 生成 与 检验
 */

public class TokenUtils {

    //token到期时间1小时
    private static final long EXPIRE_TIME = 1 * 60 * 60 * 1000;
    //密钥盐
    private static final String TOKEN_SECRET = "ljdyaishijin**3nkjnj??";

    /**
     * 生成 token
     *
     * @param user
     * @return
     */
    public static String sign(User user) {
        String token = null;
        try {
            Date expireAt = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            token = JWT.create()
                    //发行人
                    .withIssuer("auth0")
                    //存放数据
                    .withClaim("username", user.getUserName())
                    //过期时间
                    .withExpiresAt(expireAt)
                    .sign(Algorithm.HMAC256(TOKEN_SECRET));
        } catch (IllegalArgumentException | JWTCreationException je) {

        }
        return token;
    }


    /**
     * 验证 token
     *
     * @param token
     * @return
     */
    public static Boolean verify(String token) {

        try {
            //创建token验证器
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET)).withIssuer("auth0").build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
//            System.out.println("认证通过：");
//            System.out.println("username: " + decodedJWT.getClaim("username").asString());
//            System.out.println("过期时间：      " + decodedJWT.getExpiresAt());
        } catch (IllegalArgumentException | JWTVerificationException e) {
            //抛出错误即为验证不通过
            return false;
        }
        return true;
    }

    /**
     * 验证 token
     *
     * @param token
     * @return
     */
    public static Boolean tokenVerify(String token) {
        boolean tokenIsTrue;
        try {
            //创建token验证器
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET)).withIssuer("auth0").build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
//            System.out.println("认证通过：");
//            System.out.println("username: " + decodedJWT.getClaim("username").asString());
//            System.out.println("过期时间：      " + decodedJWT.getExpiresAt());
            tokenIsTrue = true;
        } catch (IllegalArgumentException | JWTVerificationException e) {
            //抛出错误即为验证不通过
            tokenIsTrue = false;
        }

        return tokenIsTrue;
    }

}