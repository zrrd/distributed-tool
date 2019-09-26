package cn.learn.distributed.lock.core;

/**
 * 锁构建器
 *
 * @author shaoyijiong
 * @date 2019/9/26
 */
public interface LockBuilder {

  /**
   * @param lockId 锁的分布式锁id
   * @return 分布式锁
   */
  DistributedLock build(String lockId);

  /***
   * @param lockId 分布式锁id
   * @param configuration 锁配置
   * @return 分布式锁
   */
  DistributedLock build(String lockId, LockConfiguration configuration);

}
