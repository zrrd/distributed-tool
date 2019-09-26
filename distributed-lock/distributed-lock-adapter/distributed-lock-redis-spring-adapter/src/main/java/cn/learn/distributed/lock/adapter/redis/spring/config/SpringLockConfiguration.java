package cn.learn.distributed.lock.adapter.redis.spring.config;

import cn.learn.distributed.lock.core.LockConfiguration;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * spring lock 配置
 *
 * @author shaoyijiong
 * @date 2019/9/26
 */
@Data
public class SpringLockConfiguration implements LockConfiguration {

  private StringRedisTemplate redisTemplate;
  private int retryAwait;
  private int lockTimeout;

  /**
   * 这边redis的配置如ip port 使用spring redis starter的配置
   *
   * @param redisTemplate 通过注入的方式实现
   * @param retryAwait 重试间隔
   * @param lockTimeout 锁时间
   */
  public SpringLockConfiguration(StringRedisTemplate redisTemplate, int retryAwait, int lockTimeout) {
    this.redisTemplate = redisTemplate;
    this.retryAwait = retryAwait;
    this.lockTimeout = lockTimeout;
  }
}
