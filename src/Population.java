
import java.util.ArrayList;

public class Population extends ArrayList<Individual> {
	
	private int numberOfVariables;
	private int numberOfBehaviors;
	private int size;
	private Generator generator;
	/*
	public Population(int size, int numberOfVariables, int numberOfBehaviors){
		super();
		this.numberOfVariables = numberOfVariables;
		this.numberOfBehaviors = numberOfBehaviors;
		for(int i=0; i<size; i++){
			add(new Solution(numberOfVariables, numberOfBehaviors));
		}
	}
	*/
	public Population(Generator generator,int size)
	{
		super();
		this.size = size;
		this.generator = generator;
		this.numberOfVariables = generator.getNumGenes();		
		this.numberOfBehaviors = generator.getNumBehaviours();
		for(int i=0; i<size; i++)
		{
			add(new Individual(generator));
		}
	}
	public int getNumberOfVariables(){
		return numberOfVariables;
	}
	
	public int getNumberOfBehaviors(){
		return numberOfBehaviors;
	}

	/*
	public Population clone()
	{
		Population pop = new Population(generator, size);
		pop.numberOfVariables = this.numberOfVariables;
		pop.numberOfBehaviors = this.numberOfBehaviors;
		for(int i =0 ; i < this.size();i++)
		{
			pop.add(this.get(i).clone());
		}
		return pop;
		
	}
	*/
}
