package shootage.src.ai;

import java.util.ArrayList;
import java.util.List;

public class ShotHistory {
    private int[][] heatmap;
    private final int width, height;
    private final List<int[]> shots = new ArrayList<>();


    public ShotHistory(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void recordShot(int x, int y) {
        shots.add(new int[]{x, y});
    }

    // For AI to use: get all previous shots
    public List<int[]> getShots() {
        return shots;
    }
}