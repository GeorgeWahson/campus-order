package com.watson.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 返回结果包装类
 * 实现序列化接口以实现缓存存储（改为service实现）
 *
 * @param <T>
 */
@Data
@ApiModel("返回结果")
public class Result<T> {

    @ApiModelProperty("编码")
    private Integer code; //编码：1成功，0和其它数字为失败

    @ApiModelProperty("错误信息")
    private String msg; //错误信息

    @ApiModelProperty("数据")
    private T data; //数据


    /**
     * 主要返回String类型的结果反馈信息，以及List及entity数据信息。
     *
     * @param object 返回结果对象
     * @param <T>    泛型
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
