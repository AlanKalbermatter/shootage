package ai.genetics;

import java.util.Random;

public class Genome implements IGenome {
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

    public double[] getGenes() {
        return genes.clone();
    }

    public IGenome copy() {
        return new Genome(this.genes);
    }
}