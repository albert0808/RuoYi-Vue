package com.albert.learning.logtest;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    /**
     * 触发疯狂写日志
     */
    @GetMapping("/log/test")
    public String testLog() {
        for (int i = 0; i < 5000; i++) { // 写入 5000 条日志
            logger.info("这是第 {} 条测试日志，用于触发日志滚动", i);
            log.info("这是第 {} 条测试日志，用于触发日志滚动", i);
        }
        return "写入完成，请检查 ./logs/test.log";
    }
}
