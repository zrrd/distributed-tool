import cn.learn.distributed.lock.adapter.redis.lettuce.RedisDistributedLock;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * @author shaoyijiong
 * @date 2019/9/24
 */
@SuppressWarnings("all")
public class RedisTest {

  /**
   * 加锁测试
   */
  @Test
  public void test1() {
    RedisClient redisClient = RedisClient.create("redis://47.99.73.15");
    StatefulRedisConnection<String, String> connect = redisClient.connect();
    RedisCommands<String, String> commands = connect.sync();
    SetArgs args = SetArgs.Builder.nx().px(9999_000);
    String value = commands.set("k", "v", args);
    System.out.println(value);
    connect.close();
    redisClient.shutdown();
  }

  /**
   * 解锁测试
   */
  @Test
  public void test2() {
    RedisClient redisClient = RedisClient.create("redis://47.99.73.15");
    StatefulRedisConnection<String, String> connect = redisClient.connect();
    RedisCommands<String, String> commands = connect.sync();

    String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then "
        + "return redis.call('del', KEYS[1]) else return 0 end";
    Object eval = commands.eval(luaScript, ScriptOutputType.INTEGER, new String[]{"k"}, "v");
    System.out.println(eval);
    connect.close();
    redisClient.shutdown();
  }

  RedisDistributedLock lock = new RedisDistributedLock("testLock", RedisClient.create("redis://47.99.73.15"));
  private static volatile int i = 0;


  /**
   * 并发测试
   */
  @Test
  public void test3() throws InterruptedException {
    Thread t1 = new Thread(() -> {
      for (int j = 0; j < 1000; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unlock();
        } else {
          System.out.println("not get");
        }
      }
    });

    Thread t2 = new Thread(() -> {
      for (int j = 0; j < 1000; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unlock();
        } else {
          System.out.println("not get");
        }
      }
    });

    Thread t3 = new Thread(() -> {
      for (int j = 0; j < 1000; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unlock();
        } else {
          System.out.println("not get");
        }
      }
    });

    Thread t4 = new Thread(() -> {
      for (int j = 0; j < 1000; j++) {
        if (lock.tryLock(Duration.ofSeconds(10))) {
          i++;
          lock.unlock();
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

    Thread t1 = new Thread(() -> {
      if (lock.tryLock(Duration.ofSeconds(100))) {
        System.out.println("t1 get the lock");
        try {
          Thread.sleep(Duration.ofSeconds(61).toMillis());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          lock.unlock();
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
          lock.unlock();
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
