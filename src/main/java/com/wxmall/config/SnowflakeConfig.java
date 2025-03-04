package com.wxmall.config;

import com.wxmall.util.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花算法配置类
 */
@Configuration
public class SnowflakeConfig {

    /**
     * 创建雪花算法ID生成器
     * 这里使用固定的数据中心ID和工作机器ID
     * 在分布式环境中，这些ID应该从配置中心获取或基于其他策略分配
     */
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        // 使用数据中心ID为1，工作机器ID为1
        return new SnowflakeIdGenerator(1, 1);
    }
}