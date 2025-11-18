package com.albert.learning.jvm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/gc")
public class GcTestController {

    private static final String EXCEL_PATH = "gc-test-result.xlsx";

    @GetMapping("/alloc")
    public String alloc(@RequestParam(defaultValue = "100000") int count) throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            byte[] data = new byte[1024]; // 1KB对象
        }

        long cost = System.currentTimeMillis() - start;
        saveExcel("alloc", count, cost);
        return "OK, cost=" + cost + "ms";
    }


    private final List<byte[]> holder = new ArrayList<>();

    @GetMapping("/retain")
    public String retain(@RequestParam(defaultValue = "1000") int mb) throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < mb; i++) {
            holder.add(new byte[1024 * 1024]); // 每次 1MB
        }

        long cost = System.currentTimeMillis() - start;
        saveExcel("retain", mb, cost);
        return "Holder size=" + holder.size() + "MB, cost=" + cost + "ms";
    }

    @GetMapping("/clear")
    public String clear() throws Exception {
        holder.clear();
        saveExcel("clear", 0, 0);
        return "Holder cleared";
    }


    /** 写入/追加 Excel 结果 */
    private void saveExcel(String action, long param, long cost) throws Exception {
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

        sheet = wb.getNumberOfSheets() == 0 ? wb.createSheet("GC Test") : wb.getSheetAt(0);

        if (sheet.getRow(0) == null) {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Time");
            header.createCell(1).setCellValue("Action");
            header.createCell(2).setCellValue("Param");
            header.createCell(3).setCellValue("Cost(ms)");
            header.createCell(4).setCellValue("JVM Args");
        }

        int lastRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(lastRow);

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String jvmArgs = String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments());

        row.createCell(0).setCellValue(time);
        row.createCell(1).setCellValue(action);
        row.createCell(2).setCellValue(param);
        row.createCell(3).setCellValue(cost);
        row.createCell(4).setCellValue(jvmArgs);

        try (OutputStream os = Files.newOutputStream(Paths.get(EXCEL_PATH))) {
            wb.write(os);
        }
        wb.close();
    }
}

