package com.zhengyuan.baselib.xmpp.reconnect;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by zy on 2017/10/23.
 * Class for summiting a new runnable task.
 */

public class TaskSubmitter {

    final ExecutorService executorService;//利用Executors 提供的线程池，执行耗时任务：

    public TaskSubmitter(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @SuppressWarnings("unchecked")
    public Future submit(Runnable task) {
        Future result = null;
        if (!executorService.isTerminated()
                && !executorService.isShutdown()
                && task != null) {
            result = executorService.submit(task);
        }
        return result;
    }
}