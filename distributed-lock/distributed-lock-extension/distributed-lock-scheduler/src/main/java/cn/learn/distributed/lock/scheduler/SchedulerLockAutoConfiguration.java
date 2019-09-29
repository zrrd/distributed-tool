package cn.learn.distributed.lock.scheduler;

import cn.learn.distributed.lock.core.DistributedLockTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SchedulerLockAutoConfiguration implements ApplicationContextAware {

  private ApplicationContext applicationContext;



  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public MethodProxyScheduledLockAdvisor methodProxyScheduledLockAdvisor(
      DistributedLockTemplate distributedLockTemplate) {
    return new MethodProxyScheduledLockAdvisor(distributedLockTemplate, applicationContext);
  }


}
