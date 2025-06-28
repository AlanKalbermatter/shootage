/**
 * GeneticAlgorithm handles the creation of new generations of genomes for evolving the target agents.

 * Features:
 * - Tournament selection for parent picking.
 * - Elitism: best genomes are passed to next generation unchanged.
 * - Crossover: genes are randomly inherited from either parent.
 * - Mutation: random Gaussian noise with configurable rate and strength.

 * Configuration:
 * - MUTATION_RATE: Probability each gene mutates.
 * - MUTATION_STRENGTH: Standard deviation of mutation noise.
 * - ELITE_COUNT: Number of top genomes preserved unchanged each generation.
 * - TOURNAMENT_SIZE: Number of competitors per parent tournament.

 * Usage:
 * Call nextGeneration() with the previous generation of Target agents and desired population size.
 * Returns a new List of Genome objects for the next generation.
 */
package ai.genetics;

import model.Target;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    /** Probability of mutating a gene (higher = more diversity). */
    private static final double MUTATION_RATE = 0.30;
    /** Magnitude of mutation change (higher = more disruptive). */
    private static final double MUTATION_STRENGTH = 0.35;
    /** Number of top genomes preserved unmodified in next generation. */
    private static final int ELITE_COUNT = 3;
    /** Number of candidates in tournament selection. */
    private static final int TOURNAMENT_SIZE = 3;

    /**
     * Tournament selection: randomly selects TOURNAMENT_SIZE individuals and picks the fittest.
     * Repeats until numParents are selected.
     */
    private List<Target> selectParents(List<Target> population, int numParents) {
        List<Target> selected = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < numParents; i++) {
            Target best = null;
            for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                Target candidate = population.get(rand.nextInt(population.size()));
                if (best == null || candidate.getFitness() > best.getFitness()) {
                    best = candidate;
                }
            }
            selected.add(best);
        }
        return selected;
    }

    /**
     * Creates the next generation of Genomes given the current population.
     * - Top ELITE_COUNT are copied unchanged (elitism).
     * - Rest are created by crossover and mutation from selected parents.

     * @param population Current population of Targets (used for fitness and genomes)
     * @param populationSize Desired size of next generation
     * @return List of Genomes for the next generation
     */
    public List<Genome> nextGeneration(List<Target> population, int populationSize) {
        List<Genome> newGenomes = new ArrayList<>();
        // Sort by fitness descending
        population.sort(Comparator.comparingDouble(Target::getFitness).reversed());
        // Elitism: copy best directly
        for (int i = 0; i < ELITE_COUNT && i < population.size(); i++) {
            newGenomes.add((Genome) population.get(i).getGenome().copy());
        }
        // Fill rest with children from crossover+mutation
        while (newGenomes.size() < populationSize) {
            List<Target> parents = selectParents(population, 2);
            Genome child = crossover(parents.get(0).getGenome(), parents.get(1).getGenome());
            mutate(child);
            newGenomes.add(child);
        }
        return newGenomes;
    }

    /**
     * Single-point crossover: each gene is chosen randomly from either parent.
     */
    private Genome crossover(Genome g1, Genome g2) {
        double[] genes1 = g1.getGenes();
        double[] genes2 = g2.getGenes();
        double[] childGenes = new double[genes1.length];
        Random rand = new Random();
        for (int i = 0; i < childGenes.length; i++) {
            childGenes[i] = rand.nextBoolean() ? genes1[i] : genes2[i];
        }
        return new Genome(childGenes);
    }

    /**
     * Mutates a genome's genes with Gaussian noise, clamping to valid ranges.
     * - First gene is clamped to [0,1] (random movement parameter).
     * - Others are clamped to [-2,2].
     */
    private void mutate(Genome g) {
        Random rand = new Random();
        double[] genes = g.getGenes();
        for (int i = 0; i < genes.length; i++) {
            if (rand.nextDouble() < MUTATION_RATE) {
                genes[i] += (rand.nextGaussian() * MUTATION_STRENGTH);
                // Clamp values for the first gene (random movement) to [0, 1]
                if (i == 0) {
                    genes[i] = Math.max(0.0, Math.min(1.0, genes[i]));
                } else {
                    genes[i] = Math.max(-2.0, Math.min(2.0, genes[i]));
                }
            }
        }
    }
}