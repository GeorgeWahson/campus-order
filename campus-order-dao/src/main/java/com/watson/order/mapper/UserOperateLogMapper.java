package com.watson.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watson.order.log.UserOperateLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserOperateLogMapper extends BaseMapper<UserOperateLog> {
}
