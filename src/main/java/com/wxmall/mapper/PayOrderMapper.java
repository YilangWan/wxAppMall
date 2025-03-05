package com.wxmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxmall.model.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单Mapper接口
 * 
 * @author System Architect
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
