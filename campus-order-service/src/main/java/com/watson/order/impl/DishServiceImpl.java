package com.watson.order.impl;

import com.aliyuncs.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.CategoryService;
import com.watson.order.DishFlavorService;
import com.watson.order.DishService;
import com.watson.order.dto.DishDto;
import com.watson.order.dto.PageBean;
import com.watson.order.mapper.DishMapper;
import com.watson.order.po.Category;
import com.watson.order.po.Dish;
import com.watson.order.po.DishFlavor;
import com.watson.order.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    @Lazy
    private DishFlavorService dishFlavorService;

    @Autowired
    @Lazy
    private CategoryService categoryService;

    @Autowired
    private AliOSSUtils aliOSSUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增菜品，同时保存口味数据
     *
     * @param dishDto 前端数据传输对象
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto); // dto 继承至 Dish

        // 保存菜品口味的基本信息到菜品口味表 dish_flavor
        // 获取菜品id
        Long dishDtoId = dishDto.getId();
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        // map() can be replaced to peek()
        flavors = flavors.stream().peek((item) -> item.setDishId(dishDtoId))
                .collect(Collectors.toList());

        // 清理所有菜品缓存数据
        // Set<String> keys = redisTemplate.keys("dish_*");
        // if (keys != null) {
        //     redisTemplate.delete(keys);
        // }*/

        // 清理该分类下面的缓存
        String key = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(key);

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 查询列表
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @param name     菜品名称 模糊查询
     * @return 封装类
     */
    @Override
    public PageBean<DishDto> page(int page, int pageSize, String name) {

        // 封裝dish的 page
        Page<Dish> dpage = new Page<>(page, pageSize);

        Page<Dish> dishPage = lambdaQuery()
                .like(!StringUtils.isEmpty(name), Dish::getName, name)
                .orderByDesc(Dish::getUpdateTime)
                .page(dpage);

        // 处理没有name的records，遍历每一条数据
        List<Dish> dishPageRecords = dishPage.getRecords();
        List<DishDto> dishDtoRecords = dishPageRecords.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            // 将普通属性拷给dishDto, id,name,price...
            BeanUtils.copyProperties(item, dishDto);
            // 查出对象，获得名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        return new PageBean<>((int) dishPage.getTotal(), dishDtoRecords);
    }

    /**
     * 根据id查询菜品信息及口味信息
     *
     * @param id 菜品id
     * @return 菜品数据传输对象
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息，从dish查询
        Dish dish = this.getById(id);
        // 拷贝属性
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        // 查询当前菜品对应的口味信息，从 dish_flavor 表查询
        List<DishFlavor> flavorList = dishFlavorService.lambdaQuery()
                .eq(DishFlavor::getDishId, dish.getId())
                .list();
        dishDto.setFlavors(flavorList);

        return dishDto;
    }

    /**
     * 更新dish dish_flavor
     *
     * @param dishDto 修改后的 dish 数据传输对象
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表基本信息(name,price等)
        this.updateById(dishDto);

        // 清理该分类下面的缓存
        String key = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(key);

        // 清理当前菜品对应口味数据---dish_flavor表的delete操作
        // 一道菜一个口味是一条数据，一道菜四个口味则在库里有四条数据。更新不能删除原本存在的数据，只能全删再插
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().peek((item) -> item.setDishId(dishDto.getId()))
                .collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }


    /**
     * 删除菜品的同时删除口味表
     *
     * @param ids 菜品id 集合
     * @throws Exception 删除图片时发生的异常
     */
    @Override
    public void deleteWithFlavor(List<String> ids) throws Exception {

        // 对每个id 执行 删除dish表，图片，dish_flavor表操作。
        for (String id : ids) {
            Dish dish = this.getById(id);
            // 根据图片网址，删除图片
            aliOSSUtils.delete(dish.getImage());

            // 清理该分类下面的缓存
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);

            // 删除 dish_flavor 表对应数据
            LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
            lqw.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(lqw);
            // 删除dish表数据
            this.removeById(id);
        }
    }


    /**
     * 批量起售与批量停售
     *
     * @param dishStatus 修改后的状态，1起售，0停售
     * @param ids        需要转换的ids集合
     */
    @Override
    public void changeBatchDishStatusByIds(int dishStatus, List<String> ids) {
        this.lambdaUpdate()
                .in(Dish::getId, ids)
                .set(Dish::getStatus, dishStatus)
                .update();

        // 修改状态不走update方法，需要删除缓存，直接删除全部缓存
        Set<String> keys = redisTemplate.keys("dish_*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 查询某个菜品分类下，所拥有的的菜品及对应口味数据
     *
     * @param dish 封装 菜品 分类id的 dish对象
     * @return 菜品传输对象dto集合
     */
    @Override
    public List<DishDto> getListWithFlavor(Dish dish) {

        List<DishDto> ret;

        // 根据 分类 构造 key， value:（每个分类下的菜品）
        String key = "dish_" + dish.getCategoryId();
        // 先从redis获取数据，
        ret = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (ret != null) {
            // 有则直接返回，无需查询数据库。
            return ret;
        }
        // 不存在，查询数据库
        List<Dish> list = this.lambdaQuery()
                .eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .like(!StringUtils.isEmpty(dish.getName()), Dish::getName, dish.getName())  // 名称查询
                .eq(Dish::getStatus, 1)  // 只查起售的菜
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime)
                .list();

        // 封装口味数据 和 分类名称

        ret = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            Long dishId = item.getId();
            List<DishFlavor> dishFlavorList = dishFlavorService.lambdaQuery()
                    .eq(DishFlavor::getDishId, dishId).list();

            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        // 存到缓存，有效期60min
        redisTemplate.opsForValue().set(key, ret, 60, TimeUnit.MINUTES);

        return ret;

    }


}
