package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.UserService;
import com.watson.order.mapper.UserMapper;
import com.watson.order.po.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 根据前端传递的 email 查找用户，没有则创建用户
     *
     * @param email 邮箱地址
     * @return 新创建的用户 或 已存在的用户
     */
    @Override
    public User login(String email) {
        // 根据邮箱查找用户
        User user = this.lambdaQuery().eq(User::getEmail, email)
                .one();

        if (user == null) {
            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            user = new User();
            user.setEmail(email);
            user.setStatus(1);
            this.save(user);
        }

        return user;
    }
}
