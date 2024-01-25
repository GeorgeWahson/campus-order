package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.po.Orders;

public interface OrderService extends IService<Orders> {

    // 用户下单
    void submit(Orders orders);
}
