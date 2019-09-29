package cn.learn.distributed.lock.adapter.redis.redisson;

import cn.learn.distributed.lock.core.DistributedLock;
import cn.learn.distributed.lock.core.LockBuilder;
import cn.learn.distributed.lock.core.LockConfiguration;
import org.redisson.api.RLock;

/**
 * @author shaoyijiong
 * @date 2019/9/29
 */
public class RedissonBuilder implements LockBuilder {

  private RedissonLockConfiguration redissonLockConfiguration;

  public RedissonBuilder() {
    this.redissonLockConfiguration = new RedissonLockConfiguration().initConfig();
  }

  public RedissonBuilder(RedissonLockConfiguration redissonLockConfiguration) {
    this.redissonLockConfiguration = redissonLockConfiguration;
  }

  @Override
  public DistributedLock build(String lockId) {
    return build(lockId, redissonLockConfiguration);
  }

  @Override
  public DistributedLock build(String lockId, LockConfiguration configuration) {
    RedissonLockConfiguration config = (RedissonLockConfiguration) configuration;
    RLock lock = config.getRedissonClient().getLock(lockId);
    return new RedissonLockAdapter(lock, config.getLockTimeout());
  }
}
