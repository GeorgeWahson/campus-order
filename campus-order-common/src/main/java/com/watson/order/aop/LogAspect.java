package com.watson.order.aop;

import com.alibaba.fastjson2.JSONObject;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.Result;
import com.watson.order.log.EmpOperateLog;
import com.watson.order.log.UserOperateLog;
import com.watson.order.mapper.EmpOperateLogMapper;
import com.watson.order.mapper.UserOperateLogMapper;
import com.watson.order.po.Employee;
import com.watson.order.po.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final EmpOperateLogMapper empOperateLogMapper;

    private final UserOperateLogMapper userOperateLogMapper;

    @Around("@annotation(com.watson.order.aop.EmpLog) || @annotation(com.watson.order.aop.UserLog)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {

        // 操作人员 id
        Long operateUser = BaseContext.getCurrentId();

        // 操作时间
        LocalDateTime operateTime = LocalDateTime.now();

        // 操作类名
        String className = joinPoint.getTarget().getClass().getName();

        // 操作方法名
        String methodName = joinPoint.getSignature().getName();

        // 操作方法参数
        Object[] args = joinPoint.getArgs();
        String methodParams = Arrays.toString(args);

        long begin = System.currentTimeMillis();

        try {
            //调用原始目标方法运行
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();

            // 方法返回值
            String returnValue = JSONObject.toJSONString(result);

            // 操作耗时
            Long costTime = end - begin;

            // 获取被拦截方法的Method对象
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            // 检查方法上是包含 @Log 或 @MyLog 注解
            EmpLog empLogAnnotation = method.getAnnotation(EmpLog.class);
            UserLog userLogAnnotation = method.getAnnotation(UserLog.class);
            if (empLogAnnotation != null) {
                // 执行 @EmpLog 注解触发的操作
                // 登录方法 操作人员id 在结果中获取
                if ("login".equals(methodName)) {
                    Result<Employee> emp = (Result<Employee>) result;
                    operateUser = emp.getData().getId();
                }
                // 记录操作日志
                EmpOperateLog empOperateLog = new EmpOperateLog(null, operateUser, operateTime, className, methodName, methodParams, returnValue, costTime);
                empOperateLogMapper.insert(empOperateLog);
                log.info("@EmpLog 注解，AOP记录操作日志: {}", empOperateLog);
            } else if (userLogAnnotation != null) {
                // 执行 @UserLog 注解触发的操作
                // 登录方法 登录用户id 在结果中获取
                if ("login".equals(methodName)) {
                    Result<User> emp = (Result<User>) result;
                    operateUser = emp.getData().getId();
                } else if ("sendMsg".equals(methodName)) {  // 发送验证码时，用户未登录，此时操作人员id为空
                    operateUser = null;
                }
                // 记录操作日志
                UserOperateLog userOperateLog = new UserOperateLog(null, operateUser, operateTime, className, methodName, methodParams, returnValue, costTime);
                userOperateLogMapper.insert(userOperateLog);
                log.info("@UserLog 注解，AOP记录操作日志: {}", userOperateLog);
            } else {
                // 如果没有找到这两个注解，可以抛出异常或者记录错误信息
                throw new IllegalStateException("Expected either @Log or @MyLog on the intercepted method.");
            }
            return result;
        } catch (Exception e) {
            // 记录异常日志
            String errorMessage = String.format("【异常日志】方法：%s, 参数：%s, 异常信息：%s%n", methodName, methodParams, e.getMessage());
            UserOperateLog userOperateLog = new UserOperateLog(null, operateUser, operateTime, className, methodName, methodParams, errorMessage, null);
            userOperateLogMapper.insert(userOperateLog);
            log.error("异常日志userOperateLog对象: {}", userOperateLog, e);
            throw new RuntimeException(e);
        }

    }

}
