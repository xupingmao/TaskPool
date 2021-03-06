package org.xpm.test;

import org.junit.Assert;
import org.junit.Test;
import org.xpm.taskpool.CreateTaskOption;
import org.xpm.taskpool.Task;
import org.xpm.taskpool.TaskToken;
import org.xpm.taskpool.exception.TaskCommitException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xupingmao on 2017/11/9.
 */
public class LockTest extends AbstractTest {


    static class LockRunnable implements Runnable {

        private String name;
        public LockRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            TaskToken taskToken = null;
            try {
                taskToken = taskPool.lock("Lock", "001");
                if (taskToken != null) {
                    String result = taskToken.getResult() + " " + name;
                    taskToken.setResult(result);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    taskPool.release(taskToken);
                } catch (TaskCommitException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void lockAndRelease() throws Exception {
        Task lock = taskPool.find("Lock", "001");
        if (lock == null) {
            CreateTaskOption option = new CreateTaskOption();
            option.setTaskType("Lock");
            option.setTaskId("001");
            taskPool.put("Lock", null, 10L);
        }

        TaskToken taskToken = null;
        try {
            taskToken = taskPool.lock("Lock", "001");
            taskToken.setResult("");
        } finally {
            taskPool.release(taskToken);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(new LockRunnable("lock1"));
        executorService.submit(new LockRunnable("lock2"));
        executorService.submit(new LockRunnable("lock3"));
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        taskToken = taskPool.lock("Lock", "001");
        Assert.assertTrue(taskToken.getResult().contains("lock1"));
        Assert.assertTrue(taskToken.getResult().contains("lock2"));
        Assert.assertTrue(taskToken.getResult().contains("lock3"));
    }

    @Test
    public void lockTimeout() throws Exception {
        String lockType = "Lock";
        Task lock = taskPool.find("Lock", "timeout");
        if (lock == null) {
            CreateTaskOption option = new CreateTaskOption();
            option.setTaskType("Lock");
            option.setTaskId("timeout");
            option.setTimeoutMillis(10L);
            taskPool.put(option);
        }

        TaskToken timeout = taskPool.lock(lockType, "timeout");

    }

}
