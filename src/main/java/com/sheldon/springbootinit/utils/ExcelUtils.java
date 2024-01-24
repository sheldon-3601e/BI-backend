package com.sheldon.springbootinit.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName Excel 相关的工具类
 * @Author 26483
 * @Date 2024/1/18 16:50
 * @Version 1.0
 * @Description TODO
 */
@Slf4j
public class ExcelUtils {

    public static String excelToCsv(MultipartFile multipartFile) {

        // 转化为 Csv文件
        StringBuilder stringBuilder = new StringBuilder();

        // 读取数据
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();

            // 读取表头
            LinkedHashMap<Integer, String> headerMap = (LinkedHashMap<Integer, String>) list.get(0);

            /*
            headerMap.values().stream().filter（）
              1. headerMap.values() 获取所有的 value
              2. stream() 转化为流
              3. filter(ObjectUtils::isNotEmpty) 过滤掉空的 value
              4. collect(Collectors.toList()) 转化为 List
             */
            List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            String header = StrUtil.join(",", headerList);
            stringBuilder.append(header).append("\n");

            // 读取数据
            for (int i = 1; i < list.size(); i++) {
                LinkedHashMap<Integer, String> dataMap = (LinkedHashMap<Integer, String>) list.get(i);
                List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
                String data = StrUtil.join(",", dataList);
                // System.out.println(data);
                stringBuilder.append(data).append("\n");
            }

        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
        }

        return stringBuilder.toString();
    }

}
