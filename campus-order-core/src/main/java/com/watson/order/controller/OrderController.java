package com.watson.order.controller;

import com.watson.order.OrderService;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    /**
     * 管理端查询订单数据
     *
     * @param page      页码
     * @param pageSize  每页显示数量
     * @param number    订单号 模糊查询
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 订单列表封装数据
     */
    @GetMapping("/page")
    public Result<PageBean<Orders>> page(int page, int pageSize, String number,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("page: {}, pageSize: {}, number: {}, beginTime: {}, endTime:{}", page, pageSize, number, beginTime, endTime);

        PageBean<Orders> ordersPageBean = orderService.page(page, pageSize, number, beginTime, endTime);
        return Result.success(ordersPageBean);
    }

    /**
     * 修改订单状态
     *
     * @param orders 包含 id 和 修改后的状态值
     * @return 操作结果信息
     */
    @PutMapping
    public Result<String> changeStatus(@RequestBody Orders orders) {
        log.info("orders: {}", orders);

        boolean update = orderService.lambdaUpdate()
                .eq(Orders::getId, orders.getId())
                .set(Orders::getStatus, orders.getStatus())
                .update();

        return update ? Result.success("状态修改成功") : Result.error("订单状态修改失败");
    }


}
