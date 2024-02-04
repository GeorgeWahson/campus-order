package com.watson.order.dto;

import com.watson.order.po.Dish;
import com.watson.order.po.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("菜品Dto")
public class DishDto extends Dish {

    // 菜品所对应的口味数据
    @ApiModelProperty("口味数据列表")
    private List<DishFlavor> flavors = new ArrayList<>();

    @ApiModelProperty("所属分类")
    private String categoryName;  // 管理端显示菜品所属分类

    // private Integer copies;
}
