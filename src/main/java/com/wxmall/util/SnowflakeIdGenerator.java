// /Users/yilangwan/Desktop/PersonalProject/AI/wxMallWindSurf/src/main/java/com/wxmall/util/SnowflakeIdGenerator.java
package com.wxmall.util;

/**
 * 雪花算法ID生成器
 * Snowflake ID生成器，生成64位的Long类型的ID，结构如下：
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 1位符号位 - 41位时间戳 - 5位数据中心ID - 5位工作机器ID - 12位序列号
 */
public class SnowflakeIdGenerator {
    // 开始时间截 (2023-01-01)
    private final long twepoch = 1672531200000L;
    
    // 数据中心ID所占的位数
    private final long datacenterIdBits = 5L;
    
    // 工作机器ID所占的位数
    private final long workerIdBits = 5L;
    
    // 序列号所占的位数
    private final long sequenceBits = 12L;
    
    // 数据中心ID的最大值
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    
    // 工作机器ID的最大值
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    
    // 序列号的最大值
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);
    
    // 工作机器ID向左移的位数
    private final long workerIdShift = sequenceBits;
    
    // 数据中心ID向左移的位数
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    
    // 时间戳向左移的位数
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    
    // 数据中心ID
    private long datacenterId;
    
    // 工作机器ID
    private long workerId;
    
    // 序列号
    private long sequence = 0L;
    
    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;
    
    /**
     * 构造函数
     * @param datacenterId 数据中心ID (0~31)
     * @param workerId 工作机器ID (0~31)
     */
    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("Datacenter ID can't be greater than %d or less than 0", maxDatacenterId));
        }
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker ID can't be greater than %d or less than 0", maxWorkerId));
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }
    
    /**
     * 获取下一个ID
     * @return 下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行序列号自增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重置
            sequence = 0L;
        }
        
        // 记录上次生成ID的时间戳
        lastTimestamp = timestamp;
        
        // 生成并返回ID
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }
    
    /**
     * 等待下一个毫秒的到来
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一个毫秒的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    /**
     * 获取当前时间戳
     * @return 当前时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}