package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.po.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {

    // 购物车添加菜品
    ShoppingCart add(ShoppingCart shoppingCart);

    // 购物车减少菜品
    ShoppingCart sub(ShoppingCart shoppingCart);
}
