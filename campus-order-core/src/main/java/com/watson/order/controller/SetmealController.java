package com.watson.order.controller;

import com.watson.order.SetmealService;
import com.watson.order.aop.EmpLog;
import com.watson.order.dto.PageBean;
import com.watson.order.dto.Result;
import com.watson.order.dto.SetmealDto;
import com.watson.order.po.Setmeal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    private final SetmealService setmealService;

    /**
     * 新增套餐
     *
     * @param setmealDto 封装前端 套餐信息
     * @return 新增结果信息
     */
    @EmpLog
    @PostMapping
    @ApiOperation(value = "新增套餐接口")
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("新增套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功!");
    }

    /**
     * 套餐分页查询
     *
     * @param page     页码
     * @param pageSize 每页显示数量
     * @param name     套餐名称 模糊查询
     * @return 包含 查询结果 包装类 的 统一返回对象
     */
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "name", value = "套餐名称", dataTypeClass = String.class)})
    public Result<PageBean<SetmealDto>> page(int page, int pageSize, String name) {

        PageBean<SetmealDto> setmealDtoPageBean = setmealService.page(page, pageSize, name);

        return Result.success(setmealDtoPageBean);
    }

    /**
     * 删除套餐同时删除 set_meal_dish 表数据
     * 删除时删除 setmealCache 下所有缓存
     *
     * @param ids 套餐 id 集合
     * @return 操作结果信息
     */
    @EmpLog
    @DeleteMapping
    @ApiOperation(value = "套餐删除接口")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("ids：{}", ids);
        setmealService.removeWithDish(ids);
        return Result.success("删除套餐数据成功!");
    }

    /**
     * 手机端 查询某个套餐分类内的菜品
     * 前端发请求为：<a href="http://localhost:8080/setmeal/list?categoryId=____&status=1">...</a>
     *
     * @param setmeal 包含 categoryId 和 status 的套餐对象
     * @return 分类下套餐集合
     */
    @GetMapping("/list")
    @ApiOperation(value = "套餐条件查询接口")
    public Result<List<Setmeal>> list(Setmeal setmeal) {

        log.info("get in setMeal controller, setMeal:{}", setmeal);
        List<Setmeal> list = setmealService.getList(setmeal);

        return Result.success(list);
    }

    /**
     * 修改套餐时获取菜品信息进行回显
     *
     * @param id 套餐id
     * @return 套餐数据 的 统一结果封装对象
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "修改套餐时获取菜品信息接口")
    public Result<SetmealDto> get(@PathVariable Long id) {
        log.info("get id for update set_meal: {}", id);
        SetmealDto setmealDto = setmealService.getWithDish(id);
        return Result.success(setmealDto);
    }


    /**
     * 修改套餐，同时修改 set_meal_dish 套餐和菜品对应表
     *
     * @param setmealDto 前端传输的 套餐对象
     * @return 操作结果信息
     */
    @EmpLog
    @PutMapping
    @ApiOperation(value = "更新套餐接口")
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("update setMealDto: {}", setmealDto.toString());
        setmealService.updateWithDish(setmealDto);

        return Result.success("修改套餐成功!");
    }


    /**
     * 批量，单个 修改菜品状态信息
     *
     * @param setmealStatus 修改后的状态，1起售，0停售
     * @param ids           需要转换的 id 集合
     * @return 操作结果信息
     */
    @EmpLog
    @DeleteMapping("/status/{setmealStatus}")
    @ApiOperation(value = "修改套餐状态接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "setmealStatus", value = "修改后的套餐状态", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "ids", value = "套餐id集合", dataTypeClass = List.class, required = true)
    })
    public Result<String> statusHandle(@PathVariable("setmealStatus") int setmealStatus, @RequestBody List<String> ids) {
        log.info("change status to: {}, ids: {}", setmealStatus, ids);
        setmealService.statusHandle(setmealStatus, ids);
        return Result.success("修改状态成功!");
    }

}
