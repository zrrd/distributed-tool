package cn.learn.distributed.id;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.net.InetAddress;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shaoyijiong
 * @date 2021/3/26
 */
@Slf4j
public class MachineIdGenerate {

  private static RedisClient redisClient = RedisFactory.getInstance();
  private static String localIp = getIPAddress(); // 192.168.0.233
  private static Long ip_ = Long.parseLong(localIp.replaceAll("\\.", ""));
  /*
   * 机器id
   */
  public static Integer machineId;
  private static String serviceName = "a";
  

  public static void main(String[] args) {

  }


  @SneakyThrows
  private static String getIPAddress() {
    InetAddress address = InetAddress.getLocalHost();
    return address.getHostAddress();
  }


  /**
   * 主方法：首先获取机器 IP 并 % 32 得到 0-31 使用 业务名 + 组名 + IP 作为 Redis 的 key，机器IP作为 value，存储到Redis中
   */
  public static Integer createMachineId() {
    try {
      // 向redis注册，并设置超时时间
      log.info("注册一个机器ID到Redis " + machineId + " IP:" + localIp);
      Boolean flag = registerMachine(machineId, localIp);
      // 注册成功
      if (flag) {
        // 启动一个线程更新超时时间
        updateExpTimeThread();
        // 返回机器Id
        log.info("Redis中端口没有冲突 " + machineId + " IP:" + localIp);
        return machineId;
      }
      // 注册失败，可能原因 Hash%32 的结果冲突
      if (!checkIfCanRegister()) {
        // 如果 0-31 已经用完，使用 32-64之间随机的ID
        getRandomMachineId();
        createMachineId();
      } else {
        // 如果存在剩余的ID
        log.warn("Redis中端口冲突了，使用 0-31 之间未占用的Id " + machineId + " IP:" + localIp);
        createMachineId();
      }
    } catch (Exception e) {
      // 获取 32 - 63 之间的随机Id
      // 返回机器Id
      log.error("Redis连接异常,不能正确注册雪花机器号 " + machineId + " IP:" + localIp, e);
      log.warn("使用临时方案，获取 32 - 63 之间的随机数作为机器号，请及时检查Redis连接");
      getRandomMachineId();
      return machineId;
    }
    return machineId;
  }

  private static Boolean registerMachine(Integer machineId, String localIp) {
    // try with resources 写法，出异常会释放括号内的资源 Java7特性
    try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
      // key 业务号 + 数据中心ID + 机器ID value 机器IP
      Boolean result = connect.sync().setnx(serviceName + machineId, localIp);
      if (result) {
        // 过期时间 1 天
        connect.sync().expire(serviceName + machineId, 60 * 60 * 24);
        return true;
      } else {
        // 如果Key存在，判断Value和当前IP是否一致，一致则返回True
        String value = connect.sync().get(serviceName + machineId);
        if (localIp.equals(value)) {
          // IP一致，注册机器ID成功
          connect.sync().expire(serviceName + machineId, 60 * 60 * 24);
          return true;
        }
        return false;
      }
    }
  }

  /**
   * 检查是否被注册满了
   */
  private static Boolean checkIfCanRegister() {
    // 判断0~31这个区间段的机器IP是否被占满
    try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
      Long flag = 0L;
      for (int i = 0; i < 32; i++) {
        flag = connect.sync().exists(serviceName + i);
        // 如果不存在。设置机器Id为这个不存在的数字
        if (flag != 0) {
          machineId = i;
          break;
        }
      }
      return flag != 0;
    }
  }



  private static void updateExpTimeThread() {
    // 开启一个线程执行定时任务:
    // 每23小时更新一次超时时间
    new Timer(localIp).schedule(new TimerTask() {
      @Override
      public void run() {
        // 检查缓存中的ip与本机ip是否一致, 一致则更新时间，不一致则重新获取一个机器id
        Boolean b = checkIsLocalIp(String.valueOf(machineId));
        if (b) {
          log.info("IP一致，更新超时时间 ip:{},machineId:{}, time:{}", localIp, machineId, new Date());
          try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
            connect.sync().expire(serviceName + machineId, 60 * 60 * 24);
          }
        } else {
          // IP冲突
          log.info("重新生成机器ID ip:{},machineId:{}, time:{}", localIp, machineId, new Date());
          // 重新生成机器ID，并且更改雪花中的机器ID
          getRandomMachineId();
          // 重新生成并注册机器id
          createMachineId();
          // 更改雪花中的机器ID

          // 结束当前任务
          log.info("Timer->thread->name:{}", Thread.currentThread().getName());
          this.cancel();
        }
      }
    }, 10 * 1000, 1000 * 60 * 60 * 23);

  }

  private static Boolean checkIsLocalIp(String mechineId) {
    try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
      String ip = connect.sync().get(serviceName + mechineId);
      log.info("checkIsLocalIp->ip:{}", ip);
      return localIp.equals(ip);
    }
  }

  /**
   * 获取32-63随机数
   */
  public static void getRandomMachineId() {
    machineId = (int) (Math.random() * 31) + 31;
  }

}