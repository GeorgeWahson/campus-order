package com.watson.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.*;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.OrderDto;
import com.watson.order.dto.PageBean;
import com.watson.order.exception.CustomException;
import com.watson.order.mapper.OrderMapper;
import com.watson.order.po.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    private final ShoppingCartService shoppingCartService;

    private final UserService userService;

    private final AddressBookService addressBookService;

    private final OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders 封装 订单信息 实体类
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获取当前用户id
        Long userId = BaseContext.getCurrentId();

        // 查询用户购物车list
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lqw);

        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new CustomException("购物车为空不能下单");
        }

        // 查询用户数据
        User user = userService.getById(userId);
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

        if (addressBook == null) {
            throw new CustomException("地址信息有误，不能下单!");
        }

        // 生成并设置订单号
        long orderId = IdWorker.getId(); //订单号

        // 保证线程安全
        AtomicInteger amount = new AtomicInteger(0);

        // 设置 订单明细 对象
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {

            OrderDetail orderDetail = new OrderDetail();
            // 拷贝除了主键id的共同属性
            BeanUtils.copyProperties(item, orderDetail, "id");

            orderDetail.setOrderId(orderId);
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

            return orderDetail;
        }).collect(Collectors.toList());

        // 设置订单实体类属性
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        // 向订单表插入数据，一条数据
        this.save(orders);

        // 向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        // 清空购物车数据
        shoppingCartService.remove(lqw);

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
    @Override
    public PageBean<Orders> page(int page, int pageSize, String number, LocalDateTime beginTime, LocalDateTime endTime) {

        Page<Orders> ordersPage = this.lambdaQuery().like(StringUtils.isNotEmpty(number), Orders::getNumber, number)
                .ge(beginTime != null, Orders::getCheckoutTime, beginTime)
                .le(endTime != null, Orders::getCheckoutTime, endTime)
                .orderByAsc(Orders::getStatus)
                .orderByDesc(Orders::getCheckoutTime)
                .page(new Page<>(page, pageSize));

        PageBean<Orders> ordersPageBean = new PageBean<>((int) ordersPage.getTotal(), ordersPage.getRecords());
        log.info("return ordersPageBean: {}", ordersPageBean);
        return ordersPageBean;
    }

    /**
     * 用户端查看订单数据
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @return 包含订单明细的数据传输对象
     */
    @Override
    public PageBean<OrderDto> userPage(Long userId, int page, int pageSize) {

        if (userId == null) {
            throw new CustomException("用户信息获取失败！");
        }
        Page<Orders> ordersPage = this.lambdaQuery().eq(Orders::getUserId, userId)
                .page(new Page<>(page, pageSize));

        List<OrderDto> orderDtoList = ordersPage.getRecords().stream().map((item) -> {
            Long orderId = item.getId();
            List<OrderDetail> orderDetailList = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();

            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item, orderDto);
            orderDto.setOrderDetails(orderDetailList);

            return orderDto;
        }).collect(Collectors.toList());

        return new PageBean<>((int) ordersPage.getTotal(), orderDtoList);
    }

}
