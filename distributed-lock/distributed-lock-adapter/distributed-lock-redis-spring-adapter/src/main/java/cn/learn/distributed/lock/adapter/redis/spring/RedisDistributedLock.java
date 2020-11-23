package cn.learn.distributed.lock.adapter.redis.spring;

import cn.learn.distributed.lock.core.DistributedLock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author shaoyijiong
 * @date 2019/9/25
 */
@Slf4j
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PACKAGE)
public class RedisDistributedLock implements DistributedLock {

  /**
   * 锁id  redis中的key
   */
  private String lockId;
  /**
   * 当前获取锁的线程
   */
  private transient Thread exclusiveOwnerThread;
  /**
   * 锁的重入次数
   */
  private AtomicInteger state;
  /**
   * redis操作
   */
  private RedisOperation redisOperation;
  /**
   * 用于存储 锁住的value , 线程只能释放自己所拥有的锁
   */
  private ThreadLocal<String> threadLocalLockValue = new ThreadLocal<>();

  /**
   * 构造
   *
   * @param lockId 对应redis 的key
   * @param redisTemplate lettuce 操作客户端
   */
  public RedisDistributedLock(String lockId, StringRedisTemplate redisTemplate) {
    this.lockId = lockId;
    this.redisOperation = new RedisOperation(redisTemplate);
    state = new AtomicInteger();
  }

  /**
   * 构造
   *
   * @param lockId 对应redis 的key
   * @param retryAwait 重试间隔
   * @param lockTimeout redis 锁最大时长
   */
  public RedisDistributedLock(String lockId, StringRedisTemplate redisTemplate, int retryAwait, int lockTimeout) {
    this.lockId = lockId;
    this.redisOperation = new RedisOperation(redisTemplate, retryAwait, lockTimeout);
    state = new AtomicInteger();
  }

  @Override
  public void lock() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean tryLock() {
    return this.tryLock(0, TimeUnit.SECONDS);
  }

  @Override
  public boolean tryLock(long timeout, TimeUnit unit) {
    // 判断持有锁的是否为当前线程
    final Thread current = Thread.currentThread();
    Thread exclusiveOwnerThread = getExclusiveOwnerThread();
    // 当前线程为持有锁的线程
    if (exclusiveOwnerThread != null && current == exclusiveOwnerThread) {
      log.debug("thread:[{}] ReenTrantLock", current);
      // 如果当前线程即为持有锁的线程 重入次数加一 返回成功
      state.incrementAndGet();
      return true;
    }
    // 实际操作 Redis 加锁
    String lockValue = redisOperation.tryRedisLock(lockId, timeout, unit);
    // 成功获取到redis锁
    if (lockValue != null) {
      // 重入次数加一
      state.incrementAndGet();
      // 设置持有锁的线程为当前线程
      setExclusiveOwnerThread(current);
      // 在threadLocal中保存随机值用于后续解锁
      threadLocalLockValue.set(lockValue);
      log.debug("thread:[{}],lockId[{}],get the lock success,lockValue:[{}]", current, lockId, lockValue);
      return true;
    }
    // 未获取到锁
    log.debug("thread[{}],lockId[{}],get the lock fail", current, lockId);
    return false;
  }

  @Override
  public boolean tryLock(Duration timeout) {
    return tryLock(timeout.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void unlock() {
    // 重入次数减一
    int c = getState().decrementAndGet();
    // 获取随机值
    String lockValue = threadLocalLockValue.get();
    // t1 t2 两个任务 , t1的任务时长超过redis的锁时长 , 导致t1还在进行任务的时候t2拿到了锁 , 这时候锁的拥有者线程为t2 ,
    // t1 完成任务后进行解锁操作 , 就会 throw IllegalMonitorStateException()
    // 解决方式 1. 优化任务执行时间 2. 延长redis 的 lockTimeout
    if (lockValue == null || Thread.currentThread() != getExclusiveOwnerThread()) {
      log.error(
          "currentThread:[{}] , ownerThread:[{}] , lockValue:[{}] , maybe your task took too long or redis's lockTimeout is too shout [{}] ",
          Thread.currentThread(), getExclusiveOwnerThread(), lockValue, redisOperation.getLockTimeout());
      throw new IllegalMonitorStateException();
    }
    // redis 中解锁
    redisOperation.unlockRedisLock(lockId, lockValue);
    // 如果重入次数为0 , 释放锁的持有线程
    if (c == 0) {
      setExclusiveOwnerThread(null);
      threadLocalLockValue.remove();
    }
    log.debug("thread:[{}],lockId:[{}],lockValue:[{}] unlock success", Thread.currentThread(), lockId, lockValue);
  }
}
