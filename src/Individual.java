
import java.util.Arrays;

public class Individual implements Comparable<Individual> {
	private Gene[] variables;
	private double[] behavior;
	private double novelty = Double.POSITIVE_INFINITY;
	public static double mutationAmount = .05;

	public Individual(Generator generator) {
		variables = generator.generateGenotype();
		behavior = generator.getBehaviour(variables);
	}
	private Individual() 
	{		
	}
	public Individual clone()
	{
		Individual clone = new Individual();
		Gene[] newVars = new Gene[variables.length];
		for(int i = 0 ; i < variables.length;i++)
		{
			newVars[i] = variables[i].clone();
		}
		clone.variables = newVars;
		clone.behavior = Arrays.copyOf(this.behavior,this.behavior.length);
		return clone;
	}



	public double distanceSquared(Individual other)
	{
		

		int k = Math.min(this.behavior.length,other.behavior.length);
		double diff = 0;
		for (int r = 0; r < k; r++) {
			diff += Math.pow(this.behavior[r] - other.behavior[r], 2);
		}
		return diff;
		
		/*
		 if (behavior.length != other.behavior.length) {
			throw new IllegalArgumentException("Different dimensions.");
		}
		double dist2 = 0.0;
		for (int i = 0; i < behavior.length; i++)
		{
			dist2 += Math.pow(behavior[i] - other.behavior[i], 2);
			//dist2 += pixelDiff((int)(behavior[i]) , (int)(other.behavior[i]));
		}
		return dist2;
		*/
	}
	 private static int pixelDiff(int rgb1, int rgb2) {
	        int r1 = (rgb1 >> 16) & 0xff;
	        int g1 = (rgb1 >>  8) & 0xff;
	        int b1 =  rgb1        & 0xff;
	        int r2 = (rgb2 >> 16) & 0xff;
	        int g2 = (rgb2 >>  8) & 0xff;
	        int b2 =  rgb2        & 0xff;
	        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
	    }
	public int getNumberOfVariables() {
		return variables.length;
	}

	public int getNumberOfBehaviors() {
		return behavior.length;
	}
	
	public Gene[] getVariables() {
		return variables;
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

	public void mutate(Generator gen) {
		mutate(1.0 / variables.length,gen);
	}

	public void mutate(double mutationProbability, Generator gen) 
	{
		for (int i = 0; i < variables.length; i++) 
		{
			if (gen.rand.nextDouble() < mutationProbability) 
			{
				double relativeMutationAmount = mutationAmount * variables[i].getRange(); // Ahmed: To mutate wrt to the domain of the variable
				variables[i].update( relativeMutationAmount * gen.rand.nextDouble() - relativeMutationAmount / 2.0);
				//Ahmed: circular update, if more than high, values becomes low
				if (variables[i].getValue() > variables[i].getHigh()) 
				{
					variables[i].update(-variables[i].getRange());
				} 
				else if (variables[i].getValue() < variables[i].getLow())
				{
					variables[i].update(variables[i].getRange());
				}
			}
		}
		behavior = gen.getBehaviour(variables);
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
	public String toString() {
		return "Solution{Variables: " + Arrays.toString(variables) + "\nBehavior: " + Arrays.toString(behavior) + "\nNovelty: " + novelty + "}";
	}
}
