package shootage.src.player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Player {
    private int x, y;
    private final int radius = 20;


    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g) {
        // Draw the player as a filled circle
        g.setColor(Color.BLUE);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Optionally, draw a border
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

        // Optionally, display a fixed label or info
        // g.drawString("Player", x - radius, y - radius - 5);
    }

    public void drawAimLine(Graphics2D g, int mouseX, int mouseY) {
        g.setColor(Color.YELLOW);
        g.setStroke(new BasicStroke(1));
        g.drawLine(x, y, mouseX, mouseY);
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
}
