package com.watson.order.dto;

import com.watson.order.po.Setmeal;
import com.watson.order.po.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("套餐Dto")
public class SetmealDto extends Setmeal {

    @ApiModelProperty("套餐菜品集合")
    private List<SetmealDish> setmealDishes;

    @ApiModelProperty("分类名称")
    private String categoryName;  // 分页查询时，显示 所属套餐分类名称
}
