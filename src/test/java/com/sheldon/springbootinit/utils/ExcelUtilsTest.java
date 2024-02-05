package com.sheldon.springbootinit.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.sheldon.springbootinit.service.ChartInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ClassName ExcelUtilsTest
 * @Author 26483
 * @Date 2024/2/5 16:02
 * @Version 1.0
 * @Description TODO
 */
class ExcelUtilsTest {
    @Resource
    private ChartInfoService chartInfoService;

    @Test
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_data.xlsx");
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        System.out.println(list);
    }

    public static MultipartFile convert(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), "application/octet-stream", input);
        return multipartFile;
    }

    public static String convert(List<Map<String, Object>> dataList, List<String> keys) {
        String header = String.join(",", keys);

        String result = dataList.stream()
                .map(data -> keys.stream()
                        .map(key -> data.get(key))
                        .map(value -> value != null ? value.toString() : "")
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n", header + "\n", ""));

        return result;
    }

    @Test
    public void doExport() throws IOException {
        File file = ResourceUtils.getFile("classpath:test_data.xlsx");
        MultipartFile multipartFile = convert(file);
        String data = ExcelUtils.excelToCsv(multipartFile);
//        System.out.println(data);
        String[] split = data.split("\n");
        String keys = split[0];
        System.out.println(keys);

        String[] split1 = keys.split(",");
        List<String> list = new ArrayList<>();
        Collections.addAll(list, split1);
        System.out.println(list);
        System.out.println("================================================");

        List<Map<String, Object>> chartInfoById = chartInfoService.getChartInfoById(1L);
        System.out.println(chartInfoById);
        System.out.println("================================================");

        String convert = convert(chartInfoById, list);
        System.out.println(convert);
    }

    @Test
    public void analyseData() {
        List<Map<String, Object>> list = chartInfoService.getChartInfoById(1L);
        Map<String, Object> map = list.get(0);
        System.out.println(map);
    }
}