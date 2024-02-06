package com.watson.order.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Map;

// java-jwt
@Slf4j
public class JwtUtils {
    private static final String SIGN_KEY = "GeorgeWatson";//签名密钥

    /**
     * 生成token
     *
     * @param map 封装参数
     * @return token
     */
    public static String getToken(Map<String, String> map) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 7);

        JWTCreator.Builder builder = JWT.create();

        // payload
        map.forEach(builder::withClaim);

        return builder.withExpiresAt(instance.getTime()).sign(Algorithm.HMAC256(SIGN_KEY));
    }


    /**
     * token解析
     *
     * @param token jwt token
     * @return token信息
     */
    public static DecodedJWT getTokenInfo(String token) {
        return JWT.require(Algorithm.HMAC256(SIGN_KEY)).build().verify(token);
    }

    /**
     * 从token中获取id
     *
     * @param token jwt token
     * @return id
     */
    public static Long getIdFromToken(String token) {
        long id;
        try {
            DecodedJWT tokenInfo = getTokenInfo(token);
            // 不加 .asString()，结果会被""包住。
            id = Long.parseLong(tokenInfo.getClaim("id").asString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        return id;
    }
}
