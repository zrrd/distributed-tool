package cn.learn.distributed.lock.adapter.redis.lettuce;

import com.google.common.base.Preconditions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisScriptingCommands;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import lombok.Getter;

/**
 * redis实际操作
 *
 * @author shaoyijiong
 * @date 2019/9/24
 */
@Getter
public class RedisOperation {


  private RedisClient redisClient;

  /**
   * 重试等待时间 500毫秒 0.5秒
   */
  private int retryAwait = 500;

  /**
   * redis 锁的最长时间 60_000毫秒 60秒
   */
  private int lockTimeout = 60_000;

  /**
   * 简单构造
   */
  RedisOperation(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  /**
   * 带时间的构造
   *
   * @param redisClient redis 客户端
   * @param retryAwait 重试时间
   * @param lockTimeout redis超时时间 根据业务执行时间长短
   */
  RedisOperation(RedisClient redisClient, int retryAwait, int lockTimeout) {
    Preconditions
        .checkArgument(retryAwait > 0 && lockTimeout > 0, "retryAwait or lockTimeout must be greater than 0");
    Preconditions.checkArgument(lockTimeout > retryAwait, "lockTimeout must be greater then retryAwait");
    this.redisClient = redisClient;
    this.retryAwait = retryAwait;
    this.lockTimeout = lockTimeout;
  }

  String tryRedisLock(String lockId, long time, TimeUnit unit) {
    if (time < 0) {
      return null;
    }
    final long deadline = System.nanoTime() + unit.toNanos(time);
    String lockValue;
    for (; ; ) {
      lockValue = createKey(lockId);
      if (lockValue != null) {
        break;
      }
      time = deadline - System.nanoTime();
      if (time <= 0L) {
        break;
      }
      LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(retryAwait));
    }
    return lockValue;
  }

  void unlockRedisLock(String key, String value) {
    try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
      RedisScriptingCommands<String, String> commands = connect.sync();
      String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then "
          + "return redis.call('del', KEYS[1]) else return 0 end";
      Object result = commands.eval(luaScript, ScriptOutputType.INTEGER, new String[]{key}, value);
    }
  }

  private String createKey(String lockId) {
    try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
      RedisCommands<String, String> commands = connect.sync();
      SetArgs args = SetArgs.Builder.nx().px(lockTimeout);
      String value = lockId + ":" + UUID.randomUUID();
      String success = commands.set(lockId, value, args);
      if (success != null) {
        return value;
      }
    }
    return null;
  }
}
