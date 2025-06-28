package model;

import ai.ShotHistory;
import ai.genetics.Genome;
import utils.SoundManager;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.Constants.EXPLOSION_SOUNDS;

public class Target {
    private int x, y;
    private int radius = 30;
    private Genome genome;
    private double fitness;
    private boolean alive = true;
    private Random rand = new Random();

    private boolean recentlyHit = false;
    private long hitTimestamp = 0;

    private float hitFlashAlpha = 0.0f;
    private static final int FLASH_DURATION_MS = 200;

    private List<Particle> particles = new ArrayList<>();

    // memo: per-round memory, resets every round
    // last closest shot x, [1]: last closest shot y, [2]: last dodge x, [3]: last dodge y
    private double[] memo;

    // for penalizing excessive movement
    private double lastMoveDistance = 0.0;

    // animated soldier GIF as target image (update the path as needed)
    private static final Image soldierGif = Toolkit.getDefaultToolkit().getImage(Target.class.getResource("/images/targets/animation.gif"));

    public Target(int x, int y, Genome genome) {
        this.x = x;
        this.y = y;
        this.genome = genome;
        this.fitness = 0;
        this.memo = new double[4];
    }

    public void reset(int width, int height) {
        this.x = 400 + rand.nextInt(Math.max(1, width - 500));
        this.y = 100 + rand.nextInt(Math.max(1, height - 200));
        this.fitness = 0;
        this.alive = true;
        this.recentlyHit = false;
        this.hitTimestamp = 0;
        this.hitFlashAlpha = 0.0f;
        this.memo = new double[4];
        this.lastMoveDistance = 0.0;
        this.particles.clear();
    }

    public void addFitness(double value) {
        this.fitness += value;
    }

    /**
     * Smarter update: move based on
     * - random movement (genes[0]) [should be in 0..1 range for less erratic motion]
     * - avoidance of current shots (genes[1])
     * - avoidance of shot history heatmap (genes[2])
     * - memo: dodge away from last closest shot (short-term memory, resets each round)
     * Penalizes excessive movement.
     */
    public void update(List<Shot> currentShots, ShotHistory shotHistory, int panelWidth, int panelHeight) {
        if (!alive) return;
        double[] genes = genome.getGenes();
        // clamp randomScale to [0, 1] for less erratic dodging
        double randomScale = genes.length > 0 ? Math.max(0, Math.min(1, genes[0])) : 0.5;
        double avoidanceScale = genes.length > 1 ? genes[1] : 1.0;
        double historyScale = genes.length > 2 ? genes[2] : 1.0;
        double memoScale = genes.length > 3 ? genes[3] : 1.0;

        // random movement for unpredictability
        double randomX = (rand.nextDouble() - 0.5) * randomScale * 8;
        double randomY = (rand.nextDouble() - 0.5) * randomScale * 8;

        // avoid current shots in the air
        double avoidX = 0, avoidY = 0;
        double nearestDist2 = Double.MAX_VALUE;
        Shot nearest = null;
        for (Shot s : currentShots) {
            double dx = x - s.x;
            double dy = y - s.y;
            double dist2 = dx * dx + dy * dy;
            if (dist2 < 40000) { // "Threat" zone (200 px radius)
                avoidX += dx / (dist2 + 1);
                avoidY += dy / (dist2 + 1);
            }
            if (dist2 < nearestDist2) {
                nearestDist2 = dist2;
                nearest = s;
            }
        }
        avoidX *= avoidanceScale * 18;
        avoidY *= avoidanceScale * 18;

        // avoid "hot zones" from previous shots (shot history heatmap)
        int hx = Math.max(0, Math.min(panelWidth - 1, x));
        int hy = Math.max(0, Math.min(panelHeight - 1, y));
        double shotDensity = shotHistory != null ? shotHistory.getDensityAt(hx, hy) : 0.0;
        double historyAngle = rand.nextDouble() * 2 * Math.PI;
        double historyX = Math.cos(historyAngle) * historyScale * shotDensity * 8;
        double historyY = Math.sin(historyAngle) * historyScale * shotDensity * 8;

        // memo: remember the nearest shot each frame (short-term adaptation)
        if (nearest != null) {
            memo[0] = nearest.x;
            memo[1] = nearest.y;
        }
        double memoDx = x - memo[0];
        double memoDy = y - memo[1];
        double memoDist = Math.sqrt(memoDx * memoDx + memoDy * memoDy);
        double memoMoveX = 0, memoMoveY = 0;
        if (memoDist > 0 && memoDist < 100) { // only if recent and close
            memoMoveX = (memoDx / memoDist) * memoScale * 6;
            memoMoveY = (memoDy / memoDist) * memoScale * 6;
        }

        // combine all movement vectors
        double moveX = randomX + avoidX + historyX + memoMoveX;
        double moveY = randomY + avoidY + historyY + memoMoveY;

        // clamp maximum movement per frame
        double maxMove = 10.0;
        double moveMag = Math.sqrt(moveX * moveX + moveY * moveY);
        if (moveMag > maxMove) {
            moveX = moveX / moveMag * maxMove;
            moveY = moveY / moveMag * maxMove;
        }

        // save last move distance for fitness penalty
        lastMoveDistance = Math.sqrt(moveX * moveX + moveY * moveY);

        // save last dodge for possible future use
        memo[2] = moveX;
        memo[3] = moveY;

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
        hitFlashAlpha = 1.0f;
    }

