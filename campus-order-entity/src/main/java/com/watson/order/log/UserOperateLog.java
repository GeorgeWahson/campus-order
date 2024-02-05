package com.watson.order.log;

import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;

@ApiModel("用户操作日志")
public class UserOperateLog extends BaseOperateLog {
    public UserOperateLog(Integer id, Long operateUser, LocalDateTime operateTime, String className, String methodName, String methodParams, String returnValue, Long costTime) {
        super(id, operateUser, operateTime, className, methodName, methodParams, returnValue, costTime);
    }
}
