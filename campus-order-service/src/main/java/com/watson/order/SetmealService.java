package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.SetmealDto;
import com.watson.order.po.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    // 新增套餐，同时保存套餐和菜品关联信息
    void saveWithDish(SetmealDto setmealDto);

    // 套餐分页查询
    PageBean<SetmealDto> page(int page, int pageSize, String name);

    // 删除套餐，同时删除 set_meal_dish中的菜品关联数据
    void removeWithDish(List<Long> ids);

    // 修改时获取套餐，同时获取套餐和菜品关联信息
    SetmealDto getWithDish(Long id);

    //修改套餐时，修改套餐菜品
    void updateWithDish(SetmealDto setmealDto);

    void statusHandle(int setmealStatus, List<String> ids);
}
