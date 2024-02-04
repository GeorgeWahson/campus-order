package com.watson.order.controller;

import com.watson.order.UserService;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.Result;
import com.watson.order.po.User;
import com.watson.order.utils.SendMailUtils;
import com.watson.order.utils.ValidateCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Api(tags = "分类相关接口")
public class UserController {

    private final UserService userService;

    private final RedisTemplate<String, String> redisTemplate;

    private final SendMailUtils sendMailUtils;

    /**
     * 发送邮件验证码
     *
     * @param user    包含 用户邮箱 的对象
     * @param session 前端会话请求
     * @return 发送验证码结果
     */
    @PostMapping("/sendMsg")
    @ApiOperation(value = "发送登录验证码接口")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取邮箱
        String email = user.getEmail();
        if (StringUtils.isNotEmpty(email)) {
            log.info("email: {}", email);
            // 生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("check-code is :{}", code);

            // 邮件发送验证码
            // sendMailUtils.sendSimpleMail(code, email);

            // 将生成的验证码存至session
            // session.setAttribute(email, code);  // key value

            // 将生成的验证码放到缓存中，有效期为5分钟
            redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);

            return Result.success("验证码发送成功");
        }
        return Result.error("验证码发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map     包含 email 和 code
     * @param session 前端会话请求
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录接口")
    public Result<User> login(@RequestBody Map<String, String> map, HttpSession session) {

        log.info("map: " + map.toString());

        // 获取邮箱地址
        String email = map.get("email");

        // 获取验证码
        String code = map.get("code");

        // 从Session中获取保存的验证码
        // Object codeInSession = session.getAttribute(email);

        // 从redis中获取验证码
        String codeInRedis = redisTemplate.opsForValue().get(email);

        // 进行验证码的比对（页面提交的验证码和Session中保存的验证码比对成功）
        if (codeInRedis != null && codeInRedis.equals(code)) {
            // 如果能够比对成功，说明登录成功
            // 查找或新建用户
            User user = userService.login(email);

            session.setAttribute("user", user.getId());

            // 如果用户登录成功，直接删除缓存中的验证码
            redisTemplate.delete(email);

            return Result.success(user);
        }
        return Result.error("验证码错误，登录失败");
    }

    /**
     * 用户点击退出
     *
     * @param request 前端发出的登出请求
     * @return 登出结果信息
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户登出接口")
    public Result<String> logout(HttpServletRequest request) {
        log.info("用户{}登出。", BaseContext.getCurrentId());
        // 清除Session中保存的当前登录员工的id
        request.getSession().removeAttribute("user");
        return Result.success("退出成功！");
    }

}
