package com.wzf.service;

import java.util.concurrent.ExecutionException;

/**
 * @author WuZhongfei
 * @date 2024年09月29日 18:31
 */
public interface FileDealService {
    int fileRead2DB() throws ExecutionException, InterruptedException;

}
