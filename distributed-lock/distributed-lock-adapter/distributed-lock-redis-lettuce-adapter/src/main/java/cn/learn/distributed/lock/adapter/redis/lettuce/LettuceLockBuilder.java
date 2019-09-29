package cn.learn.distributed.lock.adapter.redis.lettuce;

import cn.learn.distributed.lock.core.DistributedLock;
import cn.learn.distributed.lock.core.LockBuilder;
import cn.learn.distributed.lock.core.LockConfiguration;
import io.lettuce.core.RedisClient;

/**
 * 锁构建器
 *
 * @author shaoyijiong
 * @date 2019/9/26
 */
public class LettuceLockBuilder implements LockBuilder {

  private final LockConfiguration lockConfiguration;

  /**
   * 使用默认配置
   */
  public LettuceLockBuilder() {
    this.lockConfiguration = new LettuceLockConfiguration().initRedisClient();
  }

  /**
   * 使用自定义配置
   */
  public LettuceLockBuilder(LockConfiguration lockConfiguration) {
    this.lockConfiguration = lockConfiguration;
  }

  /**
   * 使用通用配置构建锁
   */
  @Override
  public DistributedLock build(String lockId) {
    return build(lockId, lockConfiguration);
  }

  /**
   * 为该锁自定义配置
   */
  @Override
  public DistributedLock build(String lockId, LockConfiguration configuration) {
    LettuceLockConfiguration lettuceLockConfiguration = (LettuceLockConfiguration) configuration;
    RedisClient redisClient = lettuceLockConfiguration.getRedisClient();
    return new RedisDistributedLock(lockId, redisClient);
  }
}
