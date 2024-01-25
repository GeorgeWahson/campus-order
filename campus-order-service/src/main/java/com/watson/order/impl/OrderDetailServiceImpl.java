package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.OrderDetailService;
import com.watson.order.mapper.OrderDetailMapper;
import com.watson.order.po.OrderDetail;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
