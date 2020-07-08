package cn.schoolwow.quickdao.dao.sql.dml;

/**
 * 雪花算法ID生成器
 * 本生成器无数据中心和机器id识别,毫秒内最大生成id数2^22 = 4194304
 */
public class SnowflakeIdGenerator implements IDGenerator {
    //时间起始标记点 (2020-01-01)
    private final static long twepoch = 1577808000000l;
    //毫秒内自增位
    private final static long sequenceBits = 22L;
    //时间毫秒数需要左移22位
    private final static long timestampLeftShift = sequenceBits;
    //序列号掩码 2^22 = 4194304
    private final static long sequenceMask = -1L ^ (-1L << sequenceBits);
    //上次生产id的时间戳
    private static long lastTimestamp = -1L;
    //毫秒内序列号
    private long sequence = 0L;

    @Override
    public synchronized long getNextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨!当前时间戳:" + timestamp + ",上次id时间戳:" + lastTimestamp);
        }
        if (lastTimestamp == timestamp) {
            //同一毫秒内
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //当前毫秒内计数满了,则等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //重置毫秒内序列号
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        // ID偏移组合生成最终的ID
        long nextId = ((timestamp - twepoch) << timestampLeftShift) | sequence;
        return nextId;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
