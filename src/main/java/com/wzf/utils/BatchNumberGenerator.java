package com.wzf.utils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String currentTime = sdf.format(new Date());
        String batchNum = fileName == null ? currentTime : fileName.substring(0, fileName.indexOf(".")) + currentTime;
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

    public String generateNewBatchNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String currentTime = sdf.format(new Date());
        String randomString = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 2);
        String batchNum = randomString + currentTime ;

        return batchNum;
    }
}
