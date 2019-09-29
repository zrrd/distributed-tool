package cn.learn.distributed.lock.adapter.redis.redisson;

import cn.learn.distributed.lock.core.DistributedLock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;

/**
 * 由于 redisson 原来就实现了redis分布式功能 , 所有在这里只是一个适配器
 *
 * @author shaoyijiong
 * @date 2019/9/29
 */
public class RedissonLockAdapter implements DistributedLock {

  /**
   * redis 锁的最长时间 60_000毫秒 60秒
   */
  private int lockTimeout = 60_000;

  private final RLock rLock;

  public RedissonLockAdapter(RLock rLock) {
    this.rLock = rLock;
  }

  public RedissonLockAdapter(RLock rLock, int lockTimeout) {
    this.lockTimeout = lockTimeout;
    this.rLock = rLock;
  }

  @Override
  public void lock() {
    rLock.lock(lockTimeout, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean tryLock() {
    return rLock.tryLock();
  }

  @Override
  public boolean tryLock(long timeout, TimeUnit unit) {
    timeout = unit.toMillis(timeout);
    try {
      return rLock.tryLock(timeout, lockTimeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean tryLock(Duration timeout) {
    return this.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void unlock() {
    rLock.unlock();
  }
}
