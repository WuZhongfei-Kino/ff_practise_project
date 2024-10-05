package com.wzf.utils;


/**
 * @author WuZhongfei
 * @date 2024年09月30日 10:04
 */
public class BatchNumberGenerator {


    /**
     * 生成一个新的批次号
     * @return 批次号
     */
    public String generateNewBatchNumber(String fileName, int number) {
        String batchNum = fileName == null ? "" : fileName;
        if (number / 1000 > 0){
            batchNum += number / 1000 + number % 1000;
        } else {
            if (number / 100 > 0) {
              batchNum += 0 + number / 100 + number % 100 % 10 ;
            } else {
                if (number / 10 > 0){
                    batchNum += 0+ 0 + number / 10 + number % 10 ;
                } else {
                    batchNum += 0 + 0 + 0 + number % 10 ;
                }
            }
        }
        return batchNum;
    }
}
