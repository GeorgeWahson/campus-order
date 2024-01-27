package com.watson.order.dto;

import com.watson.order.po.OrderDetail;
import com.watson.order.po.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderDto extends Orders {

    // 封装用户订单界面所需的 订单明细【商品名称、数量】
    private List<OrderDetail> orderDetails;

}
