package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.aop.EmpLog;
import com.watson.order.dto.PageBean;
import com.watson.order.mapper.EmployeeMapper;
import com.watson.order.EmployeeService;
import com.watson.order.po.Employee;
import com.watson.order.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    /**
     * 登录接口
     *
     * @param e 封装用户账号及密码的用户对象
     * @return token 或者 错误信息
     */
    @Override
    @EmpLog
    public String login(Employee e) {

        // 将页面提交的密码password进行md5加密处理，再与数据库数据比对
        String password = e.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        Employee loginEmp = lambdaQuery()
                .eq(Employee::getUsername, e.getUsername())
                .eq(Employee::getPassword, password)
                .one();

        // 如果没有查询到则返回登录失败结果
        if (loginEmp == null) {
            // 用户不存在，或者密码错误
            return "用户名或密码错误!!";
        }

        // 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (loginEmp.getStatus() == 0) {
            return "账号已禁用!";
        }

        // 自定义 JWT 载荷信息
        Map<String, String> claims = new HashMap<>();
        claims.put("id", String.valueOf(loginEmp.getId()));

        // Base64工具类进行编码，防止js解码中文乱码
        try {
            String encodedUrlPart = URLEncoder.encode(loginEmp.getName(), StandardCharsets.UTF_8.name());
            claims.put("name", encodedUrlPart);
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception);
        }

        claims.put("username", loginEmp.getUsername());

        //使用JWT工具类，生成身份令牌
        String token = JwtUtils.getToken(claims);
        log.info("login token: {}", token);
        return token;
    }

    /**
     * 退出登录接口
     *
     * @param request 登出请求
     * @return 登出的用户id 或者 null
     */
    @Override
    @EmpLog
    public Long logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            log.error("logout token is null");
            return null;
        }
        token = token.substring(7); // 去掉 "Bearer " 部分
        return JwtUtils.getIdFromToken(token);
    }

    /**
     * 分页查询接口
     *
     * @param page     页码
     * @param pageSize 每页记录数
     * @param name     姓名
     * @return 分页查询结果
     */
    @Override
    public PageBean<Employee> page(Integer page, Integer pageSize, String name) {

        Page<Employee> ipage = new Page<>(page, pageSize);

        Page<Employee> empPage = lambdaQuery().
                like(StringUtils.isNotEmpty(name), Employee::getName, name)  // name != null && !name.isEmpty()
                .orderByDesc(Employee::getUpdateTime)
                .page(ipage);

        return new PageBean<>((int) empPage.getTotal(), empPage.getRecords());
    }
}
