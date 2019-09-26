package cn.learn.distributed.lock.adapter.redis.spring.config;

import cn.learn.distributed.lock.core.DefaultDistributedLockTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author shaoyijiong
 * @date 2019/9/25
 */
@Configuration
@EnableConfigurationProperties(RedisLockProperties.class)
public class RedisLockAutoConfiguration {

  @Bean
  public RedisLockBuilder redisLockBuilder(StringRedisTemplate stringRedisTemplate,
      RedisLockProperties redisLockProperties) {
    return new RedisLockBuilder(new SpringLockConfiguration(stringRedisTemplate, redisLockProperties.getRetryAwait(),
        redisLockProperties.getLockTimeout()));
  }

  @Bean
  public DefaultDistributedLockTemplate defaultDistributedLockTemplate(RedisLockBuilder redis) {
    return new DefaultDistributedLockTemplate(redis);
  }

}
