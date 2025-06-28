package model;

import java.awt.*;

public class Particle {
    public float x, y;
    public float vx, vy;
    public float alpha;
    public Color color;
    public int radius;
    public int lifetime; // frames left

    public Particle(float x, float y, float vx, float vy, Color color, int radius, int lifetime) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.radius = radius;
        this.lifetime = lifetime;
        this.alpha = 1.0f;
    }

    public boolean isAlive() {
        return lifetime > 0 && alpha > 0.01f;
    }

    public void update() {
        x += vx;
        y += vy;
        vx *= 0.92f;
        vy *= 0.92f;
        // gravity effect
        vy += 0.12f;
        lifetime--;
        alpha *= 0.93f;
    }

    public void draw(Graphics2D graphics2D) {
        Composite orig = graphics2D.getComposite();
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        graphics2D.setColor(color);
        graphics2D.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2);
        graphics2D.setComposite(orig);
    }
}