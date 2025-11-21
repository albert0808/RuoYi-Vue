package com.albert.learning.jvm;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // -----------------------------
    // 正常业务接口
    // -----------------------------
    @PostMapping("/create")
    public String createOrder(@RequestParam(defaultValue = "1") int count) {
        return service.createOrders(count);
    }

    // -----------------------------
    // Heap OOM
    // -----------------------------
    @GetMapping("/test/heap-oom")
    public String heapOom() {
        return service.heapOom();
    }

    // -----------------------------
    // Full GC频繁 / 老年代压力
    // -----------------------------
    @GetMapping("/test/gc-freq")
    public String frequentGc() {
        return service.frequentFullGc();
    }

    // -----------------------------
    // StackOverflow
    // -----------------------------
    @GetMapping("/test/stack-overflow")
    public String stackOverflow() {
        return service.stackOverflow();
    }

    // -----------------------------
    // Metaspace OOM
    // -----------------------------
    @GetMapping("/test/metaspace-oom")
    public String metaspaceOom() throws Exception {
        return service.metaSpaceOom();
    }

    // -----------------------------
    // Direct Memory OOM
    // -----------------------------
    @GetMapping("/test/direct-oom")
    public String directOom() {
        return service.directOom();
    }
}

