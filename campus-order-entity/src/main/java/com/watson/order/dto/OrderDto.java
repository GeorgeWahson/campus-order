package com.watson.order.dto;

import com.watson.order.po.OrderDetail;
import com.watson.order.po.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("订单Dto")
public class OrderDto extends Orders {

    // 封装用户订单界面所需的 订单明细【商品名称、数量】
    @ApiModelProperty("订单明细集合")
    private List<OrderDetail> orderDetails;

}
