package com.watson.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watson.order.log.EmpOperateLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmpOperateLogMapper extends BaseMapper<EmpOperateLog> {
}
