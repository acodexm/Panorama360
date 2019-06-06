package study.acodexm.utils;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import study.acodexm.PicturePosition;

public class MaximumHistogram {
    public static Set<Integer> maxArea(PicturePosition position) {
        int[][] grid = extendGrid(position.getGrid());
        int[] temp = new int[grid[0].length];
        MaximumHistogram mh = new MaximumHistogram();
        int maxArea = 0;
        int area;
        Set<Integer> pictures = new HashSet<>();
        Set<Integer> tempPictures = new HashSet<>();
        for (int x = 0; x < grid.length; x++) {
            gridExtractor(position, grid, temp, tempPictures, x);
            area = mh.maxHistogram(temp);
            if (area > maxArea) {
                maxArea = area;
                pictures = new HashSet<>(tempPictures);
            }
        }
        return pictures;
    }

    private static void removeUnusedAreas(int[][] grid, Set<Integer> tempPictures, Set<Integer> points, PicturePosition position) {
        for (int x = 0; x < Math.round(grid.length / 2); x++) {
            for (int y = 0; y < grid[0].length; y++) {
                if (points.contains(y))
                    continue;
                tempPictures.remove(position.calculatePosition(x, y));
            }
        }
    }

    public static Set<Integer> maxLength(PicturePosition position) {
        int[][] grid = extendGrid(position.getGrid());
        int[] temp = new int[grid[0].length];
        Set<Integer> pictures = new HashSet<>();
        Set<Integer> tempPictures = new HashSet<>();
        int max = 0;
        for (int x = 0; x < grid.length; x++) {
            gridExtractor(position, grid, temp, tempPictures, x);
            for (int n : temp) {
                if (max < n) {
                    max = n;
                    pictures = new HashSet<>(tempPictures);
                }
            }
        }
        return pictures;
    }

    private static void gridExtractor(PicturePosition position, int[][] grid, int[] temp, Set<Integer> tempPictures, int x) {
        for (int y = 0; y < grid[0].length; y++) {
            int offset = Math.round(grid.length / 2);
            if (x >= offset) {// edge check
                int xo = x - offset;
                processGrid(position, grid, temp, tempPictures, xo, y, offset);
            } else {
                processGrid(position, grid, temp, tempPictures, x, y, offset);
            }
        }
    }

    private static void processGrid(PicturePosition position, int[][] grid, int[] temp, Set<Integer> tempPictures, int x, int y, int offset) {
        int newItem = position.calculatePosition(x, y);
        if (grid[x][y] == 0) {
            temp[y] = 0;
            for (int rmX = 0; rmX < offset; rmX++)
                tempPictures.remove(position.calculatePosition(rmX, y));
        } else if (// pictures taken closer to middle of grid has higher weight value
                y == Math.round(grid[0].length / 2) &&
                        !tempPictures.contains(newItem)
        ) {
            temp[y] += 2 * grid[x][y];
            tempPictures.add(newItem);
        } else if (!tempPictures.contains(newItem)) {
            temp[y] += grid[x][y];
            tempPictures.add(newItem);
        }
    }

    /**
     * Extend main grid for edge cases
     *
     * @param grid
     * @return
     */
    private static int[][] extendGrid(int[][] grid) {
        int[][] extendedGrid = new int[2 * grid.length][];
        System.arraycopy(grid, 0, extendedGrid, 0, grid.length);
        System.arraycopy(grid, 0, extendedGrid, grid.length, grid.length);
        return extendedGrid;
    }


    public int maxHistogram(int[] input) {
        Deque<Integer> stack = new LinkedList<>();
        int maxArea = 0;
        int area = 0;
        int i;
        for (i = 0; i < input.length; ) {
            if (stack.isEmpty() || input[stack.peekFirst()] <= input[i]) {
                stack.offerFirst(i++);
            } else {
                maxArea = getMaxArea(input, stack, maxArea, i, area);
            }
        }
        while (!stack.isEmpty()) {
            maxArea = getMaxArea(input, stack, maxArea, i, area);
        }
        return maxArea;
    }

    private int getMaxArea(int[] input, Deque<Integer> stack, int maxArea, int i, int area) {
        int top = stack.pollFirst();
        //if stack is empty means everything till i has to be
        //greater or equal to input[top] so get area by
        //input[top] * i;
        if (stack.isEmpty()) {
            area = input[top] * i;
        }
        //if stack is not empty then everything from i-1 to input.peek() + 1
        //has to be greater or equal to input[top]
        //so area = input[top]*(i - stack.peek() - 1);
        else {
            area = input[top] * (i - stack.peekFirst() - 1);
        }
        if (area > maxArea) {
            maxArea = area;
        }
        return maxArea;
    }


}