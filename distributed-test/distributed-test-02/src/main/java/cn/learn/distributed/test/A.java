package cn.learn.distributed.test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shaoyijiong
 * @date 2021/3/19
 */
public class A {

  public static void main(String[] args) throws InterruptedException {
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    Thread thread = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        System.out.println(Thread.currentThread().isInterrupted());
        LockSupport.park();
        System.out.println(Thread.currentThread().isInterrupted());
        System.out.println("中断结束");

      }
    });
    thread.start();

    Thread.sleep(1000);

    thread.interrupt();
  }

}
