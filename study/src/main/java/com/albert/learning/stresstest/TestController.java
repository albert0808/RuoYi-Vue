package com.albert.learning.stresstest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test/empty")
    public String emptyEndpoint() {

        long start = System.currentTimeMillis();

        // ---- 这里什么业务也不做，纯空接口 ----

        long cost = System.currentTimeMillis() - start;
        log.info("【/test/empty】接口耗时：{} ms", cost);

        return "ok";
    }

    /**
     * 可调控耗时的压测接口
     * 示例：/test/sleep?ms=200
     */
    @GetMapping("/test/sleep")
    public String sleep(@RequestParam(defaultValue = "0") long ms) {

        long start = System.currentTimeMillis();

        try {
            if (ms > 0) {
                Thread.sleep(ms);
            }
        } catch (InterruptedException e) {
            log.error("sleep 被中断", e);
        }

        long cost = System.currentTimeMillis() - start;
        log.info("【/test/sleep】指定耗时={}ms，实际耗时={}ms", ms, cost);

        return "ok";
    }
}
