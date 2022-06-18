package problems.kqbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import metaheuristics.ga.AbstractGA;
import metaheuristics.ga.AbstractGA.Chromosome;
import metaheuristics.ga.AbstractGA.Population;
import problems.kqbf.KQBF;
import solutions.Solution;

/**
 * Metaheuristic GA (Genetic Algorithm) for
 * obtaining an optimal solution to a KQBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). 
 * 
 * @author ccavellucci, fusberti
 */
public class GA_KQBF extends AbstractGA<Integer, Integer> {
	
	/**
	 * Constructor for the GA_KQBF class. The QBF objective function is passed as
	 * argument for the superclass constructor.
	 * 
	 * @param generations
	 *            Maximum number of generations.
	 * @param popSize
	 *            Size of the population.
	 * @param mutationRate
	 *            The mutation rate.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public GA_KQBF(Integer generations, Integer popSize, Double mutationRate, Strategies strategy, String filename) throws IOException {
		super(new KQBF(filename), generations, popSize, mutationRate, strategy);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		return new Solution<Integer>(0.0, 0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#decode(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Solution<Integer> decode(Chromosome chromosome) {

		Solution<Integer> solution = createEmptySol();
		for (int locus = 0; locus < chromosome.size(); locus++) {
			if (chromosome.get(locus) == 1) {
				solution.add(new Integer(locus));
			}
		}

		ObjFunction.evaluate(solution);
		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#generateRandomChromosome()
	 */
	@Override
	protected Chromosome generateRandomChromosome() {
		return this.optimazedGenerateRandomChromosome();
	}
	
	protected Chromosome standardGenerateRandomChromosome() {
		
		KQBF kqbf = (KQBF) ObjFunction;
		Chromosome chromosome = new Chromosome();
		
		while (chromosome.isEmpty() || decode(chromosome).weight > kqbf.weight) {
			chromosome = new Chromosome();

			for (int i = 0; i < chromosomeSize; i++) {
				chromosome.add(rng.nextInt(2));
			}
		};

		return chromosome;
	}
	
	// trying to optimize random chromosome with knapsack
	protected Chromosome optimazedGenerateRandomChromosome() {
		
		KQBF kqbf = (KQBF) ObjFunction;
		Chromosome chromosome = new Chromosome();
		
		while (chromosome.isEmpty() || decode(chromosome).weight > kqbf.weight) {
			chromosome = new Chromosome();
			boolean overWeight = false;

			for (int i = 0; i < chromosomeSize; i++) {
				if (!overWeight) {
					chromosome.add(rng.nextInt(2));
					
					if (decode(chromosome).weight > kqbf.weight) {
						chromosome.set(i, 0);
						overWeight = true;
					}
				} else {
					chromosome.add(0);
				}
			}
		};

		return chromosome;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#crossover()
	 */
	@Override
	protected Population crossover(Population parents) {
		if (Strategies.UNIFORM_CROSSOVER.equals(this.strategy)) {
			return this.crossoverUniform(parents);
		}

		return this.crossoverStandard(parents);
	}
	
	protected Population crossoverStandard(Population parents) {

		Population offsprings = new Population();

		for (int i = 0; i < popSize; i = i + 2) {

			Chromosome parent1 = parents.get(i);
			Chromosome parent2 = parents.get(i + 1);

			int crosspoint1 = rng.nextInt(chromosomeSize + 1);
			int crosspoint2 = crosspoint1 + rng.nextInt((chromosomeSize + 1) - crosspoint1);

			Chromosome offspring1 = new Chromosome();
			Chromosome offspring2 = new Chromosome();

			for (int j = 0; j < chromosomeSize; j++) {
				if (j >= crosspoint1 && j < crosspoint2) {
					offspring1.add(parent2.get(j));
					offspring2.add(parent1.get(j));
				} else {
					offspring1.add(parent1.get(j));
					offspring2.add(parent2.get(j));
				}
			}
			
			KQBF kqbf = (KQBF) ObjFunction;
			
			if (decode(offspring1).weight <= kqbf.weight) {
				offsprings.add(offspring1);
			} else {
				offsprings.add(parent1);
			}
			
			if (decode(offspring2).weight <= kqbf.weight) {
				offsprings.add(offspring2);
			} else {
				offsprings.add(parent2);
			}
		}

		return offsprings;

	}
	
	protected Population crossoverUniform(Population parents) {

		Population offsprings = new Population();

		for (int i = 0; i < popSize; i = i + 2) {

			Chromosome parent1 = parents.get(i);
			Chromosome parent2 = parents.get(i + 1);
			
			Chromosome offspring1 = new Chromosome();
			Chromosome offspring2 = new Chromosome();
			
			for (int j = 0; j < chromosomeSize; j++) {
				if (rng.nextInt(2) == 1) {
					offspring1.add(parent1.get(j));
					offspring2.add(parent2.get(j));
				} else {
					offspring1.add(parent2.get(j));
					offspring2.add(parent1.get(j));
				}
			}
			
			KQBF kqbf = (KQBF) ObjFunction;
			
			if (decode(offspring1).weight <= kqbf.weight) {
				offsprings.add(offspring1);
			} else {
				offsprings.add(parent1);
			}
			
			if (decode(offspring2).weight <= kqbf.weight) {
				offsprings.add(offspring2);
			} else {
				offsprings.add(parent2);
			}
		}

		return offsprings;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#fitness(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {
		return decode(chromosome).cost;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * metaheuristics.ga.AbstractGA#mutateGene(metaheuristics.ga.AbstractGA.
	 * Chromosome, java.lang.Integer)
	 */
	@Override
	protected void mutateGene(Chromosome chromosome, Integer locus) {
		Chromosome clone = new Chromosome();
		for (int i = 0; i < chromosomeSize; i++) {
			clone.add(chromosome.get(i));
		}

		clone.set(locus, 1 - clone.get(locus));

		KQBF kqbf = (KQBF) ObjFunction;
		if (decode(clone).weight <= kqbf.weight) {
			chromosome.set(locus, 1 - chromosome.get(locus));
		}
	}

	/**
	 * A main method used for testing the GA metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		GA_KQBF ga = new GA_KQBF(1000, 100, 1.0 / 100.0, Strategies.ADAPTIVE_MUTATION, "instances/kqbf/kqbf400");
		Solution<Integer> bestSol = ga.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");

	}

}
