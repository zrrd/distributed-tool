package cn.learn.distributed.lock.adapter.redis.lettuce;

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
public class LettuceLockConfiguration {

  private String host = "localhost";
  private int port = 6379;
  private int database = 0;
  private String password;
  private Duration timeout;

  /**
   * 创建一个redisClient 客户端
   */
  public RedisClient createRedisClient() {
    Builder builder = Builder.redis(host, port).withDatabase(database);
    Optional.ofNullable(password).ifPresent(p -> builder.withPassword(password));
    Optional.ofNullable(timeout).ifPresent(t -> builder.withTimeout(timeout));
    return RedisClient.create(builder.build());
  }
}
