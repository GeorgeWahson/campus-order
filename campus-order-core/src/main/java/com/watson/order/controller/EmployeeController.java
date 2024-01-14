package com.watson.order.controller;

import com.watson.order.EmployeeService;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor  // 必备的构造函数，注入 employeeService
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 员工登录
     * @param request 网页请求
     * @param employee 封装登录用户名及密码的用户对象
     * @return 根据用户名及密码查询的用户对象
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        log.info("in controller");
        Employee loginUser = employeeService.login(employee);

        log.info("service return login user: {}", loginUser);
        // 如果没有查询到则返回登录失败结果
        if (loginUser == null) {
            // 用户不存在，或者密码错误
            return Result.error("用户名或密码错误!!");
        }

        // 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (loginUser.getStatus() == 0) {
            return Result.error("账号已禁用!");
        }

        // 登录成功，将员工id存入Session并返回登录成功结果
        // TODO JWT
        request.getSession().setAttribute("employee", loginUser.getId());

        return Result.success(loginUser);
    }

    /**
     * 员工点击退出
     * @param request 前端发出的登出请求
     * @return 登出结果信息
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        log.info("用户{}登出。", BaseContext.getCurrentId());
        // 清除Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功！");
    }

    /**
     * 新增员工
     * @param employee 前端填写的用户信息
     * @return 新增结果信息
     */
    @PostMapping
    public Result<String> save(@RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        // 设置初始密码123456，需要进行md5加密处理
        //  employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置自定义密码
        // 理论上，md5加密是不可逆的，所以无法从数据库提取原来的明文密码
        employee.setPassword(DigestUtils.md5DigestAsHex(employee.getPassword().getBytes()));

        employeeService.save(employee);

        return Result.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * 在控制器方法的形参位置，**设置和请求参数同名的形参**，
     * 当浏览器发送请求，匹配到请求映射时，在DispatcherServlet中就会
     * 将请求参数赋值给相应的形参
     * @param page 页码
     * @param pageSize 每页显示数量
     * @param name 模糊查询的用户名
     * @return 查询结构封装dto
     */
    @GetMapping("/page")
    public Result<PageBean<Employee>> page(int page, int pageSize, String name) {
        log.info("employee page: {}, pageSize: {}, name: {}", page, pageSize, name);

        PageBean<Employee> pb = employeeService.page(page, pageSize, name);

        return Result.success(pb);
    }

    /**
     * 根据id修改员工信息
     * @param employee 更新后的用户对象
     * @return 更新结果信息
     */
    @PutMapping
    public Result<String> update(@RequestBody Employee employee) {
        log.info(employee.toString());
        log.info("线程id为：{}", Thread.currentThread().getId());

        // id为1，说明用户修改 管理员账户
        if (employee.getId() == 1) {
            // 修改状态，前端只传用户id及status，username为空。
            if (employee.getStatus() == 0) {
                // 用户执行了禁用管理员操作
                return Result.error("不能禁用管理员账户！");
            } else if ( !"admin".equals(employee.getUsername()) ) {
                // 用户修改admin用户名
                return Result.error("不能修改管理员账号名称！");
            }
        }
        // 不更新密码

        // 雪花算法id js对long型精度遗失
        employeeService.updateById(employee);
        return Result.success("员工信息修改成功!");
    }

    /**
     * 根据id查询员工信息
     * @param id 用户id
     * @return 被查询的用户对象
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee empById = employeeService.getById(id);
        log.info("查找id: {}, 结果：{}", id, empById);

        if(empById != null){
            return Result.success(empById);
        }
        return Result.error("没有查询到对应员工信息");
    }



}
