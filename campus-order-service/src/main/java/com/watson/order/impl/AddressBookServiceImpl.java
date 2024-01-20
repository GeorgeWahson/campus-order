package com.watson.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watson.order.AddressBookService;
import com.watson.order.mapper.AddressBookMapper;
import com.watson.order.po.AddressBook;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    /**
     * 将某个地址设置为用户默认地址
     *
     * @param addressBook 设置为默认地址的 地址对象
     * @param userId      用户id
     */
    @Override
    public void setDefaultAddress(AddressBook addressBook, Long userId) {
        // 设置该用户所有地址 id_default 为 0
        // SQL:update address_book set is_default = 0 where user_id = ?
        this.lambdaUpdate()
                .eq(AddressBook::getUserId, userId)
                .set(AddressBook::getIsDefault, 0).update();
        // 将要修改的数据改为1
        addressBook.setIsDefault(1);
        // SQL:update address_book set is_default = 1 where id = ?
        this.updateById(addressBook);
    }
}
