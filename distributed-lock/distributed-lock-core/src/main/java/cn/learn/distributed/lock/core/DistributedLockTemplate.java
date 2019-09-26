package cn.learn.distributed.lock.core;


import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
public interface DistributedLockTemplate {

  /**
   * 在锁的范围内执行一个任务
   *
   * @param lockId 分布式锁的id
   * @param timeout 超时时长
   * @param callBack 回调函数
   * @return 执行完毕返回值
   */
  default <T> T invoke(String lockId, Duration timeout, Callback<T> callBack) {
    return invoke(lockId, timeout.toMillis(), TimeUnit.MILLISECONDS, callBack);
  }


  /**
   * 在锁的范围内执行一个任务
   *
   * @param lockId 分布式锁的id
   * @param timeout 超时时长
   * @param unit 时长单位
   * @param callBack 回调函数
   * @return 执行完毕返回值
   */
  <T> T invoke(String lockId, long timeout, TimeUnit unit, Callback<T> callBack);
}
