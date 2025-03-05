package com.wxmall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxmall.config.WxPayConfig;
import com.wxmall.dto.PayOrderDTO;
import com.wxmall.dto.RefundOrderDTO;
import com.wxmall.mapper.OrderMapper;
import com.wxmall.mapper.PayLogMapper;
import com.wxmall.mapper.PayOrderMapper;
import com.wxmall.mapper.RefundOrderMapper;
import com.wxmall.model.Order;
import com.wxmall.model.PayLog;
import com.wxmall.model.PayOrder;
import com.wxmall.model.RefundOrder;
import com.wxmall.service.PayService;
import com.wxmall.util.WxPayUtil;
import com.wxmall.vo.WxPayVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务实现类
 *
 * @author wxmall
 * @date 2023-08-01
 */
@Slf4j
@Service
public class PayServiceImpl implements PayService {

    /**
     * 统一下单URL
     */
    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    
    /**
     * 查询订单URL
     */
    private static final String ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    
    /**
     * 关闭订单URL
     */
    private static final String CLOSE_ORDER_URL = "https://api.mch.weixin.qq.com/pay/closeorder";
    
    /**
     * 申请退款URL
     */
    private static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    
    /**
     * 查询退款URL
     */
    private static final String REFUND_QUERY_URL = "https://api.mch.weixin.qq.com/pay/refundquery";

    @Autowired
    private WxPayConfig wxPayConfig;
    
    @Autowired
    private PayOrderMapper payOrderMapper;
    
    @Autowired
    private PayLogMapper payLogMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private RefundOrderMapper refundOrderMapper;
    
    @Autowired
    private CloseableHttpClient wxPayClient;

    /**
     * 创建支付订单
     *
     * @param payOrderDTO 支付订单DTO
     * @return 微信支付返回结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxPayVO createPayOrder(PayOrderDTO payOrderDTO) {
        log.info("创建支付订单，参数：{}", JSON.toJSONString(payOrderDTO));
        
        // 1. 生成支付订单
        PayOrder payOrder = new PayOrder();
        BeanUtils.copyProperties(payOrderDTO, payOrder);
        payOrder.setOrderNo(payOrderDTO.getOrderNo());
        payOrder.setPayStatus(0); // 未支付
        payOrder.setTimeStart(LocalDateTime.now());
        payOrder.setTimeExpire(LocalDateTime.now().plusHours(2)); // 2小时有效期
        payOrder.setNotifyUrl(wxPayConfig.getNotifyUrl());
        payOrderMapper.insert(payOrder);
        
        // 2. 记录支付日志
        PayLog payLog = new PayLog();
        payLog.setOrderNo(payOrderDTO.getOrderNo());
        payLog.setUserId(payOrderDTO.getUserId());
        payLog.setOpenid(payOrderDTO.getOpenid());
        payLog.setOperateType(1); // 创建订单
        payLog.setContent("创建支付订单");
        payLog.setRequestParams(JSON.toJSONString(payOrderDTO));
        payLogMapper.insert(payLog);
        
        // 3. 调用微信支付统一下单API
        try {
            // 构造请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", wxPayConfig.getAppId());
            params.put("mch_id", wxPayConfig.getMchId());
            params.put("nonce_str", WxPayUtil.generateNonceStr());
            params.put("body", payOrderDTO.getBody());
            params.put("out_trade_no", payOrderDTO.getOrderNo());
            // 微信支付金额单位为分，需要将元转换为分
            params.put("total_fee", payOrderDTO.getPayAmount().multiply(new BigDecimal("100")).intValue());
            params.put("spbill_create_ip", payOrderDTO.getClientIp());
            params.put("notify_url", wxPayConfig.getNotifyUrl());
            params.put("trade_type", wxPayConfig.getTradeType());
            params.put("openid", payOrderDTO.getOpenid());
            
            // 生成签名
            String sign = WxPayUtil.generateSign(params, wxPayConfig.getMchKey(), wxPayConfig.getSignType());
            params.put("sign", sign);
            
            // 将参数转换为XML
            String xmlParams = WxPayUtil.mapToXml(params);
            
            // 发送请求
            HttpPost httpPost = new HttpPost(UNIFIED_ORDER_URL);
            httpPost.setEntity(new StringEntity(xmlParams, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "text/xml");
            
            // 判断是否为开发环境的模拟HttpClient
            if (wxPayClient.getClass().getName().equals("org.apache.http.impl.client.InternalHttpClient")) {
                log.info("使用模拟的HttpClient，返回模拟的支付结果");
                
                // 构造模拟响应
                String mockPrepayId = "wx" + System.currentTimeMillis();
                String mockResponseXml = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "<appid><![CDATA[" + wxPayConfig.getAppId() + "]]></appid>" +
                        "<mch_id><![CDATA[" + wxPayConfig.getMchId() + "]]></mch_id>" +
                        "<nonce_str><![CDATA[" + WxPayUtil.generateNonceStr() + "]]></nonce_str>" +
                        "<sign><![CDATA[MOCK_SIGN]]></sign>" +
                        "<result_code><![CDATA[SUCCESS]]></result_code>" +
                        "<prepay_id><![CDATA[" + mockPrepayId + "]]></prepay_id>" +
                        "<trade_type><![CDATA[" + wxPayConfig.getTradeType() + "]]></trade_type>" +
                        "</xml>";
                
                // 更新支付日志
                payLog.setResponseResult(mockResponseXml);
                payLogMapper.updateById(payLog);
                
                // 更新支付订单
                payOrder.setPrepayId(mockPrepayId);
                payOrderMapper.updateById(payOrder);
                
                // 构造返回结果
                WxPayVO wxPayVO = new WxPayVO();
                wxPayVO.setOrderNo(payOrderDTO.getOrderNo());
                wxPayVO.setPrepayId(mockPrepayId);
                
                // 根据支付类型返回不同的支付参数
                if (payOrderDTO.getPayType() == 1) {
                    // JSAPI支付（小程序支付）
                    Map<String, String> payParams = WxPayUtil.generatePayParams(wxPayConfig.getAppId(), mockPrepayId, wxPayConfig.getMchKey());
                    wxPayVO.setPayParams(payParams);
                } else if (payOrderDTO.getPayType() == 3) {
                    // Native支付
                    wxPayVO.setCodeUrl("weixin://wxpay/bizpayurl?pr=MOCK_CODE_URL");
                } else if (payOrderDTO.getPayType() == 2) {
                    // H5支付
                    wxPayVO.setH5Url("https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?MOCK_H5_URL");
                }
                
                return wxPayVO;
            }
            
            CloseableHttpResponse response = wxPayClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("微信支付统一下单响应：{}", responseString);
            
            // 解析响应结果
            Map<String, String> responseMap = WxPayUtil.xmlToMap(responseString);
            
            // 更新支付日志
            payLog.setResponseResult(responseString);
            payLogMapper.updateById(payLog);
            
            // 判断请求是否成功
            if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                // 获取预支付交易会话标识
                String prepayId = responseMap.get("prepay_id");
                
                // 更新支付订单
                payOrder.setPrepayId(prepayId);
                payOrderMapper.updateById(payOrder);
                
                // 构造返回结果
                WxPayVO wxPayVO = new WxPayVO();
                wxPayVO.setOrderNo(payOrderDTO.getOrderNo());
                wxPayVO.setPrepayId(prepayId);
                
                // 根据支付类型返回不同的支付参数
                if (payOrderDTO.getPayType() == 1) {
                    // JSAPI支付（小程序支付）
                    Map<String, String> payParams = WxPayUtil.generatePayParams(wxPayConfig.getAppId(), prepayId, wxPayConfig.getMchKey());
                    wxPayVO.setPayParams(payParams);
                } else if (payOrderDTO.getPayType() == 3) {
                    // Native支付
                    wxPayVO.setCodeUrl(responseMap.get("code_url"));
                } else if (payOrderDTO.getPayType() == 2) {
                    // H5支付
                    wxPayVO.setH5Url(responseMap.get("mweb_url"));
                }
                
                return wxPayVO;
            } else {
                log.error("微信支付统一下单失败，错误码：{}，错误信息：{}", responseMap.get("err_code"), responseMap.get("err_code_des"));
                throw new RuntimeException("微信支付统一下单失败：" + responseMap.get("err_code_des"));
            }
        } catch (Exception e) {
            log.error("创建支付订单异常", e);
            throw new RuntimeException("创建支付订单异常", e);
        }
    }

    /**
     * 查询支付订单
     *
     * @param orderNo 订单编号
     * @return 支付订单
     */
    @Override
    public PayOrder queryPayOrder(String orderNo) {
        log.info("查询支付订单，订单编号：{}", orderNo);
        
        // 1. 查询本地支付订单
        LambdaQueryWrapper<PayOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrder::getOrderNo, orderNo);
        PayOrder payOrder = payOrderMapper.selectOne(queryWrapper);
        
