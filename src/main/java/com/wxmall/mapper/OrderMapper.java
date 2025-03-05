package com.wxmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxmall.model.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 * 
 * @author System Architect
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
