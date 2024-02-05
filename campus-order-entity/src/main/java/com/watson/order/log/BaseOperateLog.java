package com.watson.order.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 基础操作日志类，包含公共属性
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("基础操作日志类")
public class BaseOperateLog {

    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Integer id; //主键ID

    @ApiModelProperty("操作人ID")
    private Long operator; //操作人ID

    @ApiModelProperty("操作时间")
    private LocalDateTime operateTime; //操作时间

    @ApiModelProperty("操作类名")
    private String className; //操作类名

    @ApiModelProperty("操作方法名")
    private String methodName; //操作方法名

    @ApiModelProperty("操作方法参数")
    private String methodParams; //操作方法参数

    @ApiModelProperty("操作方法返回值")
    private String returnValue; //操作方法返回值

    @ApiModelProperty("操作耗时")
    private Long costTime; //操作耗时
}
