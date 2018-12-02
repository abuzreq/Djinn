
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Arrays;
import java.util.Comparator;

public class NoveltySearch
{
	private Generator gen;
	
	private Population population;
	private Population archive;
	private int numberOfIterations;
	
	
	private int tournamentSize;
	private int numNearestNeighbors; 
	private double sparsenessThreshold = Math.pow(.05, 2);


	/**
	 * 
	 * @param gen: The generator
	 * @param populationSize: The size of the population
	 * @param numNearestNeighbors: The number of nearest neighbors to consider
	 * @param tournamentSize: The number of individuals to compete toward being mutated (i.e. parameter for Tournament Selection)
	 */
	public NoveltySearch(Generator gen,int populationSize,int numberOfIterations, int numNearestNeighbors, double initialSparsenessThreshold, int tournamentSize) 
	{
		this.gen = gen;
		population = new Population(gen,populationSize);
		archive = new Population(gen,0);
		this.numNearestNeighbors = numNearestNeighbors;
		this.sparsenessThreshold = initialSparsenessThreshold;
		this.tournamentSize = tournamentSize;
		this.numberOfIterations = numberOfIterations;
	}
	Comparator<Individual> noveltyCompratator = new Comparator<Individual>() {

		@Override
		public int compare(Individual o1, Individual o2) {
			return o1.getNovelty() - o2.getNovelty()> 0?-1:1  ;
		}
	};
	public void run()
	{
		for (int i = 0 ; i < numberOfIterations; i++) 
		{
			this.iterate();
		}
	}
	private void iterate() {
		//		
		/* TODO For Future: Adaptive change on novelty threshold
			An adaptive threshold ρmin is used to determine which
			behaviors to include in the archive. If the novelty of a new
			individual is higher than the threshold (ρ(x) > ρmin), it
			is added to the archive. To keep the size of the archive approximately
			constant, ρmin is increased by a fixed fraction if
			the number of added behaviors exceeds the addmax threshold
			in a certain number of evaluations. If the number of
			added behaviors is lower than addmin in a certain number
			of evaluations, ρmin is decreased by a fixed fraction
			Source: Critical Factors in the Performance of Novelty Search, 
			by Steijn Kistemaker and Shimon Whiteson
		 */
		
		 Population populationAndArchive = (Population) population.clone();
		 populationAndArchive.addAll(archive);

		// determine novelty
		for (Individual solution : population) {
			solution.setNovelty(Double.POSITIVE_INFINITY);
			computeNovelty(solution, populationAndArchive, numNearestNeighbors);
			//Scheme 1, add to archive if > threshold, threshold changes dynamicly
			if (solution.getNovelty() >= sparsenessThreshold) {
				archive.add(solution);
			}
			
		}

		// select parents
		Population children = new Population(gen,0);
		while (children.size() < population.size()) {
			Individual parent = tournamentSelect(population, tournamentSize);
			Individual child = parent.clone();

			child.mutate(.25,0.1,gen); // TODO: Fix mutation rate
			children.add(child);
		}

		population.clear();
		population.addAll(children);
	}

	public void computeNoveltyCheck(Individual solution, Population comparisonGroup, int k) {
		double leastDistSq = Double.POSITIVE_INFINITY;
		for (Individual otherSolution : comparisonGroup) {
			if (solution != otherSolution) {
				double distSq = solution.distanceSquared(otherSolution);
				if (distSq < leastDistSq) {
					leastDistSq = distSq;
				}
			}
		}
		
		if (leastDistSq != solution.getNovelty()) {
			System.out.println("Queue Novelty: " + solution.getNovelty());
			System.out.println("Loop Novelty: " + leastDistSq);
			System.out.println("This Solution:");
			System.out.println(solution);
			System.out.println("Archive:");
			printArchive();
		}
	}

	public void computeNovelty(Individual solution, Population comparisonGroup, int k) {
		// store negative values so value with greatest magnitude can be removed
		// efficiently
		PriorityQueue<Double> leastDistances = new PriorityQueue<>(k + 1);

		int nonSelfCount = 0;

		for (Individual otherSolution : comparisonGroup)
		{
			if (!solution.equals(otherSolution)) {

				nonSelfCount++;

				double distSq = -1 * solution.distanceSquared(otherSolution);
				int queueSizeBeforeAdd = leastDistances.size();
				if (queueSizeBeforeAdd < k || distSq > leastDistances.peek()) {
					leastDistances.offer(distSq);
					if (queueSizeBeforeAdd + 1 > k) {
						leastDistances.poll();
					}
				}
			}
		}

		double distSqSum = 0.0;
		for (double distSq : leastDistances) {
			distSqSum += distSq;
		}
		// recover positive square distances from negatives
		distSqSum *= -1;
		if (nonSelfCount == 0) {
			solution.setNovelty(Double.POSITIVE_INFINITY);
		} else {
			solution.setNovelty(distSqSum / Math.min(nonSelfCount, k));
		}
	}

	public Individual tournamentSelect(Population population, int tournamentSize) 
	{
		Individual bestCandidate = population.get(gen.rand.nextInt(population.size()));
		double mostNovel = bestCandidate.getNovelty();
		for (int i = 0; i < tournamentSize - 1; i++) {
			Individual candidate = population.get(gen.rand.nextInt(population.size()));
			double novelty = candidate.getNovelty();
			if (novelty > mostNovel) {
				bestCandidate = candidate;
				mostNovel = novelty;
			}
		}
		return bestCandidate;
	}

	public void printArchive() {
		printSolutions(archive);
	}
	
	public void printPopulation() {
		printSolutions(population);
	}


	private static void printSolutions(Population population){
		System.out.print("[");
		for (int i = 0; i < population.size() - 1; i++) {
			System.out.print(Arrays.toString(population.get(i).getBehavior()) + ", ");
		}
		System.out.println(Arrays.toString(population.get(population.size() - 1).getBehavior()) + "]");
	}

	public Population getArchive() 
	{
		return archive;
	}
	
	public Population getPopulation() {
		return population;
	}
}
