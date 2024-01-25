package com.watson.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watson.order.po.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
