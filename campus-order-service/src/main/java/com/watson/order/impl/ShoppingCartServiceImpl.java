package com.watson.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.ShoppingCartService;
import com.watson.order.mapper.ShoppingCartMapper;
import com.watson.order.po.ShoppingCart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    /**
     * 添加菜品到购物车，依据不同口味插入数据，
     * 同时计算购物车中单个菜品所有口味的份数
     *
     * @param shoppingCart 封装 用户添加的菜品 信息
     * @return 查询后的购物车数据
     */
    @Override
    @Transactional
    public ShoppingCart add(ShoppingCart shoppingCart) {


        Long dishId = shoppingCart.getDishId();

        int singleNum; // 单品所有口味的数量

        // 查询当前【菜品】或套餐是否已存在，一样则 number+1
        // 拼接用户id
        LambdaQueryChainWrapper<ShoppingCart> wrapper = this.lambdaQuery().eq(ShoppingCart::getUserId, shoppingCart.getUserId());

        if (dishId == null) { // 添加的是套餐
            ShoppingCart setMealShoppingCart = wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId()).one();
            if (setMealShoppingCart != null) { // 套餐原先已经存在
                setMealShoppingCart.setNumber(setMealShoppingCart.getNumber() + 1); // 单品数量 + 1
                setMealShoppingCart.setSingleNum(setMealShoppingCart.getSingleNum() + 1); // 总数量 + 1
                this.updateById(setMealShoppingCart); // 执行更新
                return setMealShoppingCart;
            } else { // 套餐原先不存在
                shoppingCart.setNumber(1); // 初始为 1
                shoppingCart.setSingleNum(1); // 初始为 1
                this.save(shoppingCart);  // 执行插入
                return shoppingCart;
            }
        } else { // 添加的是菜品
            // 精准到 口味 信息
            ShoppingCart dishFlavorShoppingCart = wrapper.eq(ShoppingCart::getDishId, dishId)
                    .eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor()).one();
            if (dishFlavorShoppingCart != null) {  // 该口味已经存在
                // 总数量 + 1
                singleNum = dishFlavorShoppingCart.getSingleNum() + 1;

                dishFlavorShoppingCart.setNumber(dishFlavorShoppingCart.getNumber() + 1);  // 单品数量 + 1
                dishFlavorShoppingCart.setSingleNum(singleNum); // 更新总数量
                this.updateById(dishFlavorShoppingCart);  // 执行【该菜品口味数据】的更新

                // 更新其他 同菜品，不同口味的【总数量】数据
                this.lambdaUpdate()
                        .eq(ShoppingCart::getDishId, dishId)
                        .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                        .set(ShoppingCart::getSingleNum, singleNum)
                        .update();
                return dishFlavorShoppingCart;
            } else { // 该口味不存在，需要判断是否有 同菜品的其他口味
                shoppingCart.setNumber(1);  // 设置单品数量为 1

                Long count = this.lambdaQuery()
                        .eq(ShoppingCart::getDishId, dishId)
                        .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                        .count();
                if (count == 0) { // 同菜品 没有 其他口味
                    shoppingCart.setSingleNum(1);  // 设置总数量为 1
                    this.save(shoppingCart);  // 保存该口味数据，由于没有其他口味，不用更新

                } else { // 同菜品 有 其他口味
                    // 获取菜品 原先的 总数量
                    ShoppingCart sc = this.lambdaQuery()
                            .eq(ShoppingCart::getDishId, dishId)
                            .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                            .list().get(0);
                    singleNum = sc.getSingleNum() + 1; // 设置总数量 +1
                    shoppingCart.setSingleNum(singleNum);
                    // 保存新添加的数据
                    this.save(shoppingCart);
                    // 更新其他 同菜品，不同口味的【总数量】数据
                    this.lambdaUpdate()
                            .eq(ShoppingCart::getDishId, dishId)
                            .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                            .set(ShoppingCart::getSingleNum, singleNum)
                            .update();

                }
                return shoppingCart;

            }

        }

    }

    /**
     * 购物车减少【1个】项目
     *
     * @param shoppingCart 封装 用户删除的菜品 信息
     * @return 删除后的购物车商品数据
     */
    @Override
    @Transactional
    public ShoppingCart sub(ShoppingCart shoppingCart) {

        Long dishId = shoppingCart.getDishId();

        // 单品 和 总数量 -1
        int number = shoppingCart.getNumber() - 1;
        Integer singleNum = shoppingCart.getSingleNum() - 1;

        // 更新
        shoppingCart.setNumber(number);
        shoppingCart.setSingleNum(singleNum);

        if (dishId == null) { // 移除的是套餐

            if (number == 0) {  // 归零，删除 该项目的 购物车数据
                LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
                lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId()).eq(ShoppingCart::getUserId, shoppingCart.getUserId());
                this.remove(lqw);

            } else {  // 减少，更新

                // 根据 套餐id 更新 套餐购物车数据
                this.lambdaUpdate()
                        .eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
                        .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                        .update(shoppingCart);
            }

        } else { // 移除的是菜品
            if (number != 0) { // 该口味数据不止一份

                // 根据 菜品id 更新 该口味菜品 的 购物车数据
                this.lambdaUpdate()
                        .eq(ShoppingCart::getDishId, dishId)
                        .eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
                        .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                        .update(shoppingCart);


            } else { // 该口味数据只有一份，执行删除
                LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
                lqw.eq(ShoppingCart::getDishId, dishId)
                        .eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor())
                        .eq(ShoppingCart::getUserId, shoppingCart.getUserId());
                this.remove(lqw);
            }

            // 更新 or 删除 后，还要判断是否存在 不同口味 的其他菜品，需要更新 singleNum

            if (number != singleNum) { // 单口味数量 不等于 总数量，说明有其他口味
                log.info("singleNum : {}, number: {}", singleNum, number);
                this.lambdaUpdate()
                        .eq(ShoppingCart::getDishId, dishId)
                        .eq(ShoppingCart::getUserId, shoppingCart.getUserId())
                        .set(ShoppingCart::getSingleNum, singleNum)
                        .update();
            }

        }
        return shoppingCart;

    }
}
