package cn.learn.distributed.lock.adapter.redis.spring;

import cn.learn.distributed.lock.adapter.redis.spring.config.RedisLockBuilder;
import cn.learn.distributed.lock.core.DistributedLock;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author shaoyijiong
 * @date 2019/9/25
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Test
  public void test1() {
    Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("a", "b", 60_000, TimeUnit.MILLISECONDS);
    System.out.println(aBoolean);
  }

  @Test
  public void test2() {
    String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then "
        + "return redis.call('del', KEYS[1]) else return 0 end";
    RedisScript script = RedisScript.of(luaScript, Long.class);
    Long execute = redisTemplate.<Long>execute(script, Collections.singletonList("a"), "b");
    System.out.println(execute);
  }

  @Autowired
  private RedisLockBuilder builder;
  private volatile int i;

  @Test
  public void test3() throws InterruptedException {
    DistributedLock lock = builder.build("lockTest");
    Thread t1 = new Thread(() -> {
      for (int j = 0; j < 100; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unLock();
        } else {
          System.out.println("not get");
        }
      }
    });

    Thread t2 = new Thread(() -> {
      for (int j = 0; j < 100; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unLock();
        } else {
          System.out.println("not get");
        }
      }
    });

    Thread t3 = new Thread(() -> {
      for (int j = 0; j < 100; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unLock();
        } else {
          System.out.println("not get");
        }
      }
    });

    Thread t4 = new Thread(() -> {
      for (int j = 0; j < 100; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unLock();
        } else {
          System.out.println("not get");
        }
      }
    });

    t1.start();
    t2.start();
    t3.start();
    t4.start();
    t1.join();
    t2.join();
    t3.join();
    t4.join();

    System.out.println(i);
  }

  /**
   * 任务时长超过redis测试
   */
  @Test
  public void test4() throws InterruptedException {
    DistributedLock lock = builder.build("lockTest");
    Thread t1 = new Thread(() -> {
      if (lock.tryLock(Duration.ofSeconds(100))) {
        System.out.println("t1 get the lock");
        try {
          Thread.sleep(Duration.ofSeconds(61).toMillis());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          lock.unLock();
          System.out.println("t1 unlock");
        }
      }

    });

    Thread t2 = new Thread(() -> {
      if (lock.tryLock(Duration.ofSeconds(100))) {
        System.out.println("t2 get the lock");
        try {
          Thread.sleep(Duration.ofSeconds(61).toMillis());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          lock.unLock();
          System.out.println("t2 unlock");
        }
      }

    });

    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
