package com.wzf.domain;

import lombok.Data;

/**
 * @author WuZhongfei
 * @date 2024年09月29日 17:02
 */

@Data
public class InDB {
    private Long id;
    private String  batchId;
    private String fromFile;
    private String content;
}
