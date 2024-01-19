package com.watson.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.CategoryService;
import com.watson.order.SetmealDishService;
import com.watson.order.SetmealService;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.SetmealDto;
import com.watson.order.exception.CustomException;
import com.watson.order.mapper.SetmealMapper;
import com.watson.order.po.Category;
import com.watson.order.po.Setmeal;
import com.watson.order.po.SetmealDish;
import com.watson.order.utils.AliOSSUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    private final SetmealDishService setmealDishService;

    @Autowired
    @Lazy
    private CategoryService categoryService;

    private final AliOSSUtils aliOSSUtils;


    /**
     * 新增套餐，同时保存套餐和菜品关联信息
     *
     * @param setmealDto 封装前端 套餐信息
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作set_meal,执行 insert 操作
        this.save(setmealDto);

        // 获取 套餐-菜品 对应集合
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        // 为每个 套餐中的菜品，赋予套餐的id
        setmealDishes = setmealDishes.stream().peek((item) -> item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());

        // 保存套餐和菜品的关联信息，操作set_meal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 套餐分页查询
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @param name     套餐名称 模糊查询
     * @return 查询结果 包装类
     */
    @Override
    public PageBean<SetmealDto> page(int page, int pageSize, String name) {

        Page<Setmeal> setmealPage = this.lambdaQuery().like(name != null, Setmeal::getName, name)
                .orderByDesc(Setmeal::getStatus) // 停售的排后面
                .orderByDesc(Setmeal::getUpdateTime)
                .page(new Page<>(page, pageSize));

        // 对象拷贝，拷贝除了records的属性
        List<SetmealDto> list = setmealPage.getRecords().stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;

        }).collect(Collectors.toList());

        return new PageBean<>((int) setmealPage.getTotal(), list);
    }

    /**
     * 删除套餐，同时删除 setMeal_dish 中的菜品关联数
     *
     * @param ids 套餐 id 集合
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 未停售套餐不可删除
        Long count = this.lambdaQuery()
                .eq(Setmeal::getStatus, 1)
                .in(Setmeal::getId, ids).count();

        if (count > 0) {
            throw new CustomException("有套餐正在售卖中，不能删除!");
        }

        // 删除套餐图片
        try {
            for (Long id : ids) {
                String imageUrl = this.getById(id).getImage();
                if (imageUrl != null) aliOSSUtils.delete(imageUrl);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // 删除 setMeal
        this.removeByIds(ids);

        // 删除 set_meal_dish 对应的菜品数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 修改套餐时获取菜品信息进行回显
     *
     * @param id 套餐id
     */
    @Override
    public SetmealDto getWithDish(Long id) {
        // 查询套餐基本信息，从dish查询
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        // 拷贝属性
        BeanUtils.copyProperties(setmeal, setmealDto);

        List<SetmealDish> setmealDishList = setmealDishService.lambdaQuery()
                .eq(SetmealDish::getSetmealId, id)
                .list();

        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;

    }

    /**
     * 更新套餐，同时更新 套餐与菜品的对应表
     *
     * @param setmealDto 前端传输的 套餐与菜品 传输对象
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {

        //更新 set_meal 表基本信息(name,price等)
        this.updateById(setmealDto);

        // 清理当前套餐对应菜品数据---set_meal_dish表的delete操作
        // 套餐里每道菜都是一条数据，更新不能删除原本存在的数据，只能全删再插

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(wrapper);

        // 添加当前提交过来的菜品数据 执行 set_meal_dish 表的 insert 操作
        List<SetmealDish> setMealDishes = setmealDto.getSetmealDishes();

        setMealDishes = setMealDishes.stream().peek((item) ->
                        item.setSetmealId(setmealDto.getId()))
                .collect(Collectors.toList());

        setmealDishService.saveBatch(setMealDishes);
    }

    /**
     * 批量，单个 修改套餐状态信息
     *
     * @param setmealStatus 修改后的状态，1起售，0停售
     * @param ids           需要转换的 id 集合
     */
    @Override
    public void statusHandle(int setmealStatus, List<String> ids) {
        this.lambdaUpdate()
                .in(Setmeal::getId, ids)
                .set(Setmeal::getStatus, setmealStatus)
                .update();
    }


}

