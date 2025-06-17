package shootage.src.target;

import shootage.src.ai.ShotHistory;
import shootage.src.genetics.Genome;

import java.awt.*;
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

    public void update(ShotHistory shotHistory, int panelWidth, int panelHeight) {
        double[] genes = genome.getGenes();
        double moveX = (rand.nextDouble() - 0.5) * genes[0] * 12;
        double moveY = (rand.nextDouble() - 0.5) * genes[1] * 12;

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