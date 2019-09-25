package cn.learn.distributed.lock.adapter.redis.spring.config;

import cn.learn.distributed.lock.adapter.redis.spring.RedisDistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * lock 构造器
 *
 * @author shaoyijiong
 * @date 2019/9/25
 */
public class RedisLockBuilder {


  private int retryAwait;
  private int lockTimeout;
  private final StringRedisTemplate stringRedisTemplate;

  public RedisLockBuilder(StringRedisTemplate stringRedisTemplate, RedisLockProperties redisLockProperties) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.retryAwait = redisLockProperties.getRetryAwait();
    this.lockTimeout = redisLockProperties.getLockTimeout();
  }

  public RedisDistributedLock build(String lockId) {
    return new RedisDistributedLock(lockId, stringRedisTemplate, retryAwait, lockTimeout);
  }
}
