package com.wzf.thread;

import com.wzf.domain.InDB;
import com.wzf.utils.BatchNumberGenerator;
import com.wzf.utils.JdbcUtil;
import com.wzf.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author WuZhongfei
 * @date 2024年09月30日 10:43
 */
@Slf4j
public class ImportDBCallable implements Callable {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    private JdbcUtil jdbcUtil;

    BlockingQueue<InDB> queue;

    public ImportDBCallable(JdbcUtil jdbcUtil, BlockingQueue<InDB> queue) {
        this.jdbcUtil = jdbcUtil;
        this.queue = queue;
    }

    @Override
    public Object call() throws Exception {
        log.info("当前线程：{}",Thread.currentThread().getName());
        Map<String, Object> resultMap = new HashMap<>();
        String sql =  "insert into indb(`batch_id, `from_file`, `content`) value(?, ?, ?)";
        //记录入库数
        Map<String, Integer> importDBMap = new HashMap<>();
        InDB inDB = queue.take();//无限阻塞等待，直到队列存在数据可删。
        if (inDB == null) {
            //添加参数
            List<String> params = new ArrayList<>();
            params.add(inDB.getBatchId());
            params.add(inDB.getFromFile());
            params.add(inDB.getContent());
            //执行sql
            int resultRow = jdbcUtil.insertSql(sql, params);
            if (ThreadLocalUtil.get() == null){
                importDBMap.put(Thread.currentThread().getName(), resultRow);
                ThreadLocalUtil.set(importDBMap);
            } else {
                importDBMap = ThreadLocalUtil.get();
                //执行结果 + 1
                importDBMap.put(Thread.currentThread().getName(), Integer.valueOf(importDBMap.get(Thread.currentThread().getName())) + resultRow);
                ThreadLocalUtil.set(importDBMap);
            }
            log.info("当前线程：{}",Thread.currentThread().getName()+"\t 处理对象:"+ inDB);
        }
        resultMap.put(Thread.currentThread().getName(), ThreadLocalUtil.get());
        return resultMap;
    }
}
