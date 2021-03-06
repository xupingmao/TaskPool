package org.xpm.taskpool;

import org.xpm.taskpool.exception.TaskCommitException;
import org.xpm.taskpool.exception.TaskRuntimeException;
import org.xpm.taskpool.exception.TaskUpdateException;

/**
 * Created by xupingmao on 2017/10/30.
 */
public interface TaskPool {

    /**
     * 根据条件添加任务 返回taskId
     * @param createOption
     */
    Task put(CreateTaskOption createOption) throws Exception;

    /**
     * 添加任务 返回taskId
     * @param taskType
     * @param params 任务参数
     * @param timeoutMillis 任务超时时间
     */
    Task put(String taskType, String params, long timeoutMillis) throws Exception;

    /**
     * 获取任务，生成一个唯一的token
     * 这是一个非阻塞的接口
     * @param taskType
     * @return
     */
    TaskToken tryGet(String taskType);

    /**
     * 获取任务，阻塞式接口，使用轮询的策略，轮询时间参考系统配置
     * @param taskType
     * @return
     */
    TaskToken get(String taskType) throws InterruptedException;

    /**
     * 获取分布式锁,非阻塞方式
     * @param lockType
     * @param id
     * @return
     */
    TaskToken tryLock(String lockType, String id);

    /**
     * 获取分布式锁,阻塞方式
     * @param lockType
     * @param id
     * @return
     */
    TaskToken lock(String lockType, String id) throws InterruptedException;

    /**
     * 释放任务，但是不重置状态
     * @param token
     * @return
     * @throws TaskCommitException
     */
    void release(TaskToken token) throws TaskCommitException;

    /**
     * 更新任务执行进度
     * @param token
     * @throws TaskUpdateException
     */
    void update(TaskToken token) throws TaskUpdateException;

    /**
     * 提交任务，这里会帮助检查任务是否超时，是否被抢占，如果失败抛出异常
     * @param task
     * @throws TaskCommitException
     */
    void commit(TaskToken task) throws TaskCommitException;

    /**
     * 取消任务
     * @param id
     */
    boolean cancel(Long id);

    /**
     * 查找任务
     * @param taskType
     * @param taskId
     * @return
     */
    Task find(String taskType, String taskId);

    /**
     * 通过主键ID查询任务
     * @param id
     * @return
     */
    Task find(Long id);

    /**
     * 关闭任务池
     */
    void close() throws InterruptedException;
}
