package com.watson.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("分页数据")
public class PageBean<T> {

    //总记录数
    @ApiModelProperty("记录数")
    private Integer total;

    //当前页数据列表
    @ApiModelProperty("当前页数据")
    private List<T> records;
}
