package com.watson.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watson.order.po.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
