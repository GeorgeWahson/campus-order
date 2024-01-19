package com.watson.order.dto;

import com.watson.order.po.Setmeal;
import com.watson.order.po.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;  // 分页查询时，显示 所属套餐分类名称
}
