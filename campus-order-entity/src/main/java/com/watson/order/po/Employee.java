package com.watson.order.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体类
 */
@Data
@ApiModel("员工")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;  // 雪花算法，长度19位，前端number精度16位，会失真。需要配置消息转换器，响应给前端json时转为String.

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("身份证号")
    private String idNumber;  // 身份证号码id_number   yml文件中已开启驼峰命名法

    @ApiModelProperty("状态")
    private Integer status;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT) // 插入时添加字段
    private LocalDateTime createTime;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入 更新时添加字段
    private LocalDateTime updateTime;

    @ApiModelProperty("创建用户")
    @TableField(fill = FieldFill.INSERT) // 插入时添加字段
    private Long createUser;

    @ApiModelProperty("更新用户")
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入 更新时添加字段
    private Long updateUser;

}
