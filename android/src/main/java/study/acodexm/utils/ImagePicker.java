package study.acodexm.utils;

public class ImagePicker {
    public int maximum(int input[][]) {
        int temp[] = new int[input[0].length];
        MaximumHistogram mh = new MaximumHistogram();
        int maxArea = 0;
        int area = 0;
        for (int[] anInput : input) {
            for (int j = 0; j < temp.length; j++) {
                if (anInput[j] == 0) {
                    temp[j] = 0;
                } else {
                    temp[j] += anInput[j];
                }
            }
            area = mh.maxHistogram(temp);
            if (area > maxArea) {
                maxArea = area;
            }
        }
        return maxArea;
    }

    public static void main(String args[]) {
        int input[][] = {{1, 1, 1, 0},
                {1, 1, 1, 1},
                {0, 1, 1, 0},
                {0, 1, 1, 1},
                {1, 0, 0, 1},
                {1, 1, 1, 1}};
        ImagePicker mrs = new ImagePicker();
        int maxRectangle = mrs.maximum(input);
        //System.out.println("Max rectangle is of size " + maxRectangle);
        assert maxRectangle == 8;
    }
}

