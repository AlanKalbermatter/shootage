package shootage.src.game;

import shootage.src.target.Target;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class GamePanel extends JPanel {
    private final GameEngine engine;
    private Timer timer;
    private boolean aiming = false;
    private int aimX, aimY;

    // Charge bar variables
    private boolean charging = false;
    private long chargeStartTime = 0;
    private float chargePower = 0;
    private static final int CHARGE_BAR_WIDTH = 180;
    private static final int CHARGE_BAR_HEIGHT = 20;
    private static final long CHARGE_DURATION_MS = 1200;

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(GameEngine.FIELD_WIDTH, GameEngine.FIELD_HEIGHT));
        setBackground(Color.WHITE);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    runRound();
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    engine.evolve();
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    engine.startRound();
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && engine.isRunningRound()) {
                    aiming = true;
                    charging = true;
                    aimX = e.getX();
                    aimY = e.getY();
                    chargeStartTime = System.currentTimeMillis();
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (charging && aiming && SwingUtilities.isLeftMouseButton(e) && engine.isRunningRound()) {
                    charging = false;
                    aiming = false;
                    float power = chargePower;
                    chargePower = 0;
                    engine.fireShotAtWithPower(e.getX(), e.getY(), power);
                    repaint();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (aiming && engine.isRunningRound()) {
                    aimX = e.getX();
                    aimY = e.getY();
                    repaint();
                }
            }
        });
    }

    private void runRound() {
        if (timer != null && timer.isRunning()) return;
        engine.startRound();
        timer = new Timer(16, e -> {
            engine.updateShotsAndTargets();
            updateChargeBar();
            repaint();
            if (!engine.isRunningRound()) timer.stop();
        });
        timer.start();
    }

    private void updateChargeBar() {
        if (charging) {
            long elapsed = System.currentTimeMillis() - chargeStartTime;
            chargePower = (elapsed % CHARGE_DURATION_MS) / (float) CHARGE_DURATION_MS;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw moving shots
        g.setColor(Color.BLUE);
        for (Shot s : engine.getActiveShots()) {
            g.fillOval((int) s.x - 4, (int) s.y - 4, 8, 8);
        }

        // Draw shot physics arc if aiming
        if (aiming) {
            g.setColor(new Color(0, 128, 0, 128));
            double shooterX = 0;
            double shooterY = GameEngine.FIELD_HEIGHT;
            double dx = aimX - shooterX;
            double dy = aimY - shooterY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            float power = charging ? chargePower : 1.0f;
            double speed = Shot.MIN_SPEED + (Shot.MAX_SPEED - Shot.MIN_SPEED) * power;
            double vx = (dx / dist) * speed;
            double vy = (dy / dist) * speed;
            double px = shooterX, py = shooterY;
            for (int i = 0; i < 60; i++) { // 60 steps ahead
                vx *= Shot.DRAG;
                vy *= Shot.DRAG;
                vy += Shot.GRAVITY;
                px += vx;
                py += vy;
                g.fillOval((int)px - 2, (int)py - 2, 4, 4);
                if (px < 0 || px > GameEngine.FIELD_WIDTH || py < 0 || py > GameEngine.FIELD_HEIGHT) break;
            }
        }

        // Draw targets
        List<Target> population = engine.getPopulation();
        for (Target target : population) {
            target.draw((Graphics2D) g);
        }

        // Draw shooter
        g.setColor(Color.DARK_GRAY);
        int shooterSize = 24;
        int shooterX = 0;
        int shooterY = GameEngine.FIELD_HEIGHT;
        g.fillRect(shooterX, shooterY - shooterSize, shooterSize, shooterSize);

        // Draw aiming line if aiming
        if (aiming) {
            g.setColor(Color.GREEN);
            g.drawLine(shooterX, shooterY, aimX, aimY);
        }

        // Draw charge bar if charging
        if (charging) {
            int barX = 10, barY = GameEngine.FIELD_HEIGHT - CHARGE_BAR_HEIGHT - 10;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(barX, barY, CHARGE_BAR_WIDTH, CHARGE_BAR_HEIGHT);
            g.setColor(Color.GREEN.darker());
            g.fillRect(barX, barY, (int)(CHARGE_BAR_WIDTH * chargePower), CHARGE_BAR_HEIGHT);
            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, CHARGE_BAR_WIDTH, CHARGE_BAR_HEIGHT);
        }

        // Draw stats
        g.setColor(Color.BLACK);
        g.drawString("Generation: " + engine.getGeneration(), 10, 20);
        g.drawString("Shots Fired: " + engine.getRoundShotsFired() + "/" + GameEngine.SHOTS_PER_ROUND, 10, 35);
        g.drawString("SPACE: Run round | N: Next generation | R: Reset round | Click+Hold+Release: Shoot", 10, 50);
        g.drawString("Hold to charge shot power (bar at bottom left)", 10, 65);
    }
}