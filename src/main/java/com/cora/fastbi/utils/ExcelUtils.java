package com.cora.fastbi.utils;


import com.alibaba.excel.EasyExcel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public class ExcelUtils {

    public static String convertExcelToCsv(MultipartFile excelFile) throws IOException {
        // 读取 Excel 文件
        InputStream inputStream = excelFile.getInputStream();
        List<Map<Integer, String>> data = EasyExcel.read(inputStream).headRowNumber(0).sheet().doReadSync();
        // 将数据转换为 CSV 格式的字符串
        StringBuilder csvString = new StringBuilder();
        for (Map<Integer, String> row : data) {
            row.values().forEach(v -> csvString.append(v).append(","));
            // 每行结束时去掉最后一个逗号
            csvString.deleteCharAt(csvString.length() - 1);
            csvString.append("\n");
        }
        return csvString.toString();
    }
}
