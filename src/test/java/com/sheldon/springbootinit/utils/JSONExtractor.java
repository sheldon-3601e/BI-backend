package com.sheldon.springbootinit.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONExtractor {

    public static String extractValidJSON(String input) {
        String regex = "\\{.*?\\}";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        String input = "啊实打实大苏打asdasdaaasdas\n" +
                "{\n" +
                "  \"xAxis\": {\n" +
                "    \"type\": \"category\",\n" +
                "    \"data\": [\"1号\", \"2号\", \"3号\", \"4号\", \"5号\", \"6号\", \"7号\", \"8号\"]\n" +
                "  },\n" +
                "  \"yAxis\": {\n" +
                "    \"type\": \"value\"\n" +
                "  },\n" +
                "  \"series\": [\n" +
                "    {\n" +
                "      \"data\": [10, 20, 30, 20, 2, 32, 20, 10],\n" +
                "      \"type\": \"bar\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String validJSON = extractValidJSON(input);
        System.out.println(validJSON);
    }
}
