package com.watson.order.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体类
 */
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;  // 雪花算法，长度19位，前端number精度16位，会失真。需要配置消息转换器，响应给前端json时转为String.

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;  // 身份证号码id_number   yml文件中已开启驼峰命名法

    private Integer status;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT) // 插入时添加字段
    private LocalDateTime createTime;

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入 更新时添加字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) // 插入时添加字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入 更新时添加字段
    private Long updateUser;

}
