package com.wxmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxmall.model.PayLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付日志Mapper接口
 * 
 * @author System Architect
 */
@Mapper
public interface PayLogMapper extends BaseMapper<PayLog> {
}
