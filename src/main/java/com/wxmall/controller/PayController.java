package com.wxmall.controller;

import com.wxmall.common.Result;
import com.wxmall.dto.PayOrderDTO;
import com.wxmall.dto.RefundOrderDTO;
import com.wxmall.model.PayOrder;
import com.wxmall.model.RefundOrder;
import com.wxmall.service.PayService;
import com.wxmall.vo.WxPayVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 支付控制器
 *
 * @author wxmall
 * @date 2023-08-01
 */
@RestController
@RequestMapping("/api/pay")
@Slf4j
public class PayController {

    @Autowired
    private PayService payService;

    /**
     * 创建支付订单
     *
     * @param payOrderDTO 支付订单DTO
     * @return 支付参数
     */
    @PostMapping("/create")
    public Result<WxPayVO> createPayOrder(@RequestBody PayOrderDTO payOrderDTO) {
        log.info("创建支付订单，参数：{}", payOrderDTO);
        WxPayVO wxPayVO = payService.createPayOrder(payOrderDTO);
        return Result.success(wxPayVO);
    }

    /**
     * 查询支付订单
     *
     * @param orderNo 订单编号
     * @return 支付订单
     */
    @GetMapping("/query/{orderNo}")
    public Result<PayOrder> queryPayOrder(@PathVariable String orderNo) {
        log.info("查询支付订单，订单编号：{}", orderNo);
        PayOrder payOrder = payService.queryPayOrder(orderNo);
        return Result.success(payOrder);
    }

    /**
     * 关闭支付订单
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @PostMapping("/close/{orderNo}")
    public Result<Boolean> closePayOrder(@PathVariable String orderNo) {
        log.info("关闭支付订单，订单编号：{}", orderNo);
        boolean result = payService.closePayOrder(orderNo);
        return Result.success(result);
    }

    /**
     * 处理支付结果通知
     *
     * @param request  请求
     * @param response 响应
     */
    @PostMapping("/notify")
    public void handlePayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("处理支付结果通知");
        payService.handlePayNotify(request, response);
    }

    /**
     * 申请退款
     *
     * @param refundOrderDTO 退款订单DTO
     * @return 退款结果
     */
    @PostMapping("/refund")
    public Result<Map<String, Object>> refund(@RequestBody RefundOrderDTO refundOrderDTO) {
        log.info("申请退款，参数：{}", refundOrderDTO);
        Map<String, Object> result = payService.refund(refundOrderDTO);
        return Result.success(result);
    }

    /**
     * 查询退款订单
     *
     * @param refundNo 退款单号
     * @return 退款订单
     */
    @GetMapping("/refund/query/{refundNo}")
    public Result<RefundOrder> queryRefundOrder(@PathVariable String refundNo) {
        log.info("查询退款订单，退款单号：{}", refundNo);
        RefundOrder refundOrder = payService.queryRefundOrder(refundNo);
        return Result.success(refundOrder);
    }

    /**
     * 处理退款结果通知
     *
     * @param request  请求
     * @param response 响应
     */
    @PostMapping("/refund/notify")
    public void handleRefundNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("处理退款结果通知");
        payService.handleRefundNotify(request, response);
    }
}
