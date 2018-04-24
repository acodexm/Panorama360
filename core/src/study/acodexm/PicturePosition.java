package study.acodexm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static study.acodexm.AndroidCamera.LAT;
import static study.acodexm.AndroidCamera.LON;

public class PicturePosition {
    private static PicturePosition thisPosition;
    private int lastX;
    private int lastY;
    private int[][] grid;

    private PicturePosition() {
        this.grid = new int[LON][LAT];
    }

    public static PicturePosition getInstance() {
        if (thisPosition == null)
            thisPosition = new PicturePosition();
        return thisPosition;
    }

    public int getLastX() {
        return lastX;
    }

    public List<Integer> getTakenPictures() {
        List<Integer> pictures = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 1) pictures.add(calculatePosition(i, j));
            }
        }
        return pictures;
    }

    public int getLastY() {
        return lastY;
    }


    public int[][] getGrid() {
        return grid;
    }


    public String getPosition() {
        return lastX + "_" + lastY;
    }

    public int calculatePosition() {
        int pos = lastX * LAT + lastY + lastX;
        return pos < 0 ? -1 : pos;
    }

    private int calculatePosition(int x, int y) {
        int pos = x * LAT + y + x;
        return pos < 0 ? -1 : pos;
    }

    public void setLastPosition(int lastX, int lastY) {
        if (grid[lastX][lastY] == 0) {
            this.lastX = lastX;
            this.lastY = lastY;
            this.grid[lastX][lastY] = 1;
        }
    }

    public void setLastPosition(String position) {
        if (position.contains("_")) {
            String[] s = position.split("_");
            this.lastX = Integer.valueOf(s[0]);
            this.lastY = Integer.valueOf(s[1]);
            if (isPositionPossible())
                this.grid[lastX][lastY] = 1;
        }
    }

    boolean isPositionPossible() {
        return grid[lastX][lastY] == 0;
    }

    @Override
    public String toString() {
        return "PicturePosition{" +
                "x=" + lastX +
                ", y=" + lastY +
                ", grid=" + Arrays.deepToString(grid) +
                '}';
    }
}
