package com.watson.order.controller;

import com.watson.order.CategoryService;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Category;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor  // 必备的构造函数，注入 categoryService
@RequestMapping("/category")
@Api(tags = "分类相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param category 分类
     * @return 结果信息
     */
    @PostMapping
    @ApiOperation(value = "新增分类接口")
    public Result<String> save(@RequestBody Category category) {
        log.info("category: {}", category);
        categoryService.save(category);
        return Result.success("新增分类成功!");
    }

    /**
     * 分类查询
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @param showType 显示类型
     * @return 封装结果dto
     */
    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "showType", value = "分类类型", dataTypeClass = String.class)
    })
    public Result<PageBean<Category>> page(int page, int pageSize, String showType) {

        log.info("category page: {}, pageSize: {}, showType: {}", page, pageSize, showType);

        PageBean<Category> pageBean = categoryService.page(page, pageSize, showType);

        return Result.success(pageBean);

    }

    /**
     * 根据id删除分类
     *
     * @param id 分类id
     * @return 删除结果信息
     */
    @DeleteMapping
    @ApiOperation(value = "删除分类接口")
    public Result<String> delete(@RequestParam("ids") Long id) {
        log.info("删除分类，id为：{}", id);

        categoryService.remove(id);

        return Result.success("删除成功!");
    }


    /**
     * 根据id修改分类信息
     *
     * @param category 封装更新后的category对象
     * @return 更新结果信息
     */
    @PutMapping
    @ApiOperation(value = "更新分类接口")
    public Result<String> update(@RequestBody Category category) {
        log.info("修改分类信息：{}", category);

        categoryService.updateById(category);

        return Result.success("修改分类信息成功");
    }

    /**
     * - 菜品 Dish 添加页面，获取【菜品】分类展示到页面下拉框中
     * - 套餐 SetMeal 添加页面，获取【套餐】分类展示到页面下拉框中
     * - 用户移动端页面，获取分类，显示到左侧分类栏目
     *
     * @param category 分类 dish type=1, set_meal = 2
     * @return 分类集合包装
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取分类集合接口")
    public Result<List<Category>> list(Category category) {

        List<Category> list = categoryService.lambdaQuery()
                .eq(category.getType() != null, Category::getType, category.getType())
                .orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime)
                .list();

        return Result.success(list);
    }

}
