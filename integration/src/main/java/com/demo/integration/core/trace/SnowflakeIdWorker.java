package com.demo.integration.core.trace;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:05
 */
public class SnowflakeIdWorker {

    private final long twepoch = 1288834974657L;

    private final long workerIdBits = 5L;

    private final long datacenterIdBits = 5L;

    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;

    private final long datacenterIdShift =
            sequenceBits + workerIdBits;

    private final long timestampLeftShift =
            sequenceBits + workerIdBits + datacenterIdBits;

    private final long sequenceMask =
            -1L ^ (-1L << sequenceBits);

    private long workerId;

    private long datacenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    public SnowflakeIdWorker(
            long workerId,
            long datacenterId) {

        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {

        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨异常");
        }

        if (lastTimestamp == timestamp) {

            sequence = (sequence + 1) & sequenceMask;

            if (sequence == 0) {

                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
            }

        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch)
                << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }
}