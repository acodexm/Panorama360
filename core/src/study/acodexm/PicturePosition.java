package study.acodexm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PicturePosition {
    private static final String TAG = PicturePosition.class.getSimpleName();
    private static PicturePosition thisPosition;


    private List<Integer> usedPositions = new ArrayList<>();
    private int lastX;
    private int LON;
    private int LAT;
    private int lastY;
    private int currX;
    private int currY;

    //test only
    public void setGrid(int[][] grid) {
        this.grid = grid;
    }

    public void markAsUsed(List<Integer> used) {
        usedPositions.addAll(used);
    }

    public void markAsUnused(List<Integer> used) {
        usedPositions.removeAll(used);
    }

    public List<Integer> getUsedPositions() {
        return usedPositions;
    }

    private int[][] grid;

    public int getLON() {
        return LON;
    }

    public int getLAT() {
        return LAT;
    }

    private PicturePosition(int LAT, int LON) {
        this.LAT = LAT;
        this.LON = LON;
        this.grid = new int[LON][LAT];
    }

    public static PicturePosition getInstance(int LAT, int LON, boolean forceRecreate) {
        if (thisPosition == null || forceRecreate)
            thisPosition = new PicturePosition(LAT, LON);
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
