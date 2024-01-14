package com.watson.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.CategoryService;
import com.watson.order.DishService;
import com.watson.order.SetmealService;
import com.watson.order.dto.PageBean;
import com.watson.order.exception.CustomException;
import com.watson.order.mapper.CategoryMapper;
import com.watson.order.po.Category;
import com.watson.order.po.Dish;
import com.watson.order.po.Setmeal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 列表查询
     * @param page 页码
     * @param pageSize 每页显示数量
     * @param showType 显示 套餐分类 or 菜品分类
     * @return 分类dto PageBean<Category>
     */
    @Override
    public PageBean<Category> page(Integer page, Integer pageSize, String showType) {
        Page<Category> ipage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(showType)) {
            lqw.eq(Category::getType, showType);
        }
        lqw.orderByAsc(Category::getSort);

        Page<Category> categoryPage = page(ipage, lqw);
        return new PageBean<>((int) categoryPage.getTotal(), categoryPage.getRecords());
    }


    private final DishService dishService;

    private final SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id 分类id
     */
    @Override
    public void remove(Long id) {
        // 根据分类id进行查询,当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        Long dishCount = dishService
                .lambdaQuery()
                .eq(Dish::getCategoryId, id)
                .count();

        if (dishCount > 0) {
            // 已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 查询关联套餐
        Long setMealCount = setmealService
                .lambdaQuery()
                .eq(Setmeal::getCategoryId, id)
                .count();

        if (setMealCount > 0) {
            // 已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        // 正常删除分类
        removeById(id);
    }
}
