package com.watson.order.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.watson.order.common.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * mybatis-plus自动填充，在数据更新及插入时自动插入或更新对应字段。
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insertFill:  " + metaObject.toString());
         this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now()); // 起始版本 3.3.0(推荐使用)
         this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now()); // 起始版本 3.3.0(推荐使用)
         this.strictInsertFill(metaObject, "createUser", Long.class, BaseContext.getCurrentId()); // 起始版本 3.3.0(推荐使用)
         this.strictInsertFill(metaObject, "updateUser", Long.class, BaseContext.getCurrentId()); // 起始版本 3.3.0(推荐使用)
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("updateFill:  " + metaObject.toString());
        log.info("线程id为：{}", Thread.currentThread().getId());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }
}
