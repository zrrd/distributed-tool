package cn.learn.distributed.lock.adapter.redis.spring;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author shaoyijiong
 * @date 2019/9/25
 */
@Getter
public class RedisOperation {


  /**
   * 重试等待时间 500毫秒 0.5秒
   */
  private int retryAwait = 500;

  /**
   * redis 锁的最长时间 60_000毫秒 60秒
   */
  private int lockTimeout = 60_000;

  private final StringRedisTemplate redisTemplate;

  /**
   * 简单构造
   */
  RedisOperation(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * 带时间的构造
   *
   * @param redisTemplate redis 客户端
   * @param retryAwait 重试时间
   * @param lockTimeout redis超时时间 根据业务执行时间长短
   */
  RedisOperation(StringRedisTemplate redisTemplate, int retryAwait, int lockTimeout) {
    Preconditions
        .checkArgument(retryAwait > 0 && lockTimeout > 0, "retryAwait or lockTimeout must be greater than 0");
    Preconditions.checkArgument(lockTimeout > retryAwait, "lockTimeout must be greater then retryAwait");
    this.redisTemplate = redisTemplate;
    this.retryAwait = retryAwait;
    this.lockTimeout = lockTimeout;
  }

  String tryRedisLock(String lockId, long time, TimeUnit unit) {
    if (time < 0) {
      return null;
    }
    // 自旋结束时间
    final long deadline = System.nanoTime() + unit.toNanos(time);
    String lockValue;
    // 整个过程就是 1. 尝试获取锁 2. 判断是否获取锁成功或到时间跳出循环 3. 休眠一定时间结束再进入循环
    for (; ; ) {
      // 尝试获取从redis中获取锁
      lockValue = createKey(lockId);
      // 自旋 直到 1. 获取锁成功 2. 当前时间大于自旋结束时间
      if (lockValue != null) {
        break;
      }
      time = deadline - System.nanoTime();
      if (time <= 0L) {
        break;
      }
      // 当前线程休眠retryAwait时间 , 期间会让出 cpu
      LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(retryAwait));
    }
    return lockValue;
  }

  void unlockRedisLock(String key, String value) {
    // 执行 Lua 脚本释放锁
    String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then "
        + "return redis.call('del', KEYS[1]) else return 0 end";
    RedisScript script = RedisScript.of(luaScript, Long.class);
    Long result = redisTemplate.<Long>execute(script, Collections.singletonList(key), value);
  }

  private String createKey(String lockId) {
    // 随机值
    String value = lockId + ":" + UUID.randomUUID();
    // SET resource_name random_value NX PX 30000
    Boolean success = redisTemplate.opsForValue().setIfAbsent(lockId, value, lockTimeout, TimeUnit.MILLISECONDS);
    // 成功返回随机值 , 否则返回 null
    if (success != null && success) {
      return value;
    }
    return null;
  }
}
