package com.watson.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.watson.order.po.AddressBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
