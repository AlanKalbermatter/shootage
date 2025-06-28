package ai.genetics;

public interface IGenome {
    double[] getGenes();
    IGenome copy();
}
