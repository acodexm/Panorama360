package study.acodexm.settings;

public class GridSize {
    private int LAT;
    private int LON;

    public GridSize(int LAT, int LON) {
        this.LAT = LAT;
        this.LON = LON;
    }

    public int getLAT() {
        return LAT;
    }

    public void setLAT(int LAT) {
        this.LAT = LAT;
    }

    public int getLON() {
        return LON;
    }

    public void setLON(int LON) {
        this.LON = LON;
    }

}
