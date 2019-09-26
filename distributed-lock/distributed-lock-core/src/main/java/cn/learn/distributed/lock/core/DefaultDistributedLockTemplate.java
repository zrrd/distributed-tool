package cn.learn.distributed.lock.core;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
@Slf4j
public class DefaultDistributedLockTemplate implements DistributedLockTemplate {

  private LockBuilder lockBuilder;
  private LockConfiguration lockConfiguration;

  public DefaultDistributedLockTemplate(LockBuilder lockBuilder, LockConfiguration lockConfiguration) {
    this.lockBuilder = lockBuilder;
    this.lockConfiguration = lockConfiguration;
  }

  public DefaultDistributedLockTemplate(LockBuilder lockBuilder) {
    this.lockBuilder = lockBuilder;
  }

  @Override
  public <T> T invoke(String lockId, long timeout, TimeUnit unit, Callback<T> callBack) {
    DistributedLock lock = null;
    boolean getLock = false;
    try {
      lock = lockConfiguration == null ? lockBuilder.build(lockId) : lockBuilder.build(lockId, lockConfiguration);
      if (lock.tryLock(timeout, unit)) {
        getLock = true;
        return callBack.onGetLock();
      } else {
        return callBack.onTimeout();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      if (getLock) {
        lock.unLock();
      }
    }
    return null;
  }
}
