package com.watson.order.controller;

import com.watson.order.OrderService;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.OrderDto;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Orders;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Api(tags = "订单相关接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * 用户下单
     *
     * @param orders 封装 订单信息【addressBookId，payMethod，remark】 实体类
     * @return 操作结果
     */
    @PostMapping("/submit")
    @ApiOperation(value = "用户下单接口")
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
    @ApiOperation(value = "管理端订单分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "number", value = "订单号", dataTypeClass = String.class),
            @ApiImplicitParam(name = "beginTime", value = "起始时间", dataTypeClass = LocalDateTime.class),
            @ApiImplicitParam(name = "endTime", value = "截止时间", dataTypeClass = LocalDateTime.class)
    })
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
    @ApiOperation(value = "修改订单状态接口")
    public Result<String> changeStatus(@RequestBody Orders orders) {
        log.info("orders: {}", orders);

        boolean update = orderService.lambdaUpdate()
                .eq(Orders::getId, orders.getId())
                .set(Orders::getStatus, orders.getStatus())
                .update();

        return update ? Result.success("状态修改成功") : Result.error("订单状态修改失败");
    }

    /**
     * 用户端获取 订单 列表
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @return 封装订单详细信息的数据传输对象
     */
    @GetMapping("/userPage")
    @ApiOperation(value = "用户端订单分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataTypeClass = int.class, required = true)
    })
    public Result<PageBean<OrderDto>> userPage(int page, int pageSize) {

        Long userId = BaseContext.getCurrentId();
        PageBean<OrderDto> orderDtoPageBean = orderService.userPage(userId, page, pageSize);

        return Result.success(orderDtoPageBean);
    }

}
