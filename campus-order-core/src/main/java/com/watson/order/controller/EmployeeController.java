package com.watson.order.controller;

import com.watson.order.EmployeeService;
import com.watson.order.aop.EmpLog;
import com.watson.order.aop.UserLog;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Employee;
import com.watson.order.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor  // 必备的构造函数，注入 employeeService
@RequestMapping("/employee")
@Api(tags = "员工相关接口")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param employee 封装登录用户名及密码的用户对象
     * @return 根据用户名及密码查询的用户对象
     */
    @PostMapping("/login")
    @ApiOperation(value = "登录接口")
    public Result<String> login(@RequestBody Employee employee) {

        String ret = employeeService.login(employee);
        // 长度小于15，说明不是token，登录失败
        if (ret.length() < 15) {
            return Result.error(ret);
        }
        // 登录成功，返回token
        return Result.success(ret);
    }

    /**
     * 员工点击退出
     *
     * @param request 前端发出的登出请求
     * @return 登出结果信息
     */
    @PostMapping("/logout")
    @ApiOperation(value = "登出接口")
    public Result<String> logout(HttpServletRequest request) {

        Long id = employeeService.logout(request);
        log.info("用户id: {}登出。", id);

        return Result.success("退出成功！");
    }

    /**
     * 新增员工
     *
     * @param employee 前端填写的用户信息
     * @return 新增结果信息
     */
    @EmpLog
    @PostMapping
    @ApiOperation(value = "新增员工接口")
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
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @param name     模糊查询的用户名
     * @return 查询结构封装dto
     */
    @GetMapping("/page")
    @ApiOperation(value = "员工分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "name", value = "姓名", dataTypeClass = String.class)
    })
    public Result<PageBean<Employee>> page(int page, int pageSize, String name) {
        log.info("employee page: {}, pageSize: {}, name: {}", page, pageSize, name);

        PageBean<Employee> pb = employeeService.page(page, pageSize, name);

        return Result.success(pb);
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee 更新后的用户对象
     * @return 更新结果信息
     */
    @EmpLog
    @PutMapping
    @ApiOperation(value = "更新员工接口")
    public Result<String> update(@RequestBody Employee employee) {
        log.info(employee.toString());
        log.info("线程id为：{}", Thread.currentThread().getId());

        // id为1，说明用户修改 管理员账户
        if (employee.getId() == 1) {
            // 修改状态，前端只传用户id及status，username为空。
            if (employee.getStatus() == 0) {
                // 用户执行了禁用管理员操作
                return Result.error("不能禁用管理员账户！");
            } else if (!"admin".equals(employee.getUsername())) {
                // 用户修改admin用户名
                return Result.error("不能修改管理员账号名称！");
            }
        }
        // 用户编辑界面无法更改密码，md5加密不可逆，修改密码需新建页面，通过输入原密码与数据库加密后密码进行匹配
        // 雪花算法id js对long型精度遗失
        employeeService.updateById(employee);
        return Result.success("员工信息修改成功!");
    }

    /**
     * 根据id查询员工信息
     *
     * @param id 用户id
     * @return 被查询的用户对象
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "查询员工接口")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee empById = employeeService.getById(id);
        log.info("查找id: {}, 结果：{}", id, empById);

        if (empById != null) {
            return Result.success(empById);
        }
        return Result.error("没有查询到对应员工信息");
    }


}
