package cn.learn.distributed.lock.core;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
public interface Callback<T> {

  /**
   * 成功拿到锁
   */
  T onGetLock();

  /**
   * 等待超时没有拿到锁
   */
  T onTimeout();
}
