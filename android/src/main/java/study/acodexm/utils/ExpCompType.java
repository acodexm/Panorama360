package study.acodexm.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExpCompType {
    public static String[] items = new String[]{"NO", "GAIN", "GAIN_BLOCKS"};

    public static String get(int position) {
        return items[position].toLowerCase();
    }

    public static int getPosition(String name) {
        return Arrays.stream(items).map(String::toUpperCase).collect(Collectors.toList()).indexOf(name.toUpperCase());
    }
}
