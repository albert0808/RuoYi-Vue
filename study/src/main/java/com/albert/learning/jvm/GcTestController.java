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
    private final List<byte[]> holder = new ArrayList<>();

    @GetMapping("/test")
    public String testGcMetrics(@RequestParam(defaultValue = "alloc") String action,
                                @RequestParam(defaultValue = "100000") int count,
                                @RequestParam(defaultValue = "G1") String gcType) throws Exception {
        long start = System.currentTimeMillis();

        if ("alloc".equals(action)) {
            for (int i = 0; i < count; i++) {
                byte[] data = new byte[1024]; // 1KB对象
            }
        } else if ("retain".equals(action)) {
            for (int i = 0; i < count; i++) {
                holder.add(new byte[1024 * 1024]); // 1MB对象
            }
        } else if ("clear".equals(action)) {
            holder.clear();
        }

        long cost = System.currentTimeMillis() - start;

        // 获取 GC Metrics
        Map<String, Long> gcMetrics = getGcMetrics();

        // 假设 QPS = count / cost(ms) * 1000
        double qps = count / ((double) cost / 1000);

        // 写入 Excel
        saveExcel(gcType, cost, qps,
                gcMetrics.get("gcTotalPause"),
                gcMetrics.get("gcMaxPause"),
                gcMetrics.get("fullGcCount"),
                gcMetrics.get("oldGenUsage"));

        return "Action=" + action + ", cost=" + cost + "ms, QPS=" + qps;
    }

    private Map<String, Long> getGcMetrics() {
        long totalPause = 0;
        long maxPause = 0;
        long fullGcCount = 0;
        long oldGenUsage = 0;

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount() == -1 ? 0 : gc.getCollectionCount();
            long time = gc.getCollectionTime() == -1 ? 0 : gc.getCollectionTime();
            totalPause += time;
            maxPause = Math.max(maxPause, time); // 简单示例
            if (gc.getName().toLowerCase().contains("old") || gc.getName().toLowerCase().contains("full")) {
                fullGcCount += count;
            }
        }

        MemoryUsage oldGen = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        oldGenUsage = oldGen.getUsed() / (1024 * 1024); // MB

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

        int lastRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(lastRow);

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
