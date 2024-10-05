package com.wzf.domain;

import lombok.Data;

/**
 * 处理数据结果类
 */
@Data
public class DealResultData {
    //批次号
    private String batchNum;
    //线程名
    private String threadName;
    //处理数量
    private Integer dealCount;
    //信息
    private String message;
    //生产数量
    private Integer produceCount;
    //消费数量
    private Integer comsumeCount;
    //入库数量
    private Integer intoDBCount;

}
