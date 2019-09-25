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

  private StringRedisTemplate redisTemplate;

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
    final long deadline = System.nanoTime() + unit.toNanos(time);
    String lockValue;
    for (; ; ) {
      lockValue = createKey(lockId);
      if (lockValue != null) {
        break;
      }
      time = deadline - System.nanoTime();
      if (time <= 0L) {
        break;
      }
      LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(retryAwait));
    }
    return lockValue;
  }

  void unlockRedisLock(String key, String value) {
    String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then "
        + "return redis.call('del', KEYS[1]) else return 0 end";
    RedisScript script = RedisScript.of(luaScript, Long.class);
    Long result = redisTemplate.<Long>execute(script, Collections.singletonList(key), value);
  }

  private String createKey(String lockId) {
    String value = lockId + ":" + UUID.randomUUID();
    Boolean success = redisTemplate.opsForValue().setIfAbsent(lockId, value, lockTimeout, TimeUnit.MILLISECONDS);
    if (success != null && success) {
      return value;
    }
    return null;
  }
}
