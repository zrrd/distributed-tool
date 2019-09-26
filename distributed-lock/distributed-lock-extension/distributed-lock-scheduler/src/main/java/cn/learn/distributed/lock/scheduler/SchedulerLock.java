package cn.learn.distributed.lock.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerLock {

  /**
   * 锁的id
   */
  String lockId() default "";

  /**
   * 锁时长
   */
  int timeout() default 0;

  /**
   * 锁时长单位
   */
  TimeUnit unit() default TimeUnit.MILLISECONDS;


}
