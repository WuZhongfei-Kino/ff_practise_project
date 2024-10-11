package com.wzf.thread;

import com.wzf.domain.DealResultData;
import com.wzf.domain.InDB;
import com.wzf.utils.BatchNumberGenerator;
import com.wzf.utils.JdbcUtil;
import com.wzf.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
//        log.info("当前线程：{}",Thread.currentThread().getName());
        String sql =  "insert into indb(`batch_id`, `from_file`, `content`) value(?, ?, ?)";
        //记录下批次信息
        DealResultData dealResultData = new DealResultData();
//        dealResultData.setBatchNum(batchNumber);
        dealResultData.setDealCount(queue.size());
        dealResultData.setMessage("执行中。。。");
        ThreadLocalUtil.set(dealResultData);
        System.err.println("当前线程：{}"+Thread.currentThread().getName()+"=="+queue.size());
        AtomicInteger dealCount = new AtomicInteger();//处理数量
        AtomicInteger intoDBCount = new AtomicInteger();//入库数量
        //生成这一批次的批次号
        String batchNum = new BatchNumberGenerator().generateNewBatchNumber();
        while (queue.size() > 0) {
//            InDB inDB = queue.take();//无限阻塞等待，直到队列存在数据可删。
            InDB inDB = queue.poll();

        if (!StringUtils.isEmpty(inDB.getContent())) {
//            //添加参数
//            List<String> params = new ArrayList<>();
//            params.add(inDB.getBatchId());
//            params.add(inDB.getFromFile());
//            params.add(inDB.getContent());
            //取出文本所在行数
            Integer lineNum = Integer.valueOf(inDB.getBatchId());
            inDB.setBatchId(batchNum + lineNum);
            dealCount.incrementAndGet();
//            log.info("主线程：{} 总消费个数：{}",Thread.currentThread().getName(), dealCount.get());
            inDB.setBatchId(inDB.getBatchId() + dealCount.get());
//                System.out.println(Thread.currentThread().getName()+"\t 获取队列元素:"+inDB);

                //执行sql
            int resultRow = jdbcUtil.executeUpdate(sql, inDB.getBatchId(), inDB.getFromFile(), inDB.getContent());
            if (resultRow > 0){
                intoDBCount.incrementAndGet();
            }
//            if (ThreadLocalUtil.get() == null){
//                importDBMap.put(Thread.currentThread().getName(), resultRow);
//                ThreadLocalUtil.set(importDBMap);
//            } else {
//                importDBMap = ThreadLocalUtil.get();
//                //执行结果 + 1
////                importDBMap.put(Thread.currentThread().getName(), Integer.valueOf(importDBMap.get(Thread.currentThread().getName())) + resultRow);
//                ThreadLocalUtil.set(importDBMap);
//            }
//                log.info("当前线程：{}",Thread.currentThread().getName()+"\t 处理对象:"+ inDB);

            }
        }
        dealResultData = ThreadLocalUtil.get();
        dealResultData.setDealCount(dealCount.get());
        dealResultData.setIntoDBCount(intoDBCount.get());
        dealResultData.setThreadName(Thread.currentThread().getName());
        dealResultData.setMessage("完成");
        ThreadLocalUtil.set(dealResultData);
        return ThreadLocalUtil.get();
    }
}
