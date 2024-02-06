package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.dto.PageBean;
import com.watson.order.po.Employee;

import javax.servlet.http.HttpServletRequest;

public interface EmployeeService extends IService<Employee> {

    // 登录方法，返回登录用户的token或错误信息
    String login(Employee e);

    // 登出方法，返回登出的用户id或null
    Long logout(HttpServletRequest request);

    PageBean<Employee> page(Integer page, Integer pageSize, String name);
}
