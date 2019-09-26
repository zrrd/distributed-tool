package cn.learn.distributed.lock.adapter.redis.spring.config;

import cn.learn.distributed.lock.adapter.redis.spring.RedisDistributedLock;
import cn.learn.distributed.lock.core.DistributedLock;
import cn.learn.distributed.lock.core.LockBuilder;
import cn.learn.distributed.lock.core.LockConfiguration;

/**
 * lock 构造器
 *
 * @author shaoyijiong
 * @date 2019/9/25
 */
public class RedisLockBuilder implements LockBuilder {


  private final LockConfiguration lockConfiguration;


  public RedisLockBuilder(LockConfiguration lockConfiguration) {
    this.lockConfiguration = lockConfiguration;
  }


  @Override
  public DistributedLock build(String lockId) {
    return build(lockId, lockConfiguration);
  }

  @Override
  public DistributedLock build(String lockId, LockConfiguration configuration) {
    SpringLockConfiguration springLockConfiguration = (SpringLockConfiguration) configuration;
    return new RedisDistributedLock(lockId, springLockConfiguration.getRedisTemplate(),
        springLockConfiguration.getRetryAwait(), springLockConfiguration.getLockTimeout());
  }
}
