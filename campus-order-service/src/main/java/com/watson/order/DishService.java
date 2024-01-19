package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.dto.DishDto;
import com.watson.order.dto.PageBean;
import com.watson.order.po.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    // 新增菜品，同时插入 dish 和 dish_flavor 两张表
    void saveWithFlavor(DishDto dishDto);

    // 菜品分页查询
    PageBean<DishDto> page(int page, int pageSize, String name);

    // 根据id查询菜品信息及口味信息
    DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息，及口味信息
    void updateWithFlavor(DishDto dishDto);

    // 删除菜品信息及对应的口味信息
    void deleteWithFlavor(List<String> ids) throws Exception;

    // 修改菜品状态
    void changeBatchDishStatusByIds(int dishStatus, List<String> ids);
}
