package study.acodexm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static study.acodexm.AndroidCamera.LAT;
import static study.acodexm.AndroidCamera.LON;

public class PicturePosition {
    private static final String TAG = PicturePosition.class.getSimpleName();
    private static PicturePosition thisPosition;
    private int lastX;
    private int lastY;
    private int currX;
    private int currY;
    private int[][] grid;

    private PicturePosition() {
        this.grid = new int[LON][LAT];
    }

    public static PicturePosition getInstance() {
        if (thisPosition == null)
            thisPosition = new PicturePosition();
        return thisPosition;
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

    public int[][] getGrid() {
        return grid;
    }

    public String getPosition() {
        return lastX + "_" + lastY;
    }

    public int calculateLastPosition() {
        int pos = lastX * LAT + lastY + lastX;
        return pos < 0 ? -1 : pos;
    }

    public int calculateCurrentPosition() {
        int pos = currX * LAT + currY + currX;
        return pos < 0 ? -1 : pos;
    }

    public int calculatePosition(int x, int y) {
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
            if (this.grid[lastX][lastY] == 0)
                this.grid[lastX][lastY] = 1;
        }
    }

    public void saveCurrentPosition() {
        setLastPosition(this.currX, this.currY);
    }

    public void setCurrentPosition(int currX, int currY) {
        this.currX = currX;
        this.currY = currY;
    }

    boolean isCurrentPositionPossible() {
        return grid[currX][currY] == 0;
    }

    @Override
    public String toString() {
        return "PicturePosition{" +
                "lastX=" + lastX +
                ", lastY=" + lastY +
                ", currX=" + currX +
                ", currY=" + currY +
                ", grid=" + Arrays.deepToString(grid) +
                '}';
    }
}
