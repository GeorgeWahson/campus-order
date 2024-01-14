package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.dto.PageBean;
import com.watson.order.po.Category;

public interface CategoryService extends IService<Category> {
//    public void remove(Long id);
    PageBean<Category> page(Integer page, Integer pageSize, String showType);

    void remove(Long id);
}
