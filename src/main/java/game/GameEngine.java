package game;

import ai.genetics.GeneticAlgorithm;
import ai.genetics.Genome;
import model.Shot;
import model.Target;
import ai.ShotHistory;
import utils.SoundManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GameEngine {
    public static final int POPULATION = 1; // Increased for diversity
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
    private int populationSize = 1;

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

    public void updateShotsAndTargets() {
        for (Shot shot : new ArrayList<>(activeShots)) {
            shot.move();
        }
        activeShots.removeIf(s -> s.x < 0 || s.x > FIELD_WIDTH || s.y < 0 || s.y > FIELD_HEIGHT);

        for (Target tar : population) {
            tar.update(activeShots, shotHistory, FIELD_WIDTH, FIELD_HEIGHT);
        }
        for (Shot shot : new ArrayList<>(activeShots)) {
            for (Target tar : population) {
                if (tar.isAlive() && tar.isHit((int) shot.x, (int) shot.y)) {
                    tar.registerHit();
                    tar.die();
                }
            }
        }

        for (Target target : population) {
            target.updateHitState();
            if (target.isAlive()) {
                target.addFitness(1.0);  // Base fitness for surviving

                // Reward for dodging close calls
                for (Shot shot : activeShots) {
                    double distanceX = target.getX() - shot.x;
                    double distanceY = target.getY() - shot.y;
                    double distanceToShot = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                    if (distanceToShot < 100 && distanceToShot > target.getRadius()) {
                        target.addFitness(8.0 / (distanceToShot + 10));  // Higher reward for closer dodges
                    }
                }

                // Penalize for clustering with other targets
                for (Target otherTarget : population) {
                    if (target != otherTarget && otherTarget.isAlive()) {
                        double distanceX = target.getX() - otherTarget.getX();
                        double distanceY = target.getY() - otherTarget.getY();
                        double squaredDistance = distanceX * distanceX + distanceY * distanceY;
                        if (squaredDistance < 1200) {  // Too close to another target
                            target.addFitness(-0.5);  // Clustering penalty
                        }
                    }
                }

                // Penalize excessive movement to encourage efficient dodging
                target.addFitness(-0.2 * target.getLastMoveDistance());

                // Reward being far from historical shot locations (hot zones)
                int historyX = Math.max(0, Math.min(FIELD_WIDTH - 1, target.getX()));
                int historyY = Math.max(0, Math.min(FIELD_HEIGHT - 1, target.getY()));
                double shotDensity = shotHistory.getDensityAt(historyX, historyY);
                target.addFitness(1.0 / (1.0 + shotDensity));  // Inverse relationship to shot density
            }
        }

        // --- Automatic next generation when all targets are dead ---
        if (runningRound && population.stream().noneMatch(Target::isAlive)) {
            runningRound = false;
            evolve();
        }
    }

    public void fireShotAtWithPower(double targetX, double targetY, float power) {
        double shooterX = 0;
        double shooterY = FIELD_HEIGHT;
        double speed = Shot.MIN_SPEED + (Shot.MAX_SPEED - Shot.MIN_SPEED) * power;
        activeShots.add(new Shot(shooterX, shooterY, targetX, targetY, speed));
        SoundManager.playSoundEffect("/sounds/shotgun.wav");
    }

    public void evolve() {
        double avgFitness = population.stream().mapToDouble(Target::getFitness).average().orElse(0);
        double std = Math.sqrt(population.stream().mapToDouble(t -> Math.pow(t.getFitness() - avgFitness, 2)).sum() / population.size());
        Target best = Collections.max(population, Comparator.comparingDouble(Target::getFitness));
        System.out.printf(
                "Generation %d - Avg fitness: %.2f, Std dev: %.2f, Best: %.2f, Genome: %s%n",
                generation, avgFitness, std, best.getFitness(), Arrays.toString(best.getGenome().getGenes())
        );
        List<Genome> nextGenGenomes = ga.nextGeneration(population, populationSize);
        population.clear();
        for (Genome g : nextGenGenomes) {
            int x = 400 + rand.nextInt(200);
            int y = 100 + rand.nextInt(400);
            population.add(new Target(x, y, g));
        }
        generation++;
        populationSize++;
        startRound();
    }

    public List<Target> getPopulation() {
        return population;
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