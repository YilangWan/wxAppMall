package com.wxmall.service;

import com.wxmall.dto.PayOrderDTO;
import com.wxmall.dto.RefundOrderDTO;
import com.wxmall.model.PayOrder;
import com.wxmall.model.RefundOrder;
import com.wxmall.vo.WxPayVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 支付服务接口
 * 
 * @author System Architect
 */
public interface PayService {
    
    /**
     * 创建支付订单
     *
     * @param payOrderDTO 支付订单DTO
     * @return 微信支付返回结果
     */
    WxPayVO createPayOrder(PayOrderDTO payOrderDTO);
    
    /**
     * 查询支付订单
     *
     * @param orderNo 订单编号
     * @return 支付订单
     */
    PayOrder queryPayOrder(String orderNo);
    
    /**
     * 关闭支付订单
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    boolean closePayOrder(String orderNo);
    
    /**
     * 处理支付结果通知
     *
     * @param request  请求
     * @param response 响应
     */
    void handlePayNotify(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 申请退款
     *
     * @param refundOrderDTO 退款订单DTO
     * @return 退款结果
     */
    Map<String, Object> refund(RefundOrderDTO refundOrderDTO);
    
    /**
     * 查询退款订单
     *
     * @param refundNo 退款单号
     * @return 退款订单
     */
    RefundOrder queryRefundOrder(String refundNo);
    
    /**
     * 处理退款结果通知
     *
     * @param request  请求
     * @param response 响应
     */
    void handleRefundNotify(HttpServletRequest request, HttpServletResponse response);
}
