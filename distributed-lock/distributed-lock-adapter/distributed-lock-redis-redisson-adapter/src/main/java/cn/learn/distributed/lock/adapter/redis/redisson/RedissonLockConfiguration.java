package cn.learn.distributed.lock.adapter.redis.redisson;

import cn.learn.distributed.lock.core.LockConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author shaoyijiong
 * @date 2019/9/29
 */
@Getter
@Setter
public class RedissonLockConfiguration implements LockConfiguration {


  private String host = "47.99.73.15";
  private int port = 6379;
  private int database = 0;
  private String password;
  /**
   * 最大连接池数量
   */
  private int poolSize = 64;
  /**
   * 最小连接池数量
   */
  private int minIdleSize = 24;
  /**
   * redis 锁的最长时间 60_000毫秒 60秒
   */
  private int lockTimeout = 60_000;
  private Config config;
  private RedissonClient redissonClient;

  public RedissonLockConfiguration initConfig() {
    Config config = new Config();
    //redis://47.99.73.15:6379
    config.useSingleServer().setAddress("redis://" + host + ":" + port).setDatabase(database).setPassword(password)
        .setConnectionPoolSize(poolSize).setConnectionMinimumIdleSize(minIdleSize);
    this.config = config;
    this.redissonClient = Redisson.create(config);
    return this;
  }
}
