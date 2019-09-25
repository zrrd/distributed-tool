package cn.learn.distributed.lock.adapter.redis.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author shaoyijiong
 * @date 2019/9/25
 */
@Data
@Component
@ConfigurationProperties(prefix = "distributed.lock.redis")
public class RedisLockProperties {

  /**
   * 重试间隔时间 单位毫秒
   */
  private int retryAwait = 500;
  /**
   * redis锁默认时间
   */
  private int lockTimeout = 60_000;
}
