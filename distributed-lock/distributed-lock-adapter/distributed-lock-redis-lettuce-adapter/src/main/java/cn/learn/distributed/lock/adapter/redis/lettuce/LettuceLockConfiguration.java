package cn.learn.distributed.lock.adapter.redis.lettuce;

import cn.learn.distributed.lock.core.LockConfiguration;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI.Builder;
import java.time.Duration;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * @author shaoyijiong
 * @date 2019/9/24
 */
@Getter
@Setter
public class LettuceLockConfiguration implements LockConfiguration {

  private String host = "47.99.73.15";
  private int port = 6379;
  private int database = 0;
  private String password;
  private Duration timeout;
  private RedisClient redisClient;

  /**
   * 初始化一个redisClient客户端
   */
  public LettuceLockConfiguration initRedisClient() {
    Builder builder = Builder.redis(host, port).withDatabase(database);
    Optional.ofNullable(password).ifPresent(p -> builder.withPassword(password));
    Optional.ofNullable(timeout).ifPresent(t -> builder.withTimeout(timeout));
    redisClient = RedisClient.create(builder.build());
    return this;
  }
}
