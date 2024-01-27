package com.watson.order.controller;

import com.watson.order.AddressBookService;
import com.watson.order.common.BaseContext;
import com.watson.order.dto.Result;
import com.watson.order.po.AddressBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
@RequiredArgsConstructor
public class AddressBookController {

    private final AddressBookService addressBookService;

    /**
     * 新增地址
     *
     * @param addressBook 包含信息的实体对象
     * @return 该对象的统一结果封装对象
     */
    @PostMapping
    public Result<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId()); // 设置当前用户的id
        log.info("controller save addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    /**
     * 将某个地址设置为用户默认地址
     *
     * @param addressBook 设置为默认地址的 地址对象
     * @return 该对象的统一结果封装对象
     */
    @PutMapping("default")
    public Result<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("set Default addressBook:{}", addressBook);

        addressBookService.setDefaultAddress(addressBook, BaseContext.getCurrentId());

        return Result.success(addressBook);
    }


    /**
     * 根据 地址id 查询地址
     *
     * @param id 地址id
     * @return 统一结果封装对象
     */
    @GetMapping("/{id}")
    public Result<AddressBook> get(@PathVariable Long id) {
        log.info("get address by Address id: {}", id);
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return Result.success(addressBook);
        } else {
            return Result.error("没有找到该对象");
        }
    }


    /**
     * 查询默认地址
     *
     * @return 包含默认地址的 统一结果封装对象
     */
    @GetMapping("default")
    public Result<AddressBook> getDefault() {

        // SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.lambdaQuery()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1)
                .one();

        if (null == addressBook) {
            return Result.error("没有找到该对象");
        } else {
            return Result.success(addressBook);
        }
    }


    /**
     * 查询指定用户的全部地址
     *
     * @param addressBook 地址对象
     * @return 用户地址集合，统一封装对象
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        // SQL:select * from address_book where user_id = ? order by update_time desc
        List<AddressBook> list = addressBookService.lambdaQuery()
                .eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId())
                .orderByDesc(AddressBook::getIsDefault)
                .orderByDesc(AddressBook::getUpdateTime)
                .list();

        return Result.success(list);
    }

    /**
     * 修改地址
     *
     * @param addressBook 修改后的地址对象
     * @return 操作结果信息
     */
    @PutMapping
    public Result<String> updateAddress(@RequestBody AddressBook addressBook) {

        log.info("update addressBook: {}", addressBook);
        boolean b = addressBookService.updateById(addressBook);

//        boolean updated = addressBookService.lambdaUpdate()
//                .eq(AddressBook::getId, addressBook.getId())
//                .update(addressBook);

        return b ? Result.success("修改成功") : Result.error("修改失败");
    }

}
