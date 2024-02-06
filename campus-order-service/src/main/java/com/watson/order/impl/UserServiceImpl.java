package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.UserService;
import com.watson.order.aop.UserLog;
import com.watson.order.dto.Result;
import com.watson.order.mapper.UserMapper;
import com.watson.order.po.User;
import com.watson.order.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 比较redis的验证码缓存，并根据前端传递的 email 查找用户，没有则创建用户
     *
     * @param map 封装 邮箱 及 验证码 的map
     * @return 登录成功返回token，失败返回错误信息
     */
    @UserLog
    @Override
    public String login(Map<String, String> map) {

        log.info("login user map: " + map.toString());

        // 获取邮箱地址
        String email = map.get("email");

        // 获取验证码
        String code = map.get("code");

        // 从redis中获取验证码
        String codeInRedis = redisTemplate.opsForValue().get(email);

        // 进行验证码的比对（页面提交的验证码和 redis 中保存的验证码比对成功）
        if (codeInRedis != null && codeInRedis.equals(code)) {
            // 如果能够比对成功，说明登录成功
            // 根据邮箱查找用户
            User user = this.lambdaQuery().eq(User::getEmail, email)
                    .one();
            // 判断当前邮箱对应的用户是否为新用户，如果是新用户就自动完成注册
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setStatus(1);
                this.save(user);
            }

            // 如果用户登录成功，直接删除缓存中的验证码
            redisTemplate.delete(email);

            // 自定义 JWT 载荷信息
            Map<String, String> claims = new HashMap<>();
            claims.put("id", String.valueOf(user.getId()));
            claims.put("email", user.getEmail());

            //使用JWT工具类，生成身份令牌
            String token = JwtUtils.getToken(claims);
            log.info("login token: {}", token);
            return token;
        }
        return "验证码错误，登录失败";
    }

    /**
     * 退出登录接口
     *
     * @param request 登出请求
     * @return 登出的用户id 或者 null
     */
    @UserLog
    @Override
    public Long logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            log.error("logout token is null");
            return null;
        }
        token = token.substring(7); // 去掉 "Bearer " 部分
        return JwtUtils.getIdFromToken(token);
    }
}
