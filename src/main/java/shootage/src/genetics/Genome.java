package shootage.src.genetics;

import java.util.Random;

public class Genome {
    private static final int GENE_COUNT = 3;
    private static final Random rand = new Random();
    private final double[] genes;

    public Genome() {
        genes = new double[GENE_COUNT];
        for (int i = 0; i < GENE_COUNT; i++) {
            genes[i] = rand.nextDouble(); // random value in [0,1)
        }
    }

    public Genome(double[] genes) {
        this.genes = genes.clone();
    }

    // Single-point crossover and mutation
    public static Genome crossover(Genome p1, Genome p2) {
        double[] childGenes = new double[GENE_COUNT];
        int crossPoint = rand.nextInt(GENE_COUNT);
        for (int i = 0; i < GENE_COUNT; i++) {
            childGenes[i] = (i < crossPoint) ? p1.genes[i] : p2.genes[i];
        }
        // Mutation
        for (int i = 0; i < GENE_COUNT; i++) {
            if (rand.nextDouble() < 0.1) { // 10% mutation rate
                childGenes[i] += (rand.nextDouble() - 0.5) * 0.2;
                childGenes[i] = Math.max(0, Math.min(1, childGenes[i])); // Clamp to [0,1]
            }
        }
        return new Genome(childGenes);
    }

    public double[] getGenes() {
        return genes.clone();
    }

    public Genome copy() {
        return new Genome(this.genes);
    }
}