package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.po.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserService extends IService<User> {

    // 传递封装 邮箱 及 验证码 的map，进行比对
    String login(Map<String, String> map);

    // 登出方法，返回登出的用户id或null
    Long logout(HttpServletRequest request);
}
