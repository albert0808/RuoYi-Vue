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

    /**
     * 可控耗时 +可控返回内容长度的压测接口
     * 示例：/test/custom?ms=100&len=1024
     *
     * @param ms  模拟耗时（毫秒）
     * @param len 返回内容长度（字节数）
     */
    @GetMapping("/test/custom")
    public String custom(
            @RequestParam(defaultValue = "0") long ms,
            @RequestParam(defaultValue = "0") int len) {

        long start = System.currentTimeMillis();

        // 1. 控制耗时
        try {
            if (ms > 0) {
                Thread.sleep(ms);
            }
        } catch (InterruptedException e) {
            log.error("sleep interrupted", e);
        }

        // 2. 构造指定长度的返回内容
        // 使用 char 填充字符串（1 char=1字节，适合 ASCII）
        StringBuilder sb = new StringBuilder();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                sb.append('A');  // 你也可以换成随机字符
            }
        }

        long cost = System.currentTimeMillis() - start;
        log.info("【/test/custom】指定耗时={}ms, 实际耗时={}ms, 返回长度={}字节",
                ms, cost, len);

        return sb.toString();
    }
}
