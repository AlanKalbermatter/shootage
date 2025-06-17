package shootage.src.genetics;

import shootage.src.target.Target;

import java.util.*;

public class GeneticAlgorithm {
    private static final Random rand = new Random();

    public List<Genome> nextGeneration(List<Target> population, int popSize) {
        // Elitism: keep best 2
        population.sort(Comparator.comparingDouble(Target::getFitness).reversed());
        List<Genome> nextGen = new ArrayList<>();
        for (int i = 0; i < 2 && i < population.size(); i++) {
            nextGen.add(population.get(i).getGenome().copy());
        }
        // Fill rest with offspring
        while (nextGen.size() < popSize) {
            Genome parent1 = tournamentSelect(population);
            Genome parent2 = tournamentSelect(population);
            Genome child = Genome.crossover(parent1, parent2);
            nextGen.add(child);
        }
        return nextGen;
    }

    private Genome tournamentSelect(List<Target> population) {
        int tournamentSize = 3;
        List<Target> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            tournament.add(population.get(rand.nextInt(population.size())));
        }
        return Collections.max(tournament, Comparator.comparingDouble(Target::getFitness)).getGenome();
    }
}