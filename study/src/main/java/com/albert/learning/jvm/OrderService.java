package com.albert.learning.jvm;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

@Service
public class OrderService {

    private final List<Map<String, Object>> orderStore = new ArrayList<>();
    private final Random random = new Random();

    // -----------------------------
    // 正常订单业务
    // -----------------------------
    public String createOrders(int count) {
        for (int i = 0; i < count; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", UUID.randomUUID().toString());
            order.put("amount", random.nextDouble() * 1000);
            order.put("time", System.currentTimeMillis());
            orderStore.add(order);
        }
        return "Created " + count + " orders, total stored: " + orderStore.size();
    }

    // -----------------------------
    // Heap OOM
    // -----------------------------
    public String heapOom() {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 10_000_000; i++) {
            list.add(new byte[1024]); // 约 10GB
        }
        return "done";
    }

    // -----------------------------
    // Full GC频繁 / 老年代压力
    // -----------------------------
    public String frequentFullGc() {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            byte[] data = new byte[1024 * 50]; // 大对象直接进入老年代
            list.add(data);
            if (i % 100 == 0) list.clear();
        }
        return "done";
    }

    // -----------------------------
    // StackOverflow
    // -----------------------------
    public String stackOverflow() {
        return recursive();
    }

    private String recursive() {
        return recursive();
    }

    // -----------------------------
    // Metaspace OOM
    // -----------------------------
    public String metaSpaceOom() throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            ClassLoader loader = new ClassLoader() {};
            Class<?> c = loader.loadClass("java.lang.String"); // 模拟生成类
            classes.add(c);
        }
        return "done";
    }

    // -----------------------------
    // Direct Memory OOM
    // -----------------------------
    public String directOom() {
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            buffers.add(ByteBuffer.allocateDirect(1024 * 1024)); // 1MB
        }
        return "done";
    }
}

