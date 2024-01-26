package com.sheldon.springbootinit.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.sheldon.springbootinit.service.ChartInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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

/**
 * EasyExcel 测试
 *
 * @author <a href="https://github.com/sheldon-3601e">sheldon</a>
 * @from <a href="https://github.com/sheldon-3601e">github</a>
 */
@SpringBootTest
public class EasyExcelTest {

    @Resource
    private ChartInfoService chartInfoService;

    @Test
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:my_test_excel.xlsx");
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

    public static String convert1(List<Map<String, Object>> dataList, List<String> keys) {
        StringJoiner result = new StringJoiner("\n");
        StringJoiner header = new StringJoiner(",");
        for (String key : keys) {
            header.add(key);
        }
        result.add(header.toString());

        for (Map<String, Object> data : dataList) {
            StringJoiner row = new StringJoiner(",");
            for (String key : keys) {
                Object value = data.get(key);
                row.add(value != null ? value.toString() : "");
            }
            result.add(row.toString());
        }
        return result.toString();
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
        File file = ResourceUtils.getFile("classpath:my_test_excel.xlsx");
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