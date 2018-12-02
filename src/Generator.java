import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;

public abstract class Generator
{
	public Random rand;
	public PApplet ap = null;
	Generator(PApplet pApplet, Random rand)
	{
		this.ap = pApplet;
		this.rand = rand;
	}
	public abstract double[] getBehaviour(Gene[] variables);
	public abstract int getNumBehaviours();	
	public abstract int getNumGenes();
	public abstract Gene[] generateGenotype();
	public abstract PImage genotypeToPhenotype(Gene[] vars);

}
