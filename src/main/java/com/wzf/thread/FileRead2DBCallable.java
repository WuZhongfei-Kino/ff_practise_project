package com.wzf.thread;

import com.wzf.domain.InDB;
import com.wzf.utils.BatchNumberGenerator;
import com.wzf.utils.JdbcUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author WuZhongfei
 * @date 2024年09月30日 10:42
 */
public class FileRead2DBCallable implements Callable {
    /**
     * 1. 创建线程池（容纳线程数根据以下业务要求）
     * 2. 创建并启动一个主线程，按行读取文本文件，并将行内容向ArrayBlockingQueue阻塞队列offer发送元素
     * 3. 创建任务线程（个数大于3），从ArrayBlockingQueue阻塞队列poll元素消费
     * 4. 主线程发送的元素包含需要写入mysql表数据内容、文件名，任务线程拉取到队列消息后，将内容写入mysql（使用jdbc即可）
     * 5. 主线程做到每发送10个元素后，等待任务线程消费处理完成后继续发送，这一批的10个元素处理就是一个批次
     * 6. 主线程实时打印进度：总生产个数和总消费个数（可使用AtomicInteger作为累加），以及mysql表已写入条数
     * 7. 主线程等待当前批次的元素处理完后，打印当前批次每个线程的处理数量（每个线程记录消费个数可使用ThreadLocal持有线程变量）
     * 8. 主线程判断文件内容全部入库后，将文件移动到backup目录
     */

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    private CountDownLatch countDownLatch;

    private String fileName;

    private BatchNumberGenerator batchNumberGenerator;

    private BlockingQueue<InDB> queue;

    private ThreadPoolTaskExecutor threadPoolExecutor;

    private JdbcUtil jdbcUtil;


    public FileRead2DBCallable(BatchNumberGenerator batchNumberGenerator, ThreadPoolTaskExecutor threadPoolExecutor, JdbcUtil jdbcUtil) {
        this.batchNumberGenerator = batchNumberGenerator;
        this.threadPoolExecutor = threadPoolExecutor;
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public Object call() throws Exception {

        try (
            BufferedReader reader = new BufferedReader(new FileReader("text/content.txt"))) {
            System.out.println("文件:"+reader);
            String line;
            InDB inDB = new InDB();
            inDB.setFromFile(fileName);
            Integer lineNum = 1;
            List<InDB> inDBList = new ArrayList<>();
            Integer count = (int) reader.lines().count();
            queue = new ArrayBlockingQueue<>(count);
            countDownLatch = new CountDownLatch(count);
            while ((line = reader.readLine()) != null) {
                System.out.println("读取一行的内容:"+line);
                //生成批次号
                String batchNumber = batchNumberGenerator.generateNewBatchNumber(fileName, lineNum);
                inDB.setBatchId(batchNumber);
                //添加文本内容
                inDB.setContent(line);
                //先判断队列里元素是否有10个
                if (queue.size() < 10){
                    queue.offer(inDB);
                } else {
                    //队列元素满10个，先把队列里的元素全部入库
                  //分配任务线程执行入库操作
                    Object subThread = threadPoolExecutor.submit(new ImportDBCallable(jdbcUtil, queue)).get();
                    System.out.println("子线程执行结果:"+subThread);
                }
                lineNum ++;
                System.out.println(line);
//                    queue.offer()
//                    queue.put(line); // 将行内容放入队列，如果队列满，这里会阻塞
            }
            queue.clear();
            reader.close();
//                queue.put("EOF"); // 文件读取完毕，放入结束标记
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // 重新中断线程
        }


        // 以下代码用于从队列中取出内容并处理，这里简单打印出来
        //            String item;
//            while (!(item = queue.take()).equals("EOF")) { // 当读到EOF标记时结束循环
//                System.out.println(item);
//            }
        //判断队列是否还有值
//            while (queue.)
        return null;

    }
}
