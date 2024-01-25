package com.watson.order.controller;

import com.watson.order.OrderService;
import com.watson.order.dto.Result;
import com.watson.order.po.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 用户下单
     *
     * @param orders 封装 订单信息【addressBookId，payMethod，remark】 实体类
     * @return 操作结果
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);

        orderService.submit(orders);
        return Result.success("下单成功!");
    }

}
