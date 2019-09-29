package cn.learn.distributed.lock.scheduler;

import cn.learn.distributed.lock.core.Callback;
import cn.learn.distributed.lock.core.DefaultDistributedLockTemplate;
import cn.learn.distributed.lock.core.DistributedLockTemplate;
import cn.learn.distributed.lock.core.LockBuilder;
import cn.learn.distributed.lock.core.LockConfiguration;
import com.google.common.base.Strings;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * aop 切面定义
 *
 * @author shaoyijiong
 * @date 2019/9/26
 */
@Slf4j
public class MethodProxyScheduledLockAdvisor extends AbstractPointcutAdvisor {


  /**
   * 切点 定义在哪里执行
   */
  private final AnnotationMatchingPointcut pointcut = AnnotationMatchingPointcut
      .forMethodAnnotation(SchedulerLock.class);

  /**
   * 切面 定义执行方法
   */
  private Advice advice;

  public MethodProxyScheduledLockAdvisor(DistributedLockTemplate distributedLockTemplate,
      ApplicationContext applicationContext) {
    String keyPrefix = "";
    String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
    if (activeProfiles.length > 0) {
      keyPrefix = activeProfiles[0] + ":";
    }
    this.advice = new LockingInterceptor(distributedLockTemplate, keyPrefix);

  }

  @Override
  public Pointcut getPointcut() {
    return pointcut;
  }

  @Override
  public Advice getAdvice() {
    return advice;
  }

  private static class LockingInterceptor implements MethodInterceptor {

    /**
     * redis 前缀
     */
    private String keyPrefix;
    private final DistributedLockTemplate distributedLockTemplate;

    public LockingInterceptor(LockBuilder lockBuilder, LockConfiguration lockConfiguration, String keyPrefix) {
      this.distributedLockTemplate = new DefaultDistributedLockTemplate(lockBuilder, lockConfiguration);
      setKeyPrefix(keyPrefix);
    }

    public LockingInterceptor(LockBuilder lockBuilder, String keyPrefix) {
      this.distributedLockTemplate = new DefaultDistributedLockTemplate(lockBuilder);
      setKeyPrefix(keyPrefix);
    }

    public LockingInterceptor(DistributedLockTemplate distributedLockTemplate, String keyPrefix) {
      this.distributedLockTemplate = distributedLockTemplate;
      setKeyPrefix(keyPrefix);
    }

    public void setKeyPrefix(String keyPrefix) {
      this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      // 有返回值不支持
      Class<?> returnType = invocation.getMethod().getReturnType();
      if (!void.class.equals(returnType) && !Void.class.equals(returnType)) {
        throw new UnsupportedOperationException();
      }
      SchedulerLock schedulerLock = AnnotatedElementUtils
          .findMergedAnnotation(invocation.getMethod(), SchedulerLock.class);
      // 没有注解直接执行
      if (schedulerLock == null) {
        return invocation.proceed();
      }
      // 为空的话 默认为方法名
      String lockId = keyPrefix + (Strings.isNullOrEmpty(schedulerLock.lockId()) ? invocation.getMethod().getName()
          : schedulerLock.lockId());
      distributedLockTemplate.invoke(lockId, schedulerLock.timeout(), schedulerLock.unit(),
          new Callback<Void>() {
            @Override
            public Void onGetLock() {
              log.debug("invoke scheduler [{}]", lockId);
              try {
                // 拿到锁执行
                invocation.proceed();
              } catch (Throwable throwable) {
                throwable.printStackTrace();
              }
              return null;
            }

            @Override
            public Void onTimeout() {
              log.debug("pass scheduler [{}]", lockId);
              // 没有拿到锁 不执行
              return null;
            }
          });
      return null;
    }
  }
}
