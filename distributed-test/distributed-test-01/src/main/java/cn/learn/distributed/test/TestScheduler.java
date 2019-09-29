package cn.learn.distributed.test;

import cn.learn.distributed.lock.scheduler.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author shaoyijiong
 * @date 2019/9/26
 */
@EnableScheduling
@Component
public class TestScheduler {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @SchedulerLock
  @Scheduled(cron = "0/15 * * * * *")
  public void testGo() {
    System.out.println("start lock");
    optSql(1);
  }

  @Scheduled(cron = "0/15 * * * * *")
  public void testGounlock() {
    System.out.println("start unlock");
    optSql(2);
  }


  private void optSql(Integer id) {
    int num = jdbcTemplate.queryForObject("SELECT num FROM lock_test WHERE id = ?", Integer.class, id);
    try {
      // 睡眠一秒模拟业务操作
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    int plus = num + 1;
    jdbcTemplate.update("UPDATE lock_test SET num = ? WHERE id = ?", plus, id);
  }


}
