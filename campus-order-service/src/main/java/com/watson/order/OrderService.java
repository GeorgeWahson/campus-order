package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.dto.OrderDto;
import com.watson.order.dto.PageBean;
import com.watson.order.po.Orders;

import java.time.LocalDateTime;

public interface OrderService extends IService<Orders> {

    // 用户下单
    void submit(Orders orders);

    // 管理端查询数据
    PageBean<Orders> page(int page, int pageSize, String number, LocalDateTime beginTime, LocalDateTime endTime);

    // 用户端查看订单数据
    PageBean<OrderDto> userPage(Long userId, int page, int pageSize);
}
