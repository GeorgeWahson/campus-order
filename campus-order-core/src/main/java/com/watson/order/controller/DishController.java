package com.watson.order.controller;

import com.watson.order.DishService;
import com.watson.order.dto.DishDto;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.po.Dish;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
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

//    @Autowired
//    private RedisTemplate redisTemplate;

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

        // 清理所有菜品缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的缓存
//        String key = "dish_" + dishDto.getCategoryId() + "_1";
//        redisTemplate.delete(key);
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
        // 清理所有菜品缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的缓存
//        String key = "dish_" + dishDto.getCategoryId() + "_1";
//        redisTemplate.delete(key);

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

//            // 修改状态不走update方法，需要删除缓存
//            String key = "dish_" + dish.getCategoryId() + "_1";
//            redisTemplate.delete(key);

        return Result.success("修改状态成功!");
    }

    /**
     * 根据条件查询菜品数据，用于添加套餐时，显示各菜品分类中所拥有的菜品
     * 传对象通用性高
     *
     * @param dish 封装 菜品 分类id的 dish对象
     * @return 菜品集合 包装类
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Dish dish) {

        List<Dish> list = dishService.lambdaQuery()
                .eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .eq(Dish::getStatus, 1)  // 只查起售的菜
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime)
                .list();

        return Result.success(list);
    }
}

