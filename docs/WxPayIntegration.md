# 微信支付集成文档

## 1. 功能概述

本项目实现了微信小程序支付功能，包括以下核心功能：

- 创建支付订单
- 查询支付订单
- 关闭支付订单
- 处理支付结果通知
- 申请退款
- 查询退款订单
- 处理退款结果通知

## 2. 技术架构

- 后端框架：Spring Boot 2.7.14
- 数据库访问：MyBatis Plus 3.5.3.1
- 工具库：Hutool、Fastjson
- 微信支付API：V2版本

## 3. 核心类说明

### 3.1 配置类

- **WxPayConfig**：微信支付配置类，负责加载微信支付相关配置和创建HttpClient

### 3.2 服务类

- **PayService**：支付服务接口
- **PayServiceImpl**：支付服务实现类，包含所有支付相关的业务逻辑

### 3.3 工具类

- **WxPayUtil**：微信支付工具类，提供签名生成、XML转换等功能

### 3.4 数据模型

- **PayOrder**：支付订单模型
- **RefundOrder**：退款订单模型
- **PayLog**：支付日志模型

### 3.5 数据传输对象

- **PayOrderDTO**：支付订单DTO
- **RefundOrderDTO**：退款订单DTO

### 3.6 视图对象

- **WxPayVO**：微信支付视图对象，返回给前端的支付参数

## 4. 配置说明

在`application.yml`中配置微信支付相关参数：

```yaml
wx:
  pay:
    appId: 你的小程序appId
    mchId: 你的商户号
    mchKey: 你的商户密钥
    notifyUrl: 支付结果通知地址
    refundNotifyUrl: 退款结果通知地址
    signType: MD5或HMAC-SHA256
    apiV3Key: API V3密钥
    privateKeyPath: 商户私钥路径
    certSerialNo: 证书序列号
```

## 5. 开发环境配置

为了方便开发和测试，系统支持在开发环境中模拟微信支付API的响应。当检测到以下情况时，系统会自动使用模拟的HttpClient：

1. 商户私钥文件不存在
2. 证书序列号未配置
3. API V3密钥未配置

模拟的HttpClient会返回成功的响应，模拟支付成功、查询成功、关闭订单成功、退款成功等场景，使开发人员能够在没有实际微信支付证书的情况下进行开发和测试。

## 6. 生产环境配置

在生产环境中，需要配置实际的微信支付参数：

1. 申请微信支付商户号
2. 下载API证书
3. 配置商户私钥
4. 配置证书序列号
5. 配置API V3密钥
6. 配置通知URL

## 7. 使用示例

### 7.1 创建支付订单

```java
PayOrderDTO payOrderDTO = new PayOrderDTO();
payOrderDTO.setOrderNo("订单编号");
payOrderDTO.setOpenid("用户openid");
payOrderDTO.setUserId(用户ID);
payOrderDTO.setTotalAmount(new BigDecimal("订单金额"));
payOrderDTO.setBody("商品描述");
payOrderDTO.setAttach("附加数据");
payOrderDTO.setClientIp("客户端IP");

WxPayVO wxPayVO = payService.createPayOrder(payOrderDTO);
```

### 7.2 查询支付订单

```java
PayOrder payOrder = payService.queryPayOrder("订单编号");
```

### 7.3 关闭支付订单

```java
boolean result = payService.closePayOrder("订单编号");
```

### 7.4 申请退款

```java
RefundOrderDTO refundOrderDTO = new RefundOrderDTO();
refundOrderDTO.setOrderNo("订单编号");
refundOrderDTO.setOpenid("用户openid");
refundOrderDTO.setUserId(用户ID);
refundOrderDTO.setTotalAmount(new BigDecimal("订单金额"));
refundOrderDTO.setRefundAmount(new BigDecimal("退款金额"));
refundOrderDTO.setRefundReason("退款原因");

Map<String, Object> result = payService.refund(refundOrderDTO);
```

### 7.5 查询退款订单

```java
RefundOrder refundOrder = payService.queryRefundOrder("退款单号");
```

## 8. 注意事项

1. 在生产环境中，必须配置真实的微信支付参数
2. 商户私钥文件应放在安全的位置，并确保应用有读取权限
3. 通知URL必须是外网可访问的地址
4. 签名类型推荐使用HMAC-SHA256，更安全
5. 退款功能需要使用微信支付证书，确保证书配置正确

## 9. 常见问题

### 9.1 开发环境无法获取证书

在开发环境中，系统会自动使用模拟的HttpClient，无需实际的证书。

### 9.2 支付通知没有收到

检查通知URL是否配置正确，确保外网可访问。

### 9.3 签名验证失败

检查商户密钥是否配置正确，签名类型是否一致。

## 10. 更新日志

### v1.0.0 (2025-03-05)
- 实现基本的支付功能
- 添加退款功能
- 支持开发环境模拟

## 11. 联系方式

如有问题，请联系系统管理员。
