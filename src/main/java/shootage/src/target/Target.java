package shootage.src.target;

import shootage.src.ai.ShotHistory;
import shootage.src.game.Shot;
import shootage.src.genetics.Genome;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Target {
    private int x, y;
    private int radius = 20;
    private Genome genome;
    private double fitness;
    private boolean alive = true;
    private Random rand = new Random();

    // For hit effect
    private boolean recentlyHit = false;
    private long hitTimestamp = 0;

    public Target(int x, int y, Genome genome) {
        this.x = x;
        this.y = y;
        this.genome = genome;
        this.fitness = 0;
    }

    public void reset(int width, int height) {
        this.x = 400 + rand.nextInt(Math.max(1, width - 500));
        this.y = 100 + rand.nextInt(Math.max(1, height - 200));
        this.fitness = 0;
        this.alive = true;
        this.recentlyHit = false;
        this.hitTimestamp = 0;
    }

    public void addFitness(double value) {
        this.fitness += value;
    }

    /**
     * Smarter update: move based on
     * - random movement (genes[0])
     * - avoidance of current shots (genes[1])
     * - avoidance of shot history heatmap (genes[2])
     */
    public void update(List<Shot> currentShots, ShotHistory shotHistory, int panelWidth, int panelHeight) {
        if (!alive) return;
        double[] genes = genome.getGenes();
        double randomScale = genes.length > 0 ? genes[0] : 1.0;
        double avoidanceScale = genes.length > 1 ? genes[1] : 1.0;
        double historyScale = genes.length > 2 ? genes[2] : 1.0;

        // 1. Random movement for unpredictability
        double randomX = (rand.nextDouble() - 0.5) * randomScale * 12;
        double randomY = (rand.nextDouble() - 0.5) * randomScale * 12;

        // 2. Avoid current shots in the air
        double avoidX = 0, avoidY = 0;
        for (Shot s : currentShots) {
            double dx = x - s.x;
            double dy = y - s.y;
            double dist2 = dx * dx + dy * dy;
            if (dist2 < 40000) { // "Threat" zone (200 px radius)
                avoidX += dx / (dist2 + 1);
                avoidY += dy / (dist2 + 1);
            }
        }
        avoidX *= avoidanceScale * 24;
        avoidY *= avoidanceScale * 24;

        // 3. Avoid "hot zones" from previous shots (shot history heatmap)
        int hx = Math.max(0, Math.min(panelWidth - 1, x));
        int hy = Math.max(0, Math.min(panelHeight - 1, y));
        double shotDensity = shotHistory != null ? shotHistory.getDensityAt(hx, hy) : 0.0;
        // The greater the shot density, the more the target is pushed away
        double historyAngle = rand.nextDouble() * 2 * Math.PI;
        double historyX = Math.cos(historyAngle) * historyScale * shotDensity * 10;
        double historyY = Math.sin(historyAngle) * historyScale * shotDensity * 10;

        // Combine all movement vectors
        double moveX = randomX + avoidX + historyX;
        double moveY = randomY + avoidY + historyY;

        x += (int) moveX;
        y += (int) moveY;

        x = Math.max(radius, Math.min(panelWidth - radius, x));
        y = Math.max(radius, Math.min(panelHeight - radius, y));
    }

    public boolean isHit(int shotX, int shotY) {
        int dx = x - shotX;
        int dy = y - shotY;
        return alive && (dx * dx + dy * dy <= radius * radius);
    }

    public void registerHit() {
        recentlyHit = true;
        hitTimestamp = System.currentTimeMillis();
    }

    public void updateHitState() {
        if (recentlyHit && (System.currentTimeMillis() - hitTimestamp > 200)) {
            recentlyHit = false;
        }
    }

    public void die() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public Genome getGenome() {
        return genome;
    }

    public double getFitness() {
        return fitness;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public void draw(Graphics2D g) {
        if (recentlyHit) {
            g.setColor(Color.YELLOW);
        } else if (alive) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.LIGHT_GRAY);
        }
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}