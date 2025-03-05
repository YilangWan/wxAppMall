package com.wxmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxmall.model.RefundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款订单Mapper接口
 * 
 * @author System Architect
 */
@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {
}
