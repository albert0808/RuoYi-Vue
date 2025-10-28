package com.ruoyi.web.controller.test;

import com.ruoyi.common.core.controller.BaseController;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController extends BaseController {

    // 用 static 保证对象不会被方法退出时释放
    private static final List<byte[]> ygcList = new ArrayList<>();
    private static final List<byte[]> fgcList = new ArrayList<>();
    private static final Map<String, Object> leakMap = new HashMap<>();

    /**
     * 场景 1: 频繁 Young GC
     * 请求示例: http://localhost:8080/test/ygc
     */
    @GetMapping("/ygc")
    public String triggerYGC() {
        // 每次加 50KB 对象到 ygcList，但会清空一部分，促使 Eden 区频繁 GC
        for (int i = 0; i < 3000; i++) {
            ygcList.add(new byte[1024 * 50]); // 50KB
        }
        if (ygcList.size() > 5000) {
            ygcList.clear(); // 释放引用，让 GC 处理
        }
        return "触发频繁 Young GC 模拟";
    }

    /**
     * 场景 2: 频繁 Full GC（老年代满）
     * 请求示例: http://localhost:8080/test/fgc
     */
    @GetMapping("/fgc")
    public String triggerFGC() {
        // 不清空集合，数据一直存放在老年代
        for (int i = 0; i < 500; i++) {
            fgcList.add(new byte[1024 * 1024]); // 1MB
        }
        return "触发频繁 Full GC 模拟";
    }

    /**
     * 场景 3: 内存泄漏模拟（对象无法回收）
     * 请求示例: http://localhost:8080/test/leak
     */
    @GetMapping("/leak")
    public String triggerLeak() {
        int start = leakMap.size();
        for (int i = 0; i < 500; i++) {
            leakMap.put("key-" + (start + i), new byte[1024 * 1024]); // 512KB
        }
        return "触发内存泄漏模拟，当前大小：" + leakMap.size();
    }

    /**
     * 清空数据
     * 请求示例: http://localhost:8080/test/clear
     */
    @GetMapping("/clear")
    public String clearAll() {
        ygcList.clear();
        fgcList.clear();
        leakMap.clear();
        System.gc(); // 主动提示 GC
        return "已清空所有数据";
    }

    // 空接口，压测用
    @GetMapping("/empty")
    public String empty() {
        return "ok"; // 简单返回，避免null
    }
}
