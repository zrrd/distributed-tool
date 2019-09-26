package cn.learn.distributed.lock.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author shaoyijiong
 * @date 2019/9/25
 */
@EnableConfigurationProperties
@SpringBootApplication
public class GoApplication {

  public static void main(String[] args) {
    SpringApplication.run(GoApplication.class, args);
  }
}
