package study.acodexm.control;

import java.util.Objects;


public class PicturePosition {
    int x;
    int y;

    public PicturePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PicturePosition(String position) {
        setPosition(position);
    }

    public String getPosition() {
        return x + "_" + y;
    }

    public void setPosition(String position) {
        if (position.contains("_")) {
            String[] s = position.split("_");
            this.x = Integer.valueOf(s[0]);
            this.y = Integer.valueOf(s[1]);
        }
    }

    boolean isPositionPossible() {
        return x > -1 && y > -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PicturePosition that = (PicturePosition) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "PicturePosition{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
