package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.dto.PageBean;
import com.watson.order.po.Employee;

public interface EmployeeService extends IService<Employee> {

    Employee login(Employee e);

    PageBean<Employee> page(Integer page, Integer pageSize, String name);
}
