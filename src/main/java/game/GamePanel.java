package game;

import model.Shot;
import model.Target;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class GamePanel extends JPanel {
    private static final int CHARGE_BAR_WIDTH = 180;
    private static final int CHARGE_BAR_HEIGHT = 20;
    private static final long CHARGE_DURATION_MS = 2400;
    private static final int MUZZLE_FLASH_DURATION_MS = 80;

    private final GameEngine engine;
    private Timer timer;
    private Timer uiTimer;

    private boolean aiming = false;
    private int aimX, aimY;
    private boolean charging = false;
    private long chargeStartTime = 0;
    private float chargePower = 0;
    private boolean showMuzzleFlash = false;
    private long muzzleFlashStartTime = 0;

    private static final Image backgroundImage = Toolkit.getDefaultToolkit().getImage(GamePanel.class.getResource("/images/background/marsmid.png"));
    private static final Image shotgunImage = Toolkit.getDefaultToolkit().getImage(GamePanel.class.getResource("/images/shooter/shotgun.png"));
    private static final Image muzzleFlashImage = Toolkit.getDefaultToolkit().getImage(GamePanel.class.getResource("/images/shooter/effect.png"));

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(GameEngine.FIELD_WIDTH, GameEngine.FIELD_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);

        setupKeyListeners();
        setupMouseListeners();
        setupTimers();
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE -> engine.startRound();
                    case KeyEvent.VK_N -> engine.evolve();
                    case KeyEvent.VK_R -> engine.startRound();
                }
                repaint();
            }
        });
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && engine.isRunningRound()) {
                    startCharging(e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (charging && aiming && SwingUtilities.isLeftMouseButton(e) && engine.isRunningRound()) {
                    fireShot(e.getX(), e.getY());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (aiming && engine.isRunningRound()) {
                    aimX = e.getX();
                    aimY = e.getY();
                    repaint();
                }
            }
        });
    }

    private void setupTimers() {
        uiTimer = new Timer(16, e -> repaint());
        uiTimer.start();

        timer = new Timer(16, e -> {
            engine.updateShotsAndTargets();
            updateChargeBar();
            repaint();
        });
        timer.start();
    }

    private void startCharging(int x, int y) {
        aiming = true;
        charging = true;
        aimX = x;
        aimY = y;
        chargeStartTime = System.currentTimeMillis();
        repaint();
    }

    private void fireShot(int x, int y) {
        charging = false;
        aiming = false;
        engine.fireShotAtWithPower(x, y, chargePower);
        chargePower = 0;
        showMuzzleFlash = true;
        muzzleFlashStartTime = System.currentTimeMillis();
        repaint();
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
        drawBackground(g);
        updateMuzzleFlash();
        drawShots(g);
        drawAimingArc(g);
        drawTargets(g);
        drawShooter(g);
        drawAimingLine(g);
        drawChargeBar(g);
        drawInformation(g);
    }

    private void drawBackground(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void updateMuzzleFlash() {
        if (showMuzzleFlash && System.currentTimeMillis() - muzzleFlashStartTime > MUZZLE_FLASH_DURATION_MS) {
            showMuzzleFlash = false;
        }
    }

    private void drawShots(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (Shot s : engine.getActiveShots()) {
            g.fillOval((int) s.x - 4, (int) s.y - 4, 8, 8);
        }
    }

    private void drawAimingArc(Graphics g) {
        if (!aiming) return;

        g.setColor(new Color(255, 0, 0, 128));
        double shooterX = 0, shooterY = GameEngine.FIELD_HEIGHT;
        double dx = aimX - shooterX, dy = aimY - shooterY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        float power = charging ? chargePower : 1.0f;
        double speed = Shot.MIN_SPEED + (Shot.MAX_SPEED - Shot.MIN_SPEED) * power;
        double vx = (dx / dist) * speed;
        double vy = (dy / dist) * speed;
        double px = shooterX, py = shooterY;

        for (int i = 0; i < 60; i++) {
            vx *= Shot.DRAG;
            vy *= Shot.DRAG;
            vy += Shot.GRAVITY;
            px += vx;
            py += vy;
            g.fillOval((int) px - 2, (int) py - 2, 4, 4);
            if (px < 0 || px > GameEngine.FIELD_WIDTH || py < 0 || py > GameEngine.FIELD_HEIGHT) break;
        }
    }

    private void drawTargets(Graphics g) {
        for (Target target : engine.getPopulation()) {
            target.updateHitState();
            target.updateExplosion();
            target.draw((Graphics2D) g);
            target.drawExplosion((Graphics2D) g);
        }
    }

    private void drawShooter(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int shooterBaseX = 0;
        int shooterBaseY = GameEngine.FIELD_HEIGHT;
        double shooterAngle = aiming ? Math.atan2(aimY - shooterBaseY, aimX - shooterBaseX) : Math.toRadians(45);
        double shotgunScale = 0.4;

        if (shotgunImage != null) {
            int shotgunImgWidth = shotgunImage.getWidth(this);
            int shotgunImgHeight = shotgunImage.getHeight(this);
            if (shotgunImgWidth <= 0 || shotgunImgHeight <= 0) return;

            Graphics2D gShotgun = (Graphics2D) g2d.create();
            gShotgun.translate(shooterBaseX, shooterBaseY);
            gShotgun.rotate(shooterAngle);
            gShotgun.scale(shotgunScale, shotgunScale);
            int shotgunXOffset = (int) (-100 * shotgunScale);
            int shotgunYOffset = (int) ((-shotgunImgHeight + 120) * shotgunScale);
            gShotgun.drawImage(shotgunImage, shotgunXOffset, shotgunYOffset, this);

            if (showMuzzleFlash && muzzleFlashImage != null) {
                int muzzleFlashWidth = muzzleFlashImage.getWidth(this);
                int muzzleFlashHeight = muzzleFlashImage.getHeight(this);
                if (muzzleFlashWidth > 0 && muzzleFlashHeight > 0) {
                    int muzzleFlashX = (int) ((shotgunImgWidth - 28) * shotgunScale);
                    int muzzleFlashY = (int) ((-shotgunImgHeight + 120 - muzzleFlashHeight / 2) * shotgunScale);
                    gShotgun.drawImage(muzzleFlashImage, muzzleFlashX, muzzleFlashY, (int) (muzzleFlashWidth * shotgunScale), (int) (muzzleFlashHeight * shotgunScale), this);
                }
            }
            gShotgun.dispose();
        } else {
            int shooterSize = 24;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(shooterBaseX, shooterBaseY - shooterSize, shooterSize, shooterSize);
        }
    }

    private void drawAimingLine(Graphics g) {
        if (aiming) {
            g.setColor(Color.GREEN);
            g.drawLine(0, GameEngine.FIELD_HEIGHT, aimX, aimY);
        }
    }

    private void drawChargeBar(Graphics g) {
        if (!charging) return;
        int barX = 10, barY = GameEngine.FIELD_HEIGHT - CHARGE_BAR_HEIGHT - 10;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(barX, barY, CHARGE_BAR_WIDTH, CHARGE_BAR_HEIGHT);
        g.setColor(Color.GREEN.darker());
        g.fillRect(barX, barY, (int) (CHARGE_BAR_WIDTH * chargePower), CHARGE_BAR_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, CHARGE_BAR_WIDTH, CHARGE_BAR_HEIGHT);
    }

    private void drawInformation(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Generation: " + engine.getGeneration(), 10, 20);
        g.drawString("Shots Fired: " + engine.getRoundShotsFired() + "/" + GameEngine.SHOTS_PER_ROUND, 10, 35);
        g.drawString("SPACE: Run round | N: Next gen | R: Reset | Click+Hold+Release: Shoot", 10, 50);
        g.drawString("Hold to charge shot power (bar at bottom left)", 10, 65);
    }
}