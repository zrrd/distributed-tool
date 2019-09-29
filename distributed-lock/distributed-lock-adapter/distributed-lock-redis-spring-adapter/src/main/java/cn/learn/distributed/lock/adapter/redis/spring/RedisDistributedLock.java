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
      state.incrementAndGet();
      return true;
    }
    String lockValue = redisOperation.tryRedisLock(lockId, timeout, unit);
    // 成功获取到redis锁
    if (lockValue != null) {
      state.incrementAndGet();
      setExclusiveOwnerThread(current);
      threadLocalLockValue.set(lockValue);
      log.debug("thread:[{}],lockId[{}],get the lock success,lockValue:[{}]", current, lockId, lockValue);
      return true;
    }
    log.debug("thread[{}],lockId[{}],get the lock fail", current, lockId);
    return false;
  }

  @Override
  public boolean tryLock(Duration timeout) {
    return tryLock(timeout.getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void unlock() {
    int c = getState().decrementAndGet();
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
    redisOperation.unlockRedisLock(lockId, lockValue);
    if (c == 0) {
      setExclusiveOwnerThread(null);
      threadLocalLockValue.remove();
    }
    log.debug("thread:[{}],lockId:[{}],lockValue:[{}] unlock success", Thread.currentThread(), lockId, lockValue);
  }
}
