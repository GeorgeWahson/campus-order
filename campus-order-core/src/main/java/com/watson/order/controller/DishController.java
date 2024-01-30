package com.watson.order.controller;

import com.watson.order.DishService;
import com.watson.order.dto.DishDto;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Dish;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dish")
public class DishController {

    private final DishService dishService;

    /**
     * 新增菜品
     *
     * @param dishDto 封装前端 数据传输对象
     * @return 新增结构信息
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info("dishDto: {}", dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return Result.success("新增菜品成功!");

    }

    /**
     * 分页查询菜品 page/food/list.html
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @param name     搜索名称 模糊匹配
     * @return DishDto数据传输对象
     */
    @GetMapping("/page")
    public Result<PageBean<DishDto>> page(int page, int pageSize, String name) {

        PageBean<DishDto> dishDtoPageBean = dishService.page(page, pageSize, name);
        return Result.success(dishDtoPageBean);
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id 菜品id
     * @return 菜品数据传输对象 的包装类
     */
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id) {
        log.info("get id of category: {}", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto 修改后的 dish 数据传输对象
     * @return 操作结果信息
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("dishDto: {}", dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        return Result.success("修改菜品成功!");
    }


    /**
     * 删除菜品，同时删除 dish_flavor, 删除图片。
     *
     * @param ids 菜品id结合
     * @return 删除结果
     */
    @DeleteMapping
    public Result<String> deleteDish(@RequestBody List<String> ids) {

        log.info("delete dish, ids: {}", ids);

        // 先判断每个菜品是否都已经停售，若有菜品未停售，则返回error
        for (String id : ids) {
            Dish dish = dishService.getById(id);
            if (dish.getStatus() == 1) {
                return Result.error("存在菜品状态未停售，无法删除!");
            }
        }
        try {
            dishService.deleteWithFlavor(ids);
        } catch (Exception e) {
            log.info(e.getMessage());
            return Result.error("发生异常，请联系管理员!");
        }
        return Result.success("删除成功！");
    }


    /**
     * 批量，单个 修改菜品状态信息
     *
     * @param dishStatus 修改后的状态，1起售，0停售
     * @param ids        需要转换的ids集合
     * @return 操作结果信息
     */
    @DeleteMapping("/status/{dishStatus}")
    public Result<String> statusHandle(@PathVariable("dishStatus") int dishStatus, @RequestBody List<String> ids) {
        log.info("ids: {}, dishStatus: {} ", ids, dishStatus);

        dishService.changeBatchDishStatusByIds(dishStatus, ids);

        return Result.success("修改状态成功!");
    }

    /**
     * 根据条件查询菜品数据，使用时机：
     * <ol>
     *     <li>用于管理端添加套餐时，增添套餐内不同菜品分类的菜品，默认查询排在第一的菜品分类</li>
     *     <li>用户端用户查询某一菜品分类内有哪些菜品时，同时包含口味信息，进行选规格操作</li>
     * </ol>
     * 显示各菜品分类中所拥有的菜品
     * <p>传对象通用性高</p>
     *
     * @param dish 封装 菜品 分类id的 dish对象
     * @return 菜品集合 包装类
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {

        // log.info("list dish: {}", dish);

        List<DishDto> list = dishService.getListWithFlavor(dish);

        return Result.success(list);
    }
}

