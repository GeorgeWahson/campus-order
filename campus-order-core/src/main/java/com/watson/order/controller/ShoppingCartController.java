package com.watson.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.watson.order.ShoppingCartService;
import com.watson.order.aop.UserLog;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.Result;
import com.watson.order.po.ShoppingCart;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/shoppingCart")
@Api(tags = "购物车相关接口")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart 封装 用户添加的菜品 信息
     * @return 查询后的购物车数据
     */
    @UserLog
    @PostMapping("/add")
    @ApiOperation(value = "购物车添加商品接口")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {

        log.info("add shopping cart: {}", shoppingCart);

        // 设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        // 调用服务，添加/更新数据到数据库
        ShoppingCart sc = shoppingCartService.add(shoppingCart);

        log.info("return cart: {}", sc);
        return Result.success(sc);
    }


    /**
     * 从购物车查询
     *
     * @return 用户购物车数据
     */
    @GetMapping("/list")
    @ApiOperation(value = "购物车查询接口")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车。。。");
        // 查询当前用户的购物车数据
        List<ShoppingCart> list = shoppingCartService.lambdaQuery()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .orderByAsc(ShoppingCart::getCreateTime)
                .list();

        return Result.success(list);
    }

    /**
     * 清空购物车
     *
     * @return 操作结果信息
     */
    @UserLog
    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车接口")
    public Result<String> clean() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);

        return Result.success("清空购物车成功!");
    }

    /**
     * 购物车减少项目
     *
     * @param shoppingCart 封装 用户删除的菜品 信息
     * @return 删除后的购物车商品数据
     */
    @UserLog
    @PostMapping("/sub")
    @ApiOperation(value = "购物车减少商品接口")
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {

        log.info("sub shopping cart: {}", shoppingCart);

        // 设置用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 调用服务，删除/更新数据到数据库
        ShoppingCart sc = shoppingCartService.sub(shoppingCart);

        log.info("return cart: {}", sc);
        return Result.success(sc);
    }
}
