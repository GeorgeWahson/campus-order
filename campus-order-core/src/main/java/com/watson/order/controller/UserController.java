package com.watson.order.controller;

import com.watson.order.UserService;
import com.watson.order.aop.UserLog;
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
    @UserLog
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
     * @param map 包含 email 和 code
     * @return 登录结果
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录接口")
    public Result<String> login(@RequestBody Map<String, String> map) {

        String ret = userService.login(map);
        // 长度小于15，说明不是token，登录失败
        if (ret.length() < 15) {
            return Result.error(ret);
        }
        // 登录成功，返回token
        return Result.success(ret);
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

        Long id = userService.logout(request);
        log.info("用户id: {}登出。", id);

        return Result.success("退出成功！");

    }

}
