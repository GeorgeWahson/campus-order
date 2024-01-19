package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.DishFlavorService;
import com.watson.order.mapper.DishFlavorMapper;
import com.watson.order.po.DishFlavor;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