    public void updateHitState() {
        if (recentlyHit) {
            long elapsed = System.currentTimeMillis() - hitTimestamp;
            if (elapsed > FLASH_DURATION_MS) {
                recentlyHit = false;
                hitFlashAlpha = 0.0f;
            } else {
                // Animate the hit flash alpha fading out
                hitFlashAlpha = 1.0f - (float) elapsed / FLASH_DURATION_MS;
                if (hitFlashAlpha < 0f) hitFlashAlpha = 0f;
            }
        }
    }

    public void die() {
        alive = false;
        fitness -= 10.0;
        spawnExplosion();
        String sound = EXPLOSION_SOUNDS[rand.nextInt(EXPLOSION_SOUNDS.length)];
        SoundManager.playSoundEffect(sound);
    }

    private void spawnExplosion() {
        int numParticles = 18;
        for (int i = 0; i < numParticles; i++) {
            double angle = 2 * Math.PI * i / numParticles;
            float speed = 2.5f + (float) Math.random() * 2.2f;
            float vx = (float) (Math.cos(angle) * speed);
            float vy = (float) (Math.sin(angle) * speed);
            Color color = Color.YELLOW;
            if (Math.random() < 0.4) color = Color.ORANGE;
            if (Math.random() < 0.2) color = Color.RED;
            int pradius = 5 + (int) (Math.random() * 3);
            int plife = 18 + (int) (Math.random() * 10);
            particles.add(new Particle(x, y, vx, vy, color, pradius, plife));
        }
    }

    public void updateExplosion() {
        particles.removeIf(p -> !p.isAlive());
        for (Particle p : particles) {
            p.update();
        }
    }

    public void drawExplosion(Graphics2D g) {
        for (Particle p : particles) {
            p.draw(g);
        }
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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }

    public double getLastMoveDistance() {
        return lastMoveDistance;
    }

    public void draw(Graphics2D g) {
        // only draw the target if alive
        if (alive) {
            // draw the animated soldier GIF centered at (x, y)
            if (soldierGif != null) {
                int imgW = soldierGif.getWidth(null);
                int imgH = soldierGif.getHeight(null);
                if (imgW > 0 && imgH > 0) {
                    g.drawImage(soldierGif, x - imgW/2, y - imgH/2, null);
                }
            } else {
                g.setColor(Color.RED);
                g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            }

            if (hitFlashAlpha > 0.01f) {
                Composite orig = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hitFlashAlpha));
                g.setColor(Color.YELLOW);
                g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                g.setComposite(orig);
            }

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            g.setFont(new Font("Arial", Font.PLAIN, 10));
            String fitnessStr = String.format("F: %.0f", fitness);
            StringBuilder genomeStr = new StringBuilder("G: [");
            double[] genes = genome.getGenes();
            for (int i = 0; i < genes.length; i++) {
                genomeStr.append(String.format("%.2f", genes[i]));
                if (i < genes.length - 1) genomeStr.append(", ");
            }
            genomeStr.append("]");
            g.setColor(Color.BLACK);
            int overlayX = x - radius;
            int overlayY = y - radius - 6;
            g.drawString(fitnessStr, overlayX, overlayY);
            g.drawString(genomeStr.toString(), overlayX, overlayY - 10);
        }
        drawExplosion(g);
    }
}