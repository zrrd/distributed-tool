package cn.learn.distributed.lock.adapter.redis.redisson;

import cn.learn.distributed.lock.core.DistributedLock;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author shaoyijiong
 * @date 2019/9/29
 */
public class RedissonTest {

  /**
   * redisson 配置
   */
  private RedissonClient createClient() {
    Config config = new Config();
    config.useSingleServer()
        //可以用"rediss://"来启用SSL连接
        .setAddress("redis://47.99.73.15:6379");
    return Redisson.create(config);
  }

  /**
   * 操作测试
   */
  @Test
  public void test1() {
    RedissonClient client = createClient();
    RAtomicLong aLong = client.getAtomicLong("long");
    long andAdd = aLong.getAndAdd(1);
    System.out.println(andAdd);
  }


  private int num = 0;

  /**
   * 锁测试
   */
  @Test
  public void test2() throws InterruptedException {
    RedissonClient client = createClient();
    RLock lock = client.getLock("lock_test");
    Thread thread1 = new Thread(() -> {
      lock.lock(1000, TimeUnit.SECONDS);
      for (int i = 0; i < 100000; i++) {
        num++;
      }
      lock.unlock();
    });
    Thread thread2 = new Thread(() -> {
      lock.lock(1000, TimeUnit.SECONDS);
      for (int i = 0; i < 100000; i++) {
        num++;
      }
      lock.unlock();
    });
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    System.out.println(num);
  }

  @Test
  public void test3() throws InterruptedException {
    RedissonLockConfiguration configuration = new RedissonLockConfiguration().initConfig();
    RedissonBuilder redissonBuilder = new RedissonBuilder(configuration);
    DistributedLock lock = redissonBuilder.build("lock_test");
    Thread thread1 = new Thread(() -> {
      lock.lock();
      for (int i = 0; i < 100000; i++) {
        num++;
      }
      lock.unlock();
    });
    Thread thread2 = new Thread(() -> {
      lock.lock();
      for (int i = 0; i < 100000; i++) {
        num++;
      }
      lock.unlock();
    });
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    System.out.println(num);
  }
}
