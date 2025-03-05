package com.wxmall.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wxmall.common.api.CommonResult;
import com.wxmall.dto.WxLoginDTO;
import com.wxmall.service.WxService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信登录测试控制器
 * 仅用于开发环境测试微信登录功能
 */
@Slf4j
@RestController
@RequestMapping("/api/test/wx")
@Api(tags = "微信登录测试接口")
public class WxTestController {

    @Autowired
    private WxService wxService;

    /**
     * 模拟微信登录
     */
    @PostMapping("/mock-login")
    @ApiOperation("模拟微信登录")
    public CommonResult<Map<String, Object>> mockWxLogin(@RequestBody WxLoginDTO wxLoginDTO) {
        // 使用固定的测试code
        if (wxLoginDTO.getCode() == null || wxLoginDTO.getCode().isEmpty()) {
            wxLoginDTO.setCode("test_code_123");
        }

        // 调用实际的微信登录服务
        Map<String, Object> result = wxService.wxLogin(wxLoginDTO);
        return CommonResult.success(result);
    }

    /**
     * 解密测试
     */
    @PostMapping("/decrypt")
    @ApiOperation("测试解密")
    public CommonResult<String> testDecrypt(@RequestParam String encryptedData,
                                            @RequestParam String sessionKey,
                                            @RequestParam String iv) {
        String decrypted = wxService.decryptUserInfo(encryptedData, sessionKey, iv);
        return CommonResult.success(decrypted);
    }
}