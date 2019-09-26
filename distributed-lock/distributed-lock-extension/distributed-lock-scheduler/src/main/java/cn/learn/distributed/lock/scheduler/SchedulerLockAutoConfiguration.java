package cn.learn.distributed.lock.scheduler;

import cn.learn.distributed.lock.core.DistributedLockTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SchedulerLockAutoConfiguration {


  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public MethodProxyScheduledLockAdvisor methodProxyScheduledLockAdvisor(DistributedLockTemplate distributedLockTemplate) {
    return new MethodProxyScheduledLockAdvisor(distributedLockTemplate);
  }


}
