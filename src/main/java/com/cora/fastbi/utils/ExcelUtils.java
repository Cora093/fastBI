package com.cora.fastbi.utils;


import com.alibaba.excel.EasyExcel;

import com.cora.fastbi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelUtils {

    /**
     * 将Excel文件转换为CSV格式
     *
     * @param excelFile
     * @return
     */
    public static String convertExcelToCsv(MultipartFile excelFile) {
        // 读取 Excel 文件
        List<Map<Integer, String>> data = readExcelFromMultipartFile(excelFile);
        if (data == null || data.isEmpty()) {
            return "";
        }
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

    private static List<Map<Integer, String>> readExcelFromMultipartFile(MultipartFile excelFile) {
        List<Map<Integer, String>> data;
        try {
            data = EasyExcel.read(excelFile.getInputStream()).headRowNumber(0).sheet().doReadSync();
        } catch (IOException e) {
            log.error("Excel文件处理错误", e);
            throw new BusinessException(10000, "Excel文件处理错误");
        }
        return data;
    }


    public static List<String> getExcelHeader(MultipartFile excelFile) {
        List<Map<Integer, String>> data = readExcelFromMultipartFile(excelFile);
        return new ArrayList<>(data.get(0).values());
    }

    public static List<List<String>> getExcelData(MultipartFile excelFile) {
        List<Map<Integer, String>> data = readExcelFromMultipartFile(excelFile);
        List<List<String>> res = new ArrayList<>();
        for (int i = 1; i < data.size(); i++) {
            res.add(new ArrayList<>(data.get(i).values()));
        }
        return res;
    }

}
