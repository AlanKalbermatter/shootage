package model;

public class Shot {
    public double x, y;
    public double vx, vy;

    public static final double MIN_SPEED = 5.0;
    public static final double MAX_SPEED = 38.0;
    public static final double GRAVITY = 0.68;
    public static final double DRAG = 0.99;

    public Shot(double startX, double startY, double targetX, double targetY, double speed) {
        this.x = startX;
        this.y = startY;
        double dx = targetX - startX;
        double dy = targetY - startY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 1; // Prevent division by zero
        this.vx = (dx / dist) * speed;
        this.vy = (dy / dist) * speed;
    }

    public void move() {
        vx *= DRAG;
        vy *= DRAG;
        vy += GRAVITY;
        x += vx;
        y += vy;
    }
}