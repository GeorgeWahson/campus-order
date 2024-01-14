package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.dto.PageBean;
import com.watson.order.mapper.EmployeeMapper;
import com.watson.order.EmployeeService;
import com.watson.order.po.Employee;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Override
    public Employee login(Employee e) {

        log.info("controller transmit service employee: {}", e);
        // 将页面提交的密码password进行md5加密处理，再与数据库数据比对
        String password = e.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        return lambdaQuery()
                .eq(Employee::getUsername, e.getUsername())
                .eq(Employee::getPassword, password)
                .one();
    }

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
