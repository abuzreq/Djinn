
import java.util.Arrays;

public class Individual implements Comparable<Individual> {
	private Gene[] genes;
	private double[] behavior;
	private double novelty = Double.POSITIVE_INFINITY;

	public Individual(Generator generator) {
		genes = generator.generateGenotype();
		behavior = generator.getBehaviour(genes);
	}

	private Individual() {
	}

	public Individual clone()
	{
		Individual clone = new Individual();
		Gene[] newGenes = new Gene[genes.length];
		for (int i = 0; i < genes.length; i++) 
		{
			newGenes[i] = genes[i].clone();
		}
		clone.genes = newGenes;
		clone.behavior = Arrays.copyOf(this.behavior, this.behavior.length);
		clone.novelty = this.novelty;
		return clone;
	}

	public double distanceSquared(Individual other) {

		int k = Math.min(this.behavior.length, other.behavior.length);
		double diff = 0;
		for (int r = 0; r < k; r++) {
			diff += Math.pow(this.behavior[r] - other.behavior[r], 2);
		}
		return diff;
	}

	public int getNumberOfGenes() {
		return genes.length;
	}

	public int getNumberOfBehaviors() {
		return behavior.length;
	}

	public Gene[] getGenes() {
		return genes;
	}
	private double[] getGenesValues()
	{
		double[] values = new double[getNumberOfGenes()];
		for(int i =0 ; i < values.length;i++)
		{
			values[i] = genes[i].getValue();
		}
		return values;
	}
	public double[] getBehavior() {
		return behavior;
	}

	public double getNovelty() {
		return novelty;
	}

	public void setNovelty(double novelty) {
		this.novelty = novelty;
	}

	public void mutate(double mutationAmount, Generator gen) {
		mutate(1.0 / genes.length,mutationAmount, gen);
	}

	public void mutate(double mutationProbability,double mutationAmount, Generator gen) {
		for (int i = 0; i < genes.length; i++) {
			if (gen.rand.nextDouble() < mutationProbability) {
				//Ahmed: this variable introduced so that mutation occures relative to the gene's domain
				double relativeMutationAmount = mutationAmount * genes[i].getRange();
				genes[i].update(relativeMutationAmount * gen.rand.nextDouble() - relativeMutationAmount / 2.0);
				// Ahmed: circular update, if more than high, values becomes low
				if (genes[i].getValue() > genes[i].getHigh()) {
					genes[i].update(-genes[i].getRange());
				} else if (genes[i].getValue() < genes[i].getLow()) {
					genes[i].update(genes[i].getRange());
				}
			}
		}
		behavior = gen.getBehaviour(genes);
	}

	@Override
	public int compareTo(Individual solution2) {
		if (novelty < solution2.novelty) {
			return -1;
		} else if (novelty > solution2.novelty) {
			return 1;
		} else {
			return 0;
		}
	}
	@Override
	public boolean equals(Object obj) 
    {
		Individual ind = (Individual)obj;	
		return Arrays.equals(ind.getGenesValues(), this.getGenesValues());
    }
	
	@Override
	public String toString() 
	{
		return ""+novelty;
		/*
		return "Solution{Variables: " + Arrays.toString(genes) + "\nBehavior: " + Arrays.toString(behavior)
				+ "\nNovelty: " + novelty + "}";*/
	}
}
