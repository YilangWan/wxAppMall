package com.wxmall.controller;

import com.wxmall.common.api.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/api")
@Api(tags = "首页管理")
public class HomeController {

    @ApiOperation("获取首页信息")
    @GetMapping("/home")
    public CommonResult<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("appName", "微信商城小程序");
        data.put("version", "1.0.0");
        data.put("author", "WxMall Team");
        return CommonResult.success(data);
    }
}
