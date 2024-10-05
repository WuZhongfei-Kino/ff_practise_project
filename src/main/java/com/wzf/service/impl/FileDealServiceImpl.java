package com.wzf.service.impl;

import com.wzf.config.TaskToolConfig;
import com.wzf.domain.InDB;
import com.wzf.service.FileDealService;
import com.wzf.thread.FileRead2DBCallable;
import com.wzf.utils.BatchNumberGenerator;
import com.wzf.utils.JdbcUtil;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.concurrent.*;

/**
 * @author WuZhongfei
 * @date 2024年09月30日 14:20
 */
@Service
public class FileDealServiceImpl implements FileDealService {

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public int fileRead2DB() throws ExecutionException, InterruptedException {
        FileRead2DBCallable fileRead2DBCallable = new FileRead2DBCallable(new BatchNumberGenerator(), taskExecutor, new JdbcUtil());
        FutureTask futureTask = new FutureTask<>(fileRead2DBCallable);
        Thread thread = new Thread(futureTask);
        thread.start();
        Object o = futureTask.get();
        return 0;
    }
}
