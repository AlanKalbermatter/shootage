package shootage.src.projectile;

import java.awt.*;

public class Projectile {
    private int x, y;
    private double dx, dy;
    private final int radius = 6;

    public Projectile(int x, int y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
        g.setColor(Color.ORANGE);
        g.drawOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }
}
