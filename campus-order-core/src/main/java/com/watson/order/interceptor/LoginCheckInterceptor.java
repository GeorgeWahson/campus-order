package com.watson.order.interceptor;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.Result;
import com.watson.order.exception.CustomException;
import com.watson.order.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        log.info("拦截到请求：{}", request.getRequestURI());

        // 判断用户设备
        String userAgent = request.getHeader("User-Agent");

        // 获取请求头中的令牌（token）
        String token = request.getHeader("Authorization");
        if (token != null) {
            token = token.substring(7); // 去掉 "Bearer " 部分
        }
        // log.info("从请求头中获取的令牌：{}", token);

        // 当请求根路径时,进行重定向和登录检查
        if ("/".equals(request.getRequestURI())) {
            String FRONT_INDEX = "/front/index.html";
            // 移动端，判断用户是否登录
            if (isMobile(userAgent)) {
                // 已登录则直接返回，不再处理后续请求
                if (isUserNotLogin(token)) {
                    writeNotLoginResponse(response);
                    response.sendRedirect(request.getContextPath() + FRONT_INDEX);
                } else {
                    // 已登录则直接返回，不再return处理后续请求
                    response.sendRedirect(request.getContextPath() + FRONT_INDEX);
                }
                return false;
            } else if (isPc(userAgent)) {
                String BACKEND_INDEX = "/backend/index.html";
                if (isUserNotLogin(token)) {
                    writeNotLoginResponse(response);
                    response.sendRedirect(request.getContextPath() + BACKEND_INDEX);
                } else {
                    // 已登录则直接返回，不再处理后续请求
                    response.sendRedirect(request.getContextPath() + BACKEND_INDEX);
                }
                return false;
            }
        }

        // 其他非根路径请求，仅检查用户是否已登录
        if (isUserNotLogin(token)) {
            writeNotLoginResponse(response);
            return false;
        }
        Long userId = JwtUtils.getIdFromToken(token);
        if (userId != null) {
            log.info("用户已登录，用户id: {}", userId);
            BaseContext.setCurrentId(userId);
            log.info("线程id为：{}", Thread.currentThread().getId());
        } else {
            throw new CustomException("jwt 解析异常");
        }

        return true;
    }

    private void writeNotLoginResponse(HttpServletResponse response) throws IOException {
        //设置响应头
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(Result.error("NOT_LOGIN")));
        log.info("NOT_LOGIN");
    }

    private boolean isPc(String userAgent) {
        return !userAgent.toLowerCase().matches(".*(mobile|android|iphone).*");
    }

    private boolean isMobile(String userAgent) {
        return userAgent.toLowerCase().matches(".*(mobile|android|iphone).*");
    }

    /**
     * 判断用户是否【未】登录
     *
     * @param token JWT token
     * @return true：未登录，false：已登录
     */
    private boolean isUserNotLogin(String token) {

        //4.判断令牌是否存在，如果不存在，返回错误结果（未登录）
        if (!StringUtils.hasLength(token)) {
            log.info("Token不存在");
            return true;
        }
        try {
            JwtUtils.getTokenInfo(token);
        } catch (Exception e) {
            log.info("令牌解析失败!");
            return true;
        }
        return false;
    }

}
