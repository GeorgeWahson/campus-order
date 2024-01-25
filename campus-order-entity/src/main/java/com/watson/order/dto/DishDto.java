package com.watson.order.dto;

import com.watson.order.po.Dish;
import com.watson.order.po.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    // 菜品所对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;  // 管理端显示菜品所属分类

    private Integer copies;
}
