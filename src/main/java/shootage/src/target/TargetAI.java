package shootage.src.target;

import shootage.src.ai.ShotHistory;

public class TargetAI {
    private ShotHistory shotHistory;

    public TargetAI(ShotHistory shotHistory) {
        this.shotHistory = shotHistory;
    }

    public void dodge(Target target) {
        // Analyze shotHistory and move target accordingly
    }
}
