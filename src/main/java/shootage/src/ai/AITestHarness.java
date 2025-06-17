package shootage.src.ai;

import shootage.src.genetics.GeneticAlgorithm;
import shootage.src.genetics.Genome;
import shootage.src.target.Target;

import java.util.*;

public class AITestHarness {
    static final int POPULATION = 20;
    static final int ROUNDS = 50;
    static final int SHOTS_PER_ROUND = 30;
    static final int FIELD_WIDTH = 800;
    static final int FIELD_HEIGHT = 600;

    public static void main(String[] args) {
        GeneticAlgorithm ga = new GeneticAlgorithm();
        List<Target> population = new ArrayList<>();
        Random rand = new Random();

        // Initialize random population
        for (int i = 0; i < POPULATION; i++) {
            Genome g = new Genome();
            int x = 400 + rand.nextInt(200);
            int y = 100 + rand.nextInt(400);
            population.add(new Target(x, y, g));
        }

        for (int round = 1; round <= ROUNDS; round++) {
            // Reset targets for new round
            for (Target t : population) {
                t.reset(FIELD_WIDTH, FIELD_HEIGHT);
            }

            ShotHistory history = new ShotHistory(FIELD_WIDTH, FIELD_HEIGHT);

            for (int shot = 0; shot < SHOTS_PER_ROUND; shot++) {
                // Simulate a shot: random location
                int shotX = 100 + rand.nextInt(FIELD_WIDTH - 200);
                int shotY = 50 + rand.nextInt(FIELD_HEIGHT - 100);
                history.recordShot(shotX, shotY);

                for (Target tar : population) {
                    if (tar.isAlive()) {
                        tar.update(history, FIELD_WIDTH, FIELD_HEIGHT);

                        // If target is too close to shot, it's "hit"
                        if (tar.isHit(shotX, shotY)) {
                            tar.die();
                        } else {
                            tar.addFitness(1); // Survived another shot
                        }
                    }
                }
            }

            // Report and evolve
            double avgFitness = population.stream().mapToDouble(Target::getFitness).average().orElse(0);
            Target best = Collections.max(population, Comparator.comparingDouble(Target::getFitness));
            System.out.printf(
                    "Round %d - Avg fitness: %.2f, Best: %.2f, Genome: %s%n",
                    round, avgFitness, best.getFitness(), Arrays.toString(best.getGenome().getGenes())
            );

            // Evolve next generation
            List<Genome> nextGenGenomes = ga.nextGeneration(population, POPULATION);
            population.clear();
            for (Genome g : nextGenGenomes) {
                int x = 400 + rand.nextInt(200);
                int y = 100 + rand.nextInt(400);
                population.add(new Target(x, y, g));
            }
        }
    }
}