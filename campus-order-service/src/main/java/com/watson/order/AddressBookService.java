package com.watson.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watson.order.po.AddressBook;

public interface AddressBookService extends IService<AddressBook> {

    void setDefaultAddress(AddressBook addressBook, Long userId);
}
