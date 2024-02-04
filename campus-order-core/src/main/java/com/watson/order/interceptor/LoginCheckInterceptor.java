package com.watson.order.interceptor;

import com.alibaba.fastjson2.JSON;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

//    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 网页管理端 和 移动端 理论上不会同时登录。否则一端登录后另一端无需登录
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        //1、获取本次请求的URI
//        String requestURI = request.getRequestURI();
//        log.info("拦截到请求：requestURI：" + requestURI);
//
//        if ("/".equals(requestURI)) {
//            log.info("访问根路径");
//            response.sendRedirect("/backend/index.html");
//        }
//
//        // 4-1、判断【后端】登录状态，如果已登录，则直接放行
//        if (request.getSession().getAttribute("employee") != null) {
//            log.info("用户已登录，用户id:{}", request.getSession().getAttribute("employee"));
//
//            Long empId = (Long) request.getSession().getAttribute("employee");
//            BaseContext.setCurrentId(empId);
//
//            log.info("线程id为：{}", Thread.currentThread().getId());
//            return true;
//        }
//
//
//        //4-2、判断 【移动端】 登录状态，如果已登录，则直接放行
//        if (request.getSession().getAttribute("user") != null) {
//            log.info("用户已登录，用户id:{}", request.getSession().getAttribute("user"));
//
//            Long userId = (Long) request.getSession().getAttribute("user");
//            BaseContext.setCurrentId(userId);
//
//            log.info("线程id为：{}", Thread.currentThread().getId());
//            return true;
//        }
//
//        String userAgent = request.getHeader("User-Agent");
//        log.info(userAgent);
//        if (userAgent != null) {
//            String[] userAgentParts = userAgent.toLowerCase().split(" ");
//            if (userAgentParts[0].contains("android") || userAgentParts[0].contains("iphone") || userAgentParts[0].contains("ipad")) {
//                // 如果是手机访问，重定向到/front/index.html
//                response.getWriter().write(JSON.toJSONString(Result.error("NOT_LOGIN")));
//                log.info("mobile, NOT_LOGIN");
//                response.sendRedirect("/front/index.html");
//                return false; // 返回false表示请求已经处理，不再继续向下执行
//            } else {
//                // 如果是电脑访问，重定向到/backend/index.html
//                response.sendRedirect("/backend/index.html");
//                response.getWriter().write(JSON.toJSONString(Result.error("NOT_LOGIN")));
//                log.info("PC NOT_LOGIN");
//                return false; // 返回false表示请求已经处理，不再继续向下执行
//            }
//        }
//
//        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
//        response.getWriter().write(JSON.toJSONString(Result.error("NOT_LOGIN")));
//        log.info("NOT_LOGIN");
//        return false;
//    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,  @NonNull Object handler) throws Exception {

        // 判断用户设备
        String userAgent = request.getHeader("User-Agent");

        log.info("拦截到请求：{}", request.getRequestURI());

        // 当请求根路径时,进行重定向和登录检查
        if ("/".equals(request.getRequestURI())) {
            String FRONT_INDEX = "/front/index.html";
            // 移动端，判断用户是否登录
            if (isMobile(userAgent)) {
                log.info("isMobile");
                // 已登录则直接返回，不再处理后续请求
                if (isUserNotLogin(request, "user")) {
                    writeNotLoginResponse(response);
                    response.sendRedirect(request.getContextPath() + FRONT_INDEX);
                } else {
                    // 已登录则直接返回，不再return处理后续请求
                    response.sendRedirect(request.getContextPath() + FRONT_INDEX);
                }
                return false;
            } else if (isPc(userAgent)) {
                log.info("isPc");
                String BACKEND_INDEX = "/backend/index.html";
                if (isUserNotLogin(request, "employee")) {
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
        if (isPc(userAgent)) {
            if (isUserNotLogin(request, "employee")) {
                writeNotLoginResponse(response);
                return false;
            }
            // 设置用户id
            log.info("用户已登录，用户id:{}", request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            log.info("线程id为：{}", Thread.currentThread().getId());
        } else if (isMobile(userAgent)) {
            if (isUserNotLogin(request, "user")) {
                writeNotLoginResponse(response);
                return false;
            }
            // 设置用户id
            log.info("用户已登录，用户id:{}", request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            log.info("线程id为：{}", Thread.currentThread().getId());
        }

        return true;
    }

    private void writeNotLoginResponse(HttpServletResponse response) throws IOException {
        response.getWriter().write(JSON.toJSONString(Result.error("NOT_LOGIN")));
        log.info("NOT_LOGIN");
    }

    private boolean isPc(String userAgent) {
        return !userAgent.toLowerCase().matches(".*(mobile|android|iphone).*");
    }

    private boolean isMobile(String userAgent) {
        return userAgent.toLowerCase().matches(".*(mobile|android|iphone).*");
    }

    private boolean isUserNotLogin(HttpServletRequest request, String attributeName) {
        return request.getSession().getAttribute(attributeName) == null;
    }

}
