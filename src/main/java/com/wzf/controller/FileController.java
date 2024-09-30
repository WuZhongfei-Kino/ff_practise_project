package com.wzf.controller;

import com.wzf.service.FileDealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @author WuZhongfei
 * @date 2024年09月30日 14:17
 */
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileDealService fileDealService;

    @GetMapping("/readFile2DB")
    public void readFile2DB() throws ExecutionException, InterruptedException {
        fileDealService.fileRead2DB();
    }
}
