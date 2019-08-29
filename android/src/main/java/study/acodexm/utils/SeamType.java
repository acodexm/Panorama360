package study.acodexm.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SeamType {
    public static String[] items = new String[]{"no", "voronoi", "gc_color", "gc_colorgrad", "dp_color", "dp_colorgrad"};

    public static String get(int position) {
        return items[position].toLowerCase();
    }

    public static int getPosition(String name) {
        return Arrays.stream(items).map(String::toUpperCase).collect(Collectors.toList()).indexOf(name.toUpperCase());
    }
}
