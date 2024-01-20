package com.watson.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watson.order.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
