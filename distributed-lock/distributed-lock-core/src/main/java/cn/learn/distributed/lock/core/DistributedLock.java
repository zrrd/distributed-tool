package cn.learn.distributed.lock.core;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 *
 * @author shaoyijiong
 * @date 2019/9/24
 */
public interface DistributedLock {

  /**
   * 开启锁
   */
  void lock();

  /**
   * 尝试获取锁
   */
  boolean tryLock();

  /**
   * 尝试获取锁
   *
   * @param timeout 等待时长
   * @param unit 单位
   */
  boolean tryLock(long timeout, TimeUnit unit);

  /**
   * 尝试获取锁
   *
   * @param timeout 等待时长
   */
  boolean tryLock(Duration timeout);

  /**
   * 解锁
   */
  void unLock();
}
