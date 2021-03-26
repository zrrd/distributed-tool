package cn.learn.distributed.id;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI.Builder;
import java.util.Optional;

/**
 * @author shaoyijiong
 * @date 2021/3/26
 */
public class RedisFactory {

  private static final String HOST = "121.36.136.81";
  private static final int PORT = 6379;
  private static final int DATABASE = 0;
  private static final String PASSWORD = "5YauX3cf";

  private static RedisClient redisClient = null;

  public static RedisClient getInstance() {
    if (redisClient == null) {
      synchronized (RedisFactory.class) {
        Builder builder = Builder.redis(HOST, PORT).withDatabase(DATABASE);
        Optional.of(PASSWORD).ifPresent(p -> builder.withPassword(PASSWORD));
        redisClient = RedisClient.create(builder.build());
      }
    }
    return redisClient;
  }

}
