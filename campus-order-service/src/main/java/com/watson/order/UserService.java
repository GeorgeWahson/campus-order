package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.po.User;

public interface UserService extends IService<User> {

    User login(String email);
}
