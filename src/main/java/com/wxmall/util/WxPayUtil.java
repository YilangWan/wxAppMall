package com.wxmall.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.digest.HMac;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 微信支付工具类
 * 
 * @author System Architect
 */
@Slf4j
public class WxPayUtil {
    
    /**
     * 生成随机字符串
     *
     * @return 随机字符串
     */
    public static String generateNonceStr() {
        return RandomUtil.randomString(32);
    }
    
    /**
     * 生成订单号
     *
     * @param prefix 前缀
     * @return 订单号
     */
    public static String generateOrderNo(String prefix) {
        // 生成格式：前缀 + 年月日时分秒 + 6位随机数
        return prefix + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
    }
    
    /**
     * 生成签名
     *
     * @param params  参数
     * @param apiKey  API密钥
     * @param signType 签名类型
     * @return 签名
     */
    public static String generateSign(Map<String, Object> params, String apiKey, String signType) {
        // 1. 参数名ASCII码从小到大排序
        TreeMap<String, Object> sortedParams = new TreeMap<>(params);
        
        // 2. 拼接字符串
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !"".equals(entry.getValue())) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key=").append(apiKey);
        
        // 3. 根据签名类型计算签名
        String sign;
        if ("HMAC-SHA256".equals(signType)) {
            // 使用HMac类替代HmacUtil
            HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, apiKey.getBytes(StandardCharsets.UTF_8));
            sign = hmac.digestHex(sb.toString());
        } else {
            sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();
        }
        
        return sign;
    }
    
    /**
     * 生成V3签名
     *
     * @param method    请求方法
     * @param url       请求URL
     * @param timestamp 时间戳
     * @param nonceStr  随机字符串
     * @param body      请求体
     * @param privateKey 私钥
     * @return 签名
     */
    public static String generateSignatureV3(String method, String url, long timestamp, String nonceStr, String body, String privateKey) {
        // 构造签名字符串
        String signatureStr = method + "\n"
                + url + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
        
        try {
            // 使用HMac类替代HmacUtil
            HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, privateKey.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = hmac.digest(signatureStr.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(signBytes);
        } catch (Exception e) {
            log.error("生成V3签名失败", e);
            throw new RuntimeException("生成V3签名失败", e);
        }
    }
    
    /**
     * 验证签名
     *
     * @param params   参数
     * @param apiKey   API密钥
     * @param signType 签名类型
     * @param sign     签名
     * @return 是否验证通过
     */
    public static boolean verifySign(Map<String, Object> params, String apiKey, String signType, String sign) {
        String generatedSign = generateSign(params, apiKey, signType);
        return generatedSign.equals(sign);
    }
    
    /**
     * 将Map转换为XML
     *
     * @param params 参数
     * @return XML字符串
     */
    public static String mapToXml(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append("<").append(entry.getKey()).append(">");
            sb.append(entry.getValue());
            sb.append("</").append(entry.getKey()).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }
    
    /**
     * 将XML转换为Map
     *
     * @param xml XML字符串
     * @return Map
     */
    public static Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        // 简化实现，实际项目中可以使用DOM解析
        String[] elements = xml.replaceAll("</?xml>", "").split("</");
        for (String element : elements) {
            if (element.contains(">")) {
                String[] keyValue = element.split(">");
                if (keyValue.length >= 2) {
                    String key = keyValue[0].replaceAll("<", "");
                    String value = keyValue[1];
                    map.put(key, value);
                }
            }
        }
        return map;
    }
    
    /**
     * 将JSON对象转换为Map
     *
     * @param json JSON对象
     * @return Map
     */
    public static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
    
    /**
     * 将Map转换为JSON字符串
     *
     * @param map Map
     * @return JSON字符串
     */
    public static String mapToJson(Map<String, Object> map) {
        return JSON.toJSONString(map);
    }
    
    /**
     * 生成小程序支付参数
     *
     * @param appId    小程序appId
     * @param prepayId 预支付交易会话标识
     * @param apiKey   API密钥
     * @return 支付参数
     */
    public static Map<String, String> generatePayParams(String appId, String prepayId, String apiKey) {
        Map<String, String> payParams = new HashMap<>();
        payParams.put("appId", appId);
        payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        payParams.put("nonceStr", generateNonceStr());
        payParams.put("package", "prepay_id=" + prepayId);
        payParams.put("signType", "MD5");
        
        // 构造签名参数
        Map<String, Object> signParams = new HashMap<>();
        signParams.put("appId", payParams.get("appId"));
        signParams.put("timeStamp", payParams.get("timeStamp"));
        signParams.put("nonceStr", payParams.get("nonceStr"));
        signParams.put("package", payParams.get("package"));
        signParams.put("signType", payParams.get("signType"));
        
        // 生成签名
        String sign = generateSign(signParams, apiKey, "MD5");
        payParams.put("paySign", sign);
        
        return payParams;
    }
    
    /**
     * 生成V3小程序支付参数
     *
     * @param appId    小程序appId
     * @param prepayId 预支付交易会话标识
     * @param privateKey 私钥
     * @return 支付参数
     */
    public static Map<String, String> generatePayParamsV3(String appId, String prepayId, String privateKey) {
        Map<String, String> payParams = new HashMap<>();
        payParams.put("appId", appId);
        payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        payParams.put("nonceStr", generateNonceStr());
        payParams.put("package", "prepay_id=" + prepayId);
        payParams.put("signType", "RSA");
        
        // 构造签名字符串
        String signatureStr = payParams.get("appId") + "\n"
                + payParams.get("timeStamp") + "\n"
                + payParams.get("nonceStr") + "\n"
                + payParams.get("package") + "\n";
        
        try {
            // 使用HMac类替代HmacUtil
            HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, privateKey.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = hmac.digest(signatureStr.getBytes(StandardCharsets.UTF_8));
            payParams.put("paySign", Base64.encodeBase64String(signBytes));
        } catch (Exception e) {
            log.error("生成V3支付参数签名失败", e);
            throw new RuntimeException("生成V3支付参数签名失败", e);
        }
        
        return payParams;
    }
}