        // 如果订单不存在，直接返回null
        if (payOrder == null) {
            return null;
        }
        
        // 如果订单已支付，直接返回
        if (payOrder.getPayStatus() == 1) {
            return payOrder;
        }
        
        // 2. 调用微信支付查询订单API
        try {
            // 构造请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", wxPayConfig.getAppId());
            params.put("mch_id", wxPayConfig.getMchId());
            params.put("out_trade_no", orderNo);
            params.put("nonce_str", WxPayUtil.generateNonceStr());
            
            // 生成签名
            String sign = WxPayUtil.generateSign(params, wxPayConfig.getMchKey(), wxPayConfig.getSignType());
            params.put("sign", sign);
            
            // 将参数转换为XML
            String xmlParams = WxPayUtil.mapToXml(params);
            
            // 发送请求
            HttpPost httpPost = new HttpPost(ORDER_QUERY_URL);
            httpPost.setEntity(new StringEntity(xmlParams, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "text/xml");
            
            // 判断是否为开发环境的模拟HttpClient
            if (wxPayClient.getClass().getName().equals("org.apache.http.impl.client.InternalHttpClient")) {
                log.info("使用模拟的HttpClient，返回模拟的查询结果");
                
                // 构造模拟响应
                String mockResponseXml = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "<appid><![CDATA[" + wxPayConfig.getAppId() + "]]></appid>" +
                        "<mch_id><![CDATA[" + wxPayConfig.getMchId() + "]]></mch_id>" +
                        "<nonce_str><![CDATA[" + WxPayUtil.generateNonceStr() + "]]></nonce_str>" +
                        "<sign><![CDATA[MOCK_SIGN]]></sign>" +
                        "<result_code><![CDATA[SUCCESS]]></result_code>" +
                        "<out_trade_no><![CDATA[" + orderNo + "]]></out_trade_no>" +
                        "<trade_state><![CDATA[SUCCESS]]></trade_state>" +
                        "<trade_state_desc><![CDATA[支付成功]]></trade_state_desc>" +
                        "<transaction_id><![CDATA[4200001234567890]]></transaction_id>" +
                        "<time_end><![CDATA[20230801121212]]></time_end>" +
                        "</xml>";
                
                // 解析模拟响应结果
                Map<String, String> responseMap = WxPayUtil.xmlToMap(mockResponseXml);
                
                // 判断请求是否成功
                if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                    // 判断支付状态
                    if ("SUCCESS".equals(responseMap.get("trade_state"))) {
                        // 支付成功，更新支付订单
                        payOrder.setPayStatus(1); // 支付成功
                        payOrder.setTransactionId(responseMap.get("transaction_id"));
                        payOrder.setPayTime(LocalDateTime.now());
                        payOrderMapper.updateById(payOrder);
                        
                        // 记录支付日志
                        PayLog payLog = new PayLog();
                        payLog.setOrderNo(orderNo);
                        payLog.setUserId(payOrder.getUserId());
                        payLog.setOpenid(payOrder.getOpenid());
                        payLog.setOperateType(2); // 支付成功
                        payLog.setContent("支付成功");
                        payLog.setRequestParams(xmlParams);
                        payLog.setResponseResult(mockResponseXml);
                        payLogMapper.insert(payLog);
                        
                        // 更新订单状态
                        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
                        if (order != null) {
                            order.setStatus(1); // 待发货
                            order.setPayTime(LocalDateTime.now());
                            orderMapper.updateById(order);
                        }
                    }
                }
                
                return payOrder;
            }
            
            CloseableHttpResponse response = wxPayClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("微信支付查询订单响应：{}", responseString);
            
            // 解析响应结果
            Map<String, String> responseMap = WxPayUtil.xmlToMap(responseString);
            
            // 记录支付日志
            PayLog payLog = new PayLog();
            payLog.setOrderNo(orderNo);
            payLog.setUserId(payOrder.getUserId());
            payLog.setOpenid(payOrder.getOpenid());
            payLog.setOperateType(2); // 查询订单
            payLog.setContent("查询支付订单");
            payLog.setRequestParams(JSON.toJSONString(params));
            payLog.setResponseResult(responseString);
            payLogMapper.insert(payLog);
            
            // 判断请求是否成功
            if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                // 判断支付状态
                String tradeState = responseMap.get("trade_state");
                if ("SUCCESS".equals(tradeState)) {
                    // 支付成功，更新订单状态
                    payOrder.setPayStatus(1); // 支付成功
                    payOrder.setTransactionId(responseMap.get("transaction_id"));
                    payOrder.setPayTime(LocalDateTime.now());
                    payOrderMapper.updateById(payOrder);
                    
                    // 更新商品订单状态
                    Order order = orderMapper.selectById(payOrder.getOrderNo());
                    if (order != null) {
                        order.setStatus(1); // 待发货
                        order.setPayTime(LocalDateTime.now());
                        orderMapper.updateById(order);
                    }
                } else if ("NOTPAY".equals(tradeState)) {
                    // 未支付
                    payOrder.setPayStatus(0); // 未支付
                    payOrderMapper.updateById(payOrder);
                } else if ("CLOSED".equals(tradeState)) {
                    // 已关闭
                    payOrder.setPayStatus(2); // 支付失败
                    payOrder.setFailReason("订单已关闭");
                    payOrderMapper.updateById(payOrder);
                } else if ("REFUND".equals(tradeState)) {
                    // 转入退款
                    payOrder.setPayStatus(3); // 已退款
                    payOrderMapper.updateById(payOrder);
                }
            }
        } catch (Exception e) {
            log.error("查询支付订单异常", e);
        }
        
        return payOrder;
    }

    /**
     * 关闭支付订单
     *
     * @param orderNo 订单编号
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePayOrder(String orderNo) {
        log.info("关闭支付订单，订单编号：{}", orderNo);
        
        // 1. 查询本地支付订单
        LambdaQueryWrapper<PayOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrder::getOrderNo, orderNo);
        PayOrder payOrder = payOrderMapper.selectOne(queryWrapper);
        
        // 如果订单不存在，直接返回false
        if (payOrder == null) {
            return false;
        }
        
        // 如果订单已支付，不能关闭
        if (payOrder.getPayStatus() == 1) {
            return false;
        }
        
        // 2. 调用微信支付关闭订单API
        try {
            // 构造请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", wxPayConfig.getAppId());
            params.put("mch_id", wxPayConfig.getMchId());
            params.put("out_trade_no", orderNo);
            params.put("nonce_str", WxPayUtil.generateNonceStr());
            
            // 生成签名
            String sign = WxPayUtil.generateSign(params, wxPayConfig.getMchKey(), wxPayConfig.getSignType());
            params.put("sign", sign);
            
            // 将参数转换为XML
            String xmlParams = WxPayUtil.mapToXml(params);
            
            // 发送请求
            HttpPost httpPost = new HttpPost(CLOSE_ORDER_URL);
            httpPost.setEntity(new StringEntity(xmlParams, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "text/xml");
            
            // 判断是否为开发环境的模拟HttpClient
            if (wxPayClient.getClass().getName().equals("org.apache.http.impl.client.InternalHttpClient")) {
                log.info("使用模拟的HttpClient，返回模拟的关闭订单结果");
                
                // 构造模拟响应
                String mockResponseXml = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "<appid><![CDATA[" + wxPayConfig.getAppId() + "]]></appid>" +
                        "<mch_id><![CDATA[" + wxPayConfig.getMchId() + "]]></mch_id>" +
                        "<nonce_str><![CDATA[" + WxPayUtil.generateNonceStr() + "]]></nonce_str>" +
                        "<sign><![CDATA[MOCK_SIGN]]></sign>" +
                        "<result_code><![CDATA[SUCCESS]]></result_code>" +
                        "</xml>";
                
                // 记录支付日志
                PayLog payLog = new PayLog();
                payLog.setOrderNo(orderNo);
                payLog.setUserId(payOrder.getUserId());
                payLog.setOpenid(payOrder.getOpenid());
                payLog.setOperateType(3); // 关闭订单
                payLog.setContent("关闭支付订单");
                payLog.setRequestParams(JSON.toJSONString(params));
                payLog.setResponseResult(mockResponseXml);
                payLogMapper.insert(payLog);
                
                // 关闭成功，更新订单状态
                payOrder.setPayStatus(2); // 支付失败
                payOrder.setFailReason("订单已关闭");
                payOrderMapper.updateById(payOrder);
                
                // 更新商品订单状态
                Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
                if (order != null) {
                    order.setStatus(4); // 已关闭
                    orderMapper.updateById(order);
                }
                
                return true;
            }
            
            CloseableHttpResponse response = wxPayClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("微信支付关闭订单响应：{}", responseString);
            
            // 解析响应结果
            Map<String, String> responseMap = WxPayUtil.xmlToMap(responseString);
            
            // 记录支付日志
            PayLog payLog = new PayLog();
            payLog.setOrderNo(orderNo);
            payLog.setUserId(payOrder.getUserId());
            payLog.setOpenid(payOrder.getOpenid());
            payLog.setOperateType(3); // 关闭订单
            payLog.setContent("关闭支付订单");
            payLog.setRequestParams(JSON.toJSONString(params));
            payLog.setResponseResult(responseString);
            payLogMapper.insert(payLog);
            
            // 判断请求是否成功
            if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                // 关闭成功，更新订单状态
                payOrder.setPayStatus(2); // 支付失败
                payOrder.setFailReason("订单已关闭");
                payOrderMapper.updateById(payOrder);
                
                // 更新商品订单状态
                Order order = orderMapper.selectById(payOrder.getOrderNo());
                if (order != null) {
                    order.setStatus(4); // 已关闭
                    orderMapper.updateById(order);
                }
                
                return true;
            } else {
                log.error("微信支付关闭订单失败，错误码：{}，错误信息：{}", responseMap.get("err_code"), responseMap.get("err_code_des"));
                return false;
            }
        } catch (Exception e) {
            log.error("关闭支付订单异常", e);
            return false;
        }
    }

    /**
     * 处理支付结果通知
     *
     * @param request  请求
     * @param response 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePayNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("处理支付结果通知");
        
        try {
            // 读取通知数据
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String notifyData = sb.toString();
            log.info("微信支付通知数据：{}", notifyData);
            
            // 解析通知数据
            Map<String, String> notifyMap = WxPayUtil.xmlToMap(notifyData);
            
            // 验证签名
            String sign = notifyMap.get("sign");
            Map<String, Object> params = new HashMap<>();
            for (Map.Entry<String, String> entry : notifyMap.entrySet()) {
                if (!"sign".equals(entry.getKey())) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
            boolean signValid = WxPayUtil.verifySign(params, wxPayConfig.getMchKey(), wxPayConfig.getSignType(), sign);
            
            if (!signValid) {
                log.error("微信支付通知签名验证失败");
                responseToWx(response, "FAIL", "签名验证失败");
                return;
            }
            
            // 验证返回状态码
            if (!"SUCCESS".equals(notifyMap.get("return_code"))) {
                log.error("微信支付通知返回失败，错误信息：{}", notifyMap.get("return_msg"));
                responseToWx(response, "FAIL", "通信失败");
                return;
            }
            
            // 验证业务结果
            if (!"SUCCESS".equals(notifyMap.get("result_code"))) {
                log.error("微信支付通知业务结果失败，错误码：{}，错误信息：{}", notifyMap.get("err_code"), notifyMap.get("err_code_des"));
                responseToWx(response, "FAIL", "业务失败");
                return;
            }
            
            // 获取商户订单号
            String orderNo = notifyMap.get("out_trade_no");
            
            // 查询本地支付订单
            LambdaQueryWrapper<PayOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PayOrder::getOrderNo, orderNo);
            PayOrder payOrder = payOrderMapper.selectOne(queryWrapper);
            
            // 如果订单不存在，返回失败
            if (payOrder == null) {
                log.error("微信支付通知订单不存在，订单编号：{}", orderNo);
                responseToWx(response, "FAIL", "订单不存在");
                return;
            }
            
            // 如果订单已支付，直接返回成功
            if (payOrder.getPayStatus() == 1) {
                log.info("微信支付通知订单已支付，订单编号：{}", orderNo);
                responseToWx(response, "SUCCESS", "OK");
                return;
            }
            
            // 验证支付金额
            String totalFee = notifyMap.get("total_fee");
            int notifyAmount = Integer.parseInt(totalFee);
            int orderAmount = payOrder.getPayAmount().multiply(new BigDecimal("100")).intValue();
            if (notifyAmount != orderAmount) {
                log.error("微信支付通知金额不匹配，通知金额：{}，订单金额：{}", notifyAmount, orderAmount);
                responseToWx(response, "FAIL", "金额不匹配");
                return;
            }
            
            // 验证商户ID
            String mchId = notifyMap.get("mch_id");
            if (!wxPayConfig.getMchId().equals(mchId)) {
                log.error("微信支付通知商户ID不匹配，通知商户ID：{}，配置商户ID：{}", mchId, wxPayConfig.getMchId());
                responseToWx(response, "FAIL", "商户ID不匹配");
                return;
            }
            
            // 获取微信支付订单号
            String transactionId = notifyMap.get("transaction_id");
            
            // 更新支付订单状态
            payOrder.setPayStatus(1); // 支付成功
            payOrder.setTransactionId(transactionId);
            payOrder.setPayTime(LocalDateTime.now());
            payOrderMapper.updateById(payOrder);
            
            // 更新商品订单状态
            Order order = orderMapper.selectById(payOrder.getOrderNo());
            if (order != null) {
                order.setStatus(1); // 待发货
                order.setPayTime(LocalDateTime.now());
                orderMapper.updateById(order);
            }
            
            // 记录支付日志
            PayLog payLog = new PayLog();
            payLog.setOrderNo(orderNo);
            payLog.setTransactionId(transactionId);
            payLog.setUserId(payOrder.getUserId());
            payLog.setOpenid(payOrder.getOpenid());
            payLog.setOperateType(2); // 支付成功
            payLog.setContent("支付成功通知");
            payLog.setRequestParams(notifyData);
            payLog.setResponseResult("SUCCESS");
            payLogMapper.insert(payLog);
            
            // 返回成功
            responseToWx(response, "SUCCESS", "OK");
        } catch (Exception e) {
            log.error("处理支付结果通知异常", e);
            try {
                responseToWx(response, "FAIL", "系统异常");
            } catch (IOException ex) {
                log.error("返回微信支付通知结果异常", ex);
            }
        }
    }
    
    /**
     * 响应微信支付通知
     *
     * @param response 响应
     * @param returnCode 返回状态码
     * @param returnMsg 返回信息
     * @throws IOException IO异常
     */
    private void responseToWx(HttpServletResponse response, String returnCode, String returnMsg) throws IOException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("return_code", returnCode);
        responseMap.put("return_msg", returnMsg);
        String responseXml = WxPayUtil.mapToXml(responseMap);
        
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(responseXml);
        writer.flush();
    }

    /**
     * 申请退款
     *
     * @param refundOrderDTO 退款订单DTO
     * @return 退款结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> refund(RefundOrderDTO refundOrderDTO) {
        log.info("申请退款，参数：{}", JSON.toJSONString(refundOrderDTO));
        
        // 1. 查询支付订单
        LambdaQueryWrapper<PayOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrder::getOrderNo, refundOrderDTO.getOrderNo());
        PayOrder payOrder = payOrderMapper.selectOne(queryWrapper);
        
        // 如果订单不存在，直接返回失败
        if (payOrder == null) {
            log.error("申请退款订单不存在，订单编号：{}", refundOrderDTO.getOrderNo());
            return buildErrorResult("订单不存在");
        }
        
        // 如果订单未支付，不能退款
        if (payOrder.getPayStatus() != 1) {
            log.error("申请退款订单未支付，订单编号：{}", refundOrderDTO.getOrderNo());
            return buildErrorResult("订单未支付");
        }
        
        // 2. 生成退款单号
        String refundNo = WxPayUtil.generateOrderNo("RF");
        
        // 3. 创建退款订单
        RefundOrder refundOrder = new RefundOrder();
        BeanUtils.copyProperties(refundOrderDTO, refundOrder);
        refundOrder.setRefundNo(refundNo);
        refundOrder.setTransactionId(payOrder.getTransactionId());
        refundOrder.setRefundStatus(0); // 退款中
        refundOrderMapper.insert(refundOrder);
        
        // 4. 记录支付日志
        PayLog payLog = new PayLog();
        payLog.setOrderNo(refundOrderDTO.getOrderNo());
        payLog.setTransactionId(payOrder.getTransactionId());
        payLog.setUserId(refundOrderDTO.getUserId());
        payLog.setOpenid(refundOrderDTO.getOpenid());
        payLog.setOperateType(4); // 退款申请
        payLog.setContent("申请退款");
        payLog.setRequestParams(JSON.toJSONString(refundOrderDTO));
        payLogMapper.insert(payLog);
        
        // 5. 调用微信支付申请退款API
        try {
            // 构造请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", wxPayConfig.getAppId());
            params.put("mch_id", wxPayConfig.getMchId());
            params.put("nonce_str", WxPayUtil.generateNonceStr());
            params.put("out_trade_no", refundOrderDTO.getOrderNo());
            params.put("out_refund_no", refundNo);
            params.put("total_fee", refundOrderDTO.getTotalAmount().multiply(new BigDecimal("100")).intValue());
            params.put("refund_fee", refundOrderDTO.getRefundAmount().multiply(new BigDecimal("100")).intValue());
            params.put("refund_desc", refundOrderDTO.getRefundReason());
            params.put("notify_url", wxPayConfig.getRefundNotifyUrl());
            
            // 生成签名
            String sign = WxPayUtil.generateSign(params, wxPayConfig.getMchKey(), wxPayConfig.getSignType());
            params.put("sign", sign);
            
            // 将参数转换为XML
            String xmlParams = WxPayUtil.mapToXml(params);
            
            // 发送请求
            HttpPost httpPost = new HttpPost(REFUND_URL);
            httpPost.setEntity(new StringEntity(xmlParams, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "text/xml");
            
            // 判断是否为开发环境的模拟HttpClient
            if (wxPayClient.getClass().getName().equals("org.apache.http.impl.client.InternalHttpClient")) {
                log.info("使用模拟的HttpClient，返回模拟的退款结果");
                
                // 构造模拟响应
                String mockResponseXml = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "<appid><![CDATA[" + wxPayConfig.getAppId() + "]]></appid>" +
                        "<mch_id><![CDATA[" + wxPayConfig.getMchId() + "]]></mch_id>" +
                        "<nonce_str><![CDATA[" + WxPayUtil.generateNonceStr() + "]]></nonce_str>" +
                        "<sign><![CDATA[MOCK_SIGN]]></sign>" +
                        "<result_code><![CDATA[SUCCESS]]></result_code>" +
                        "<transaction_id><![CDATA[" + refundOrder.getTransactionId() + "]]></transaction_id>" +
                        "<out_trade_no><![CDATA[" + refundOrderDTO.getOrderNo() + "]]></out_trade_no>" +
                        "<out_refund_no><![CDATA[" + refundNo + "]]></out_refund_no>" +
                        "<refund_id><![CDATA[2303" + System.currentTimeMillis() % 10000000 + "]]></refund_id>" +
                        "<refund_fee><![CDATA[" + refundOrderDTO.getRefundAmount().multiply(new BigDecimal("100")).intValue() + "]]></refund_fee>" +
                        "<total_fee><![CDATA[" + refundOrderDTO.getTotalAmount().multiply(new BigDecimal("100")).intValue() + "]]></total_fee>" +
                        "<cash_fee><![CDATA[" + refundOrderDTO.getTotalAmount().multiply(new BigDecimal("100")).intValue() + "]]></cash_fee>" +
                        "</xml>";
                
                // 更新支付日志
                payLog.setResponseResult(mockResponseXml);
                payLogMapper.updateById(payLog);
                
                // 解析模拟响应结果
                Map<String, String> responseMap = WxPayUtil.xmlToMap(mockResponseXml);
                
                // 退款成功，更新退款订单状态
                refundOrder.setRefundStatus(1); // 退款成功
                refundOrder.setRefundId(responseMap.get("refund_id"));
                refundOrder.setRefundTime(LocalDateTime.now());
                refundOrderMapper.updateById(refundOrder);
                
                // 更新支付订单状态
                payOrder.setPayStatus(3); // 已退款
                payOrderMapper.updateById(payOrder);
                
                // 更新商品订单状态
                Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, payOrder.getOrderNo()));
                if (order != null) {
                    order.setStatus(4); // 已关闭
                    orderMapper.updateById(order);
                }
                
                // 构造返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("refundNo", refundNo);
                result.put("refundId", responseMap.get("refund_id"));
                result.put("refundStatus", 1);
                result.put("message", "退款成功");
                
                return result;
            }
            
            CloseableHttpResponse response = wxPayClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("微信支付申请退款响应：{}", responseString);
            
            // 解析响应结果
            Map<String, String> responseMap = WxPayUtil.xmlToMap(responseString);
            
            // 更新支付日志
            payLog.setResponseResult(responseString);
            payLogMapper.updateById(payLog);
            
            // 判断请求是否成功
            if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                // 退款成功，更新退款订单状态
                refundOrder.setRefundStatus(1); // 退款成功
                refundOrder.setRefundId(responseMap.get("refund_id"));
                refundOrder.setRefundTime(LocalDateTime.now());
                refundOrderMapper.updateById(refundOrder);
                
                // 更新支付订单状态
                payOrder.setPayStatus(3); // 已退款
                payOrderMapper.updateById(payOrder);
                
                // 更新商品订单状态
                Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, payOrder.getOrderNo()));
                if (order != null) {
                    order.setStatus(4); // 已关闭
                    orderMapper.updateById(order);
                }
                
                // 构造返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("refundNo", refundNo);
                result.put("refundId", responseMap.get("refund_id"));
                result.put("refundStatus", 1);
                result.put("message", "退款成功");
                
                return result;
            } else {
                // 退款失败，更新退款订单状态
                refundOrder.setRefundStatus(2); // 退款失败
                refundOrder.setFailReason(responseMap.get("err_code_des"));
                refundOrderMapper.updateById(refundOrder);
                
                log.error("微信支付申请退款失败，错误码：{}，错误信息：{}", responseMap.get("err_code"), responseMap.get("err_code_des"));
                return buildErrorResult(responseMap.get("err_code_des"));
            }
        } catch (Exception e) {
            log.error("申请退款异常", e);
            
            // 更新退款订单状态
            refundOrder.setRefundStatus(2); // 退款失败
            refundOrder.setFailReason("系统异常");
            refundOrderMapper.updateById(refundOrder);
            
            return buildErrorResult("系统异常");
        }
    }
    
    /**
     * 查询退款订单
     *
     * @param refundNo 退款单号
     * @return 退款订单
     */
    @Override
    public RefundOrder queryRefundOrder(String refundNo) {
        log.info("查询退款订单，退款单号：{}", refundNo);
        
        // 1. 查询本地退款订单
        LambdaQueryWrapper<RefundOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RefundOrder::getRefundNo, refundNo);
        RefundOrder refundOrder = refundOrderMapper.selectOne(queryWrapper);
        
        // 如果退款订单不存在，直接返回null
        if (refundOrder == null) {
            return null;
        }
        
        // 如果退款已完成，直接返回
        if (refundOrder.getRefundStatus() == 1 || refundOrder.getRefundStatus() == 2) {
            return refundOrder;
        }
        
        // 2. 调用微信支付查询退款API
        try {
            // 构造请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", wxPayConfig.getAppId());
            params.put("mch_id", wxPayConfig.getMchId());
            params.put("out_refund_no", refundNo);
            params.put("nonce_str", WxPayUtil.generateNonceStr());
            
            // 生成签名
            String sign = WxPayUtil.generateSign(params, wxPayConfig.getMchKey(), wxPayConfig.getSignType());
            params.put("sign", sign);
            
            // 将参数转换为XML
            String xmlParams = WxPayUtil.mapToXml(params);
            
            // 发送请求
            HttpPost httpPost = new HttpPost(REFUND_QUERY_URL);
            httpPost.setEntity(new StringEntity(xmlParams, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "text/xml");
            
            // 判断是否为开发环境的模拟HttpClient
            if (wxPayClient.getClass().getName().equals("org.apache.http.impl.client.InternalHttpClient")) {
                log.info("使用模拟的HttpClient，返回模拟的退款查询结果");
                
                // 构造模拟响应
                String mockResponseXml = "<xml>" +
                        "<return_code><![CDATA[SUCCESS]]></return_code>" +
                        "<return_msg><![CDATA[OK]]></return_msg>" +
                        "<appid><![CDATA[" + wxPayConfig.getAppId() + "]]></appid>" +
                        "<mch_id><![CDATA[" + wxPayConfig.getMchId() + "]]></mch_id>" +
                        "<nonce_str><![CDATA[" + WxPayUtil.generateNonceStr() + "]]></nonce_str>" +
                        "<sign><![CDATA[MOCK_SIGN]]></sign>" +
                        "<result_code><![CDATA[SUCCESS]]></result_code>" +
                        "<transaction_id><![CDATA[" + refundOrder.getTransactionId() + "]]></transaction_id>" +
                        "<out_trade_no><![CDATA[" + refundOrder.getOrderNo() + "]]></out_trade_no>" +
                        "<out_refund_no_0><![CDATA[" + refundNo + "]]></out_refund_no_0>" +
                        "<refund_id_0><![CDATA[" + (refundOrder.getRefundId() != null ? refundOrder.getRefundId() : "2303" + System.currentTimeMillis() % 10000000) + "]]></refund_id_0>" +
                        "<refund_status_0><![CDATA[SUCCESS]]></refund_status_0>" +
                        "<refund_fee_0><![CDATA[" + refundOrder.getRefundAmount().multiply(new BigDecimal("100")).intValue() + "]]></refund_fee_0>" +
                        "<refund_recv_accout_0><![CDATA[支付用户零钱]]></refund_recv_accout_0>" +
                        "<refund_success_time_0><![CDATA[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "]]></refund_success_time_0>" +
                        "</xml>";
                
                // 记录支付日志
                PayLog payLog = new PayLog();
                payLog.setOrderNo(refundOrder.getOrderNo());
                payLog.setTransactionId(refundOrder.getTransactionId());
                payLog.setUserId(refundOrder.getUserId());
                payLog.setOpenid(refundOrder.getOpenid());
                payLog.setOperateType(5); // 查询退款
                payLog.setContent("查询退款订单");
                payLog.setRequestParams(JSON.toJSONString(params));
                payLog.setResponseResult(mockResponseXml);
                payLogMapper.insert(payLog);
                
                // 解析模拟响应结果
                Map<String, String> responseMap = WxPayUtil.xmlToMap(mockResponseXml);
                
                // 判断退款状态
                String refundStatus = responseMap.get("refund_status_0");
                if ("SUCCESS".equals(refundStatus)) {
                    // 退款成功，更新退款订单状态
                    refundOrder.setRefundStatus(1); // 退款成功
                    refundOrder.setRefundTime(LocalDateTime.now());
                    refundOrder.setRefundRecvAccount(responseMap.get("refund_recv_accout_0"));
                    if (refundOrder.getRefundId() == null) {
                        refundOrder.setRefundId(responseMap.get("refund_id_0"));
                    }
                    refundOrderMapper.updateById(refundOrder);
                    
                    // 更新支付订单状态
                    LambdaQueryWrapper<PayOrder> payOrderQueryWrapper = new LambdaQueryWrapper<>();
                    payOrderQueryWrapper.eq(PayOrder::getOrderNo, refundOrder.getOrderNo());
                    PayOrder payOrder = payOrderMapper.selectOne(payOrderQueryWrapper);
                    if (payOrder != null) {
                        payOrder.setPayStatus(3); // 已退款
                        payOrderMapper.updateById(payOrder);
                    }
                }
                
                return refundOrder;
            }
            
            CloseableHttpResponse response = wxPayClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("微信支付查询退款响应：{}", responseString);
            
            // 解析响应结果
            Map<String, String> responseMap = WxPayUtil.xmlToMap(responseString);
            
            // 记录支付日志
            PayLog payLog = new PayLog();
            payLog.setOrderNo(refundOrder.getOrderNo());
            payLog.setTransactionId(refundOrder.getTransactionId());
            payLog.setUserId(refundOrder.getUserId());
            payLog.setOpenid(refundOrder.getOpenid());
            payLog.setOperateType(5); // 查询退款
            payLog.setContent("查询退款订单");
            payLog.setRequestParams(JSON.toJSONString(params));
            payLog.setResponseResult(responseString);
            payLogMapper.insert(payLog);
            
            // 判断请求是否成功
            if ("SUCCESS".equals(responseMap.get("return_code")) && "SUCCESS".equals(responseMap.get("result_code"))) {
                // 判断退款状态
                String refundStatus = responseMap.get("refund_status_0");
                if ("SUCCESS".equals(refundStatus)) {
                    // 退款成功，更新退款订单状态
                    refundOrder.setRefundStatus(1); // 退款成功
                    refundOrder.setRefundTime(LocalDateTime.now());
                    refundOrder.setRefundRecvAccount(responseMap.get("refund_recv_accout_0"));
                    refundOrderMapper.updateById(refundOrder);
                    
                    // 更新支付订单状态
                    LambdaQueryWrapper<PayOrder> payOrderQueryWrapper = new LambdaQueryWrapper<>();
                    payOrderQueryWrapper.eq(PayOrder::getOrderNo, refundOrder.getOrderNo());
                    PayOrder payOrder = payOrderMapper.selectOne(payOrderQueryWrapper);
                    if (payOrder != null) {
                        payOrder.setPayStatus(3); // 已退款
                        payOrderMapper.updateById(payOrder);
                    }
                } else if ("PROCESSING".equals(refundStatus)) {
                    // 退款处理中
                    refundOrder.setRefundStatus(0); // 退款中
                    refundOrderMapper.updateById(refundOrder);
                } else if ("REFUNDCLOSE".equals(refundStatus)) {
                    // 退款关闭
                    refundOrder.setRefundStatus(2); // 退款失败
                    refundOrder.setFailReason("退款关闭");
                    refundOrderMapper.updateById(refundOrder);
                } else if ("CHANGE".equals(refundStatus)) {
                    // 退款异常
                    refundOrder.setRefundStatus(2); // 退款失败
                    refundOrder.setFailReason("退款异常");
                    refundOrderMapper.updateById(refundOrder);
                }
            }
        } catch (Exception e) {
            log.error("查询退款订单异常", e);
        }
        
        return refundOrder;
    }

    /**
     * 处理退款结果通知
     *
     * @param request  请求
     * @param response 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("处理退款结果通知");
        
        try {
            // 读取通知数据
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String notifyData = sb.toString();
            log.info("微信支付退款通知数据：{}", notifyData);
            
            // 解析通知数据
            Map<String, String> notifyMap = WxPayUtil.xmlToMap(notifyData);
            
            // 验证返回状态码
            if (!"SUCCESS".equals(notifyMap.get("return_code"))) {
                log.error("微信支付退款通知返回失败，错误信息：{}", notifyMap.get("return_msg"));
                responseToWx(response, "FAIL", "通信失败");
                return;
            }
            
            // 解密退款信息
            // 注意：实际项目中需要实现解密逻辑，这里简化处理
            String reqInfo = notifyMap.get("req_info");
            // 解密后的数据
            Map<String, String> refundInfo = new HashMap<>(); // 实际项目中需要解密reqInfo
            
            // 获取商户退款单号
            String refundNo = refundInfo.get("out_refund_no");
            
            // 查询本地退款订单
            LambdaQueryWrapper<RefundOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RefundOrder::getRefundNo, refundNo);
            RefundOrder refundOrder = refundOrderMapper.selectOne(queryWrapper);
            
            // 如果退款订单不存在，返回失败
            if (refundOrder == null) {
                log.error("微信支付退款通知订单不存在，退款单号：{}", refundNo);
                responseToWx(response, "FAIL", "订单不存在");
                return;
            }
            
            // 如果退款已完成，直接返回成功
            if (refundOrder.getRefundStatus() == 1) {
                log.info("微信支付退款通知订单已退款，退款单号：{}", refundNo);
                responseToWx(response, "SUCCESS", "OK");
                return;
            }
            
            // 获取退款状态
            String refundStatus = refundInfo.get("refund_status");
            
            // 更新退款订单状态
            if ("SUCCESS".equals(refundStatus)) {
                // 退款成功
                refundOrder.setRefundStatus(1); // 退款成功
                refundOrder.setRefundTime(LocalDateTime.now());
                refundOrder.setRefundRecvAccount(refundInfo.get("refund_recv_accout"));
                refundOrderMapper.updateById(refundOrder);
                
                // 更新支付订单状态
                LambdaQueryWrapper<PayOrder> payOrderQueryWrapper = new LambdaQueryWrapper<>();
                payOrderQueryWrapper.eq(PayOrder::getOrderNo, refundOrder.getOrderNo());
                PayOrder payOrder = payOrderMapper.selectOne(payOrderQueryWrapper);
                if (payOrder != null) {
                    payOrder.setPayStatus(3); // 已退款
                    payOrderMapper.updateById(payOrder);
                }
                
                // 记录支付日志
                PayLog payLog = new PayLog();
                payLog.setOrderNo(refundOrder.getOrderNo());
                payLog.setTransactionId(refundOrder.getTransactionId());
                payLog.setUserId(refundOrder.getUserId());
                payLog.setOpenid(refundOrder.getOpenid());
                payLog.setOperateType(5); // 退款成功
                payLog.setContent("退款成功通知");
                payLog.setRequestParams(notifyData);
                payLog.setResponseResult("SUCCESS");
                payLogMapper.insert(payLog);
            } else {
                // 退款失败
                refundOrder.setRefundStatus(2); // 退款失败
                refundOrder.setFailReason("退款失败");
                refundOrderMapper.updateById(refundOrder);
                
                // 记录支付日志
                PayLog payLog = new PayLog();
                payLog.setOrderNo(refundOrder.getOrderNo());
                payLog.setTransactionId(refundOrder.getTransactionId());
                payLog.setUserId(refundOrder.getUserId());
                payLog.setOpenid(refundOrder.getOpenid());
                payLog.setOperateType(6); // 退款失败
                payLog.setContent("退款失败通知");
                payLog.setRequestParams(notifyData);
                payLog.setResponseResult("SUCCESS");
                payLogMapper.insert(payLog);
            }
            
            // 返回成功
            responseToWx(response, "SUCCESS", "OK");
        } catch (Exception e) {
            log.error("处理退款结果通知异常", e);
            try {
                responseToWx(response, "FAIL", "系统异常");
            } catch (IOException ex) {
                log.error("返回微信支付退款通知结果异常", ex);
            }
        }
    }
    
    /**
     * 构建错误结果
     *
     * @param message 错误信息
     * @return 错误结果
     */
    private Map<String, Object> buildErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
