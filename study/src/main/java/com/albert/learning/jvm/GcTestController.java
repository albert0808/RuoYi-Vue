package com.albert.learning.jvm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.management.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/gc")
public class GcTestController {

    private static final String EXCEL_PATH = "gc-metrics.xlsx";

    @GetMapping("/test")
    public String testGc(@RequestParam(defaultValue = "100") int loops,
                         @RequestParam(defaultValue = "5000") int batch,
                         @RequestParam(defaultValue = "G1") String gcType) throws Exception {

        /**
         * loops：循环次数（一次循环分配一定量的对象）
         * batch：每次循环分配对象个数
         *
         * 每次测试总分配对象 = loops * batch
         */

        long start = System.currentTimeMillis();

        Random r = new Random();

        for (int i = 0; i < loops; i++) {

            // 临时对象列表 —— 循环结束自动释放
            List<byte[]> tmp = new ArrayList<>();

            for (int j = 0; j < batch; j++) {
                // 100byte ~ 500byte 随机
                tmp.add(new byte[100 + r.nextInt(400)]);
            }

            // 释放引用，让 GC 有机会回收
            tmp.clear();
        }

        long cost = System.currentTimeMillis() - start;

        // 获取 GC 指标
        Map<String, Long> gc = getGcMetrics();

        // QPS 估算
        long totalOps = (long) loops * batch;
        double qps = totalOps / ((double) cost / 1000);

        saveExcel(gcType, cost, qps,
                gc.get("gcTotalPause"),
                gc.get("gcMaxPause"),
                gc.get("fullGcCount"),
                gc.get("oldGenUsage"));

        return "GC=" + gcType + ", cost=" + cost + "ms, QPS=" + qps;
    }

    private Map<String, Long> getGcMetrics() {
        long totalPause = 0;
        long maxPause = 0;
        long fullGcCount = 0;
        long oldGenUsage;

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = Math.max(0, gc.getCollectionCount());
            long time = Math.max(0, gc.getCollectionTime());
            totalPause += time;
            maxPause = Math.max(maxPause, time);

            String name = gc.getName().toLowerCase();
            if (name.contains("old") || name.contains("mark") || name.contains("full")) {
                fullGcCount += count;
            }
        }

        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        oldGenUsage = heap.getUsed() / (1024 * 1024);

        Map<String, Long> map = new HashMap<>();
        map.put("gcTotalPause", totalPause);
        map.put("gcMaxPause", maxPause);
        map.put("fullGcCount", fullGcCount);
        map.put("oldGenUsage", oldGenUsage);
        return map;
    }

    private void saveExcel(String gcType, long avgRT, double qps,
                           long gcTotalPause, long maxPause,
                           long fullGcCount, long oldGenUsage) throws Exception {

        Workbook wb;
        Sheet sheet;
        File file = new File(EXCEL_PATH);

        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                wb = new XSSFWorkbook(is);
            }
        } else {
            wb = new XSSFWorkbook();
        }

        sheet = wb.getNumberOfSheets() == 0 ? wb.createSheet("GC Metrics") : wb.getSheetAt(0);

        if (sheet.getRow(0) == null) {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Time");
            header.createCell(1).setCellValue("GC Type");
            header.createCell(2).setCellValue("Avg RT(ms)");
            header.createCell(3).setCellValue("QPS");
            header.createCell(4).setCellValue("GC Total Pause(ms)");
            header.createCell(5).setCellValue("Max Pause(ms)");
            header.createCell(6).setCellValue("Full GC Count");
            header.createCell(7).setCellValue("Old Gen Usage(MB)");
        }

        int last = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(last);

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        row.createCell(0).setCellValue(time);
        row.createCell(1).setCellValue(gcType);
        row.createCell(2).setCellValue(avgRT);
        row.createCell(3).setCellValue(qps);
        row.createCell(4).setCellValue(gcTotalPause);
        row.createCell(5).setCellValue(maxPause);
        row.createCell(6).setCellValue(fullGcCount);
        row.createCell(7).setCellValue(oldGenUsage);

        try (OutputStream os = Files.newOutputStream(Paths.get(EXCEL_PATH))) {
            wb.write(os);
        }
        wb.close();
    }
}
