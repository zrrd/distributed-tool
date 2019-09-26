package cn.learn.distributed.test;

import cn.learn.distributed.lock.core.Callback;
import cn.learn.distributed.lock.core.DistributedLockTemplate;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

  @Autowired
  private DistributedLockTemplate template;
  @Autowired
  private JdbcTemplate jdbcTemplate;


  @Test
  public void test1() {
    System.out.println(jdbcTemplate.queryForObject("SELECT num FROM lock_test WHERE id = 1", Integer.class));
  }


  @Test
  public void test2() {
    optSql();
  }

  /**
   * 不加锁测试
   */
  @Test
  public void test3() throws InterruptedException {
    while (true) {
      LocalTime time = LocalTime.of(15, 25);
      if (LocalTime.now().equals(time)) {
        break;
      }
    }

    System.out.println("start");

    int size = 2;
    CountDownLatch latch = new CountDownLatch(size);
    CountDownLatch join = new CountDownLatch(size);
    ExecutorService executor = Executors.newFixedThreadPool(size);
    for (int j = 0; j < size; j++) {
      executor.submit(() -> {
        // 等待同步执行
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        optSql();
        join.countDown();
      });
      latch.countDown();
    }

    join.await();
    outNum();
  }


  /**
   * DistributedLockTemplate 测试
   */
  @Test
  public void test5() throws InterruptedException {
    while (true) {
      LocalTime time = LocalTime.of(15, 40);
      if (LocalTime.now().equals(time)) {
        break;
      }
    }

    System.out.println("start");

    int size = 2;
    CountDownLatch latch = new CountDownLatch(size);
    CountDownLatch join = new CountDownLatch(size);
    ExecutorService executor = Executors.newFixedThreadPool(size);
    for (int j = 0; j < size; j++) {
      executor.submit(() -> {
        // 等待同步执行
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        template.invoke("db_lock", Duration.ofSeconds(100), new Callback<Void>() {
          @Override
          public Void onGetLock() {
            System.exit(-1);
            optSql();
            return null;
          }

          @Override
          public Void onTimeout() {
            System.out.println("not get the lock");
            return null;
          }
        });
        join.countDown();
      });
      latch.countDown();
    }

    join.await();
    outNum();  }

  private void optSql() {
    int num = jdbcTemplate.queryForObject("SELECT num FROM lock_test WHERE id = 1", Integer.class);
    try {
      // 睡眠一秒模拟业务操作
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    int plus = num + 1;
    jdbcTemplate.update("UPDATE lock_test SET num = ? WHERE id = 1", plus);
  }

  private void outNum() {
    System.out.println(jdbcTemplate.queryForObject("SELECT num FROM lock_test WHERE id = 1", Integer.class));
  }
}
