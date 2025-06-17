package shootage.src.game;

import shootage.src.genetics.GeneticAlgorithm;
import shootage.src.genetics.Genome;
import shootage.src.target.Target;
import shootage.src.ai.ShotHistory;

import java.util.*;

public class GameEngine {
    public static final int POPULATION = 20;
    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 600;
    public static final int SHOTS_PER_ROUND = 30;

    private List<Target> population;
    private ShotHistory shotHistory;
    private GeneticAlgorithm ga;
    private int generation;
    private Random rand;
    private int roundShotsFired;
    private boolean runningRound;
    private List<Shot> activeShots = new ArrayList<>();

    public GameEngine() {
        ga = new GeneticAlgorithm();
        rand = new Random();
        generation = 1;
        resetPopulation();
    }

    private void resetPopulation() {
        population = new ArrayList<>();
        for (int i = 0; i < POPULATION; i++) {
            Genome g = new Genome();
            int x = 400 + rand.nextInt(200);
            int y = 100 + rand.nextInt(400);
            population.add(new Target(x, y, g));
        }
        shotHistory = new ShotHistory(FIELD_WIDTH, FIELD_HEIGHT);
        roundShotsFired = 0;
        runningRound = false;
        activeShots.clear();
    }

    public void startRound() {
        for (Target t : population) {
            t.reset(FIELD_WIDTH, FIELD_HEIGHT);
        }
        shotHistory = new ShotHistory(FIELD_WIDTH, FIELD_HEIGHT);
        roundShotsFired = 0;
        runningRound = true;
        activeShots.clear();
    }

    // Timer-based update: move shots and check for hits
    public void updateShotsAndTargets() {
        // Move shots
        for (Shot shot : new ArrayList<>(activeShots)) {
            shot.move();
        }

        // Remove shots out of bounds
        activeShots.removeIf(s -> s.x < 0 || s.x > FIELD_WIDTH || s.y < 0 || s.y > FIELD_HEIGHT);

        // Check collisions
        for (Shot shot : new ArrayList<>(activeShots)) {
            for (Target tar : population) {
                if (tar.isAlive() && tar.isHit((int) shot.x, (int) shot.y)) {
                    tar.registerHit();
                    tar.die();
                }
            }
        }

        // Update hit animation states
        for (Target t : population) {
            t.updateHitState();
        }
    }

    public void step() {
        if (!runningRound) return;
        if (roundShotsFired >= SHOTS_PER_ROUND) {
            runningRound = false;
            return;
        }
        int shotX = 100 + rand.nextInt(FIELD_WIDTH - 200);
        int shotY = 50 + rand.nextInt(FIELD_HEIGHT - 100);
        shotHistory.recordShot(shotX, shotY);

        for (Target tar : population) {
            if (tar.isAlive()) {
                tar.update(shotHistory, FIELD_WIDTH, FIELD_HEIGHT);
                if (tar.isHit(shotX, shotY)) {
                    tar.registerHit();
                    tar.die();
                } else {
                    tar.addFitness(1);
                }
            }
        }
        roundShotsFired++;
        if (roundShotsFired >= SHOTS_PER_ROUND) {
            runningRound = false;
        }
    }

    // Called from mouse release to shoot a physics-based bullet
    public void fireShotAt(double targetX, double targetY) {
        double shooterX = 0;
        double shooterY = FIELD_HEIGHT;
        activeShots.add(new Shot(shooterX, shooterY, targetX, targetY));
    }

    // Called from GamePanel with charge power
    public void fireShotAtWithPower(double targetX, double targetY, float power) {
        double shooterX = 0;
        double shooterY = FIELD_HEIGHT;
        double speed = Shot.MIN_SPEED + (Shot.MAX_SPEED - Shot.MIN_SPEED) * power;
        activeShots.add(new Shot(shooterX, shooterY, targetX, targetY, speed));
    }

    public void evolve() {
        double avgFitness = population.stream().mapToDouble(Target::getFitness).average().orElse(0);
        Target best = Collections.max(population, Comparator.comparingDouble(Target::getFitness));
        System.out.printf("Generation %d - Avg fitness: %.2f, Best: %.2f, Genome: %s%n",
                generation, avgFitness, best.getFitness(), Arrays.toString(best.getGenome().getGenes()));

        List<Genome> nextGenGenomes = ga.nextGeneration(population, POPULATION);
        population.clear();
        for (Genome g : nextGenGenomes) {
            int x = 400 + rand.nextInt(200);
            int y = 100 + rand.nextInt(400);
            population.add(new Target(x, y, g));
        }
        generation++;
        startRound();
    }

    public List<Target> getPopulation() {
        return population;
    }

    public ShotHistory getShotHistory() {
        return shotHistory;
    }

    public int getGeneration() {
        return generation;
    }

    public int getRoundShotsFired() {
        return roundShotsFired;
    }

    public boolean isRunningRound() {
        return runningRound;
    }

    public List<Shot> getActiveShots() {
        return activeShots;
    }
}