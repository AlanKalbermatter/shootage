package ai;

public class ShotHistory {
    private final int width, height;
    private final int[][] heatmap;

    public ShotHistory(int width, int height) {
        this.width = width;
        this.height = height;
        this.heatmap = new int[width][height];
    }

    /**
     * Returns the local density (sum in a 20x20 region) around (x, y)
     */
    public double getDensityAt(int x, int y) {
        int sum = 0, count = 0;
        for (int dx = -10; dx <= 10; dx++) {
            for (int dy = -10; dy <= 10; dy++) {
                int xx = x + dx, yy = y + dy;
                if (xx >= 0 && xx < width && yy >= 0 && yy < height) {
                    sum += heatmap[xx][yy];
                    count++;
                }
            }
        }
        return count > 0 ? (double) sum / count : 0.0;
    }
}