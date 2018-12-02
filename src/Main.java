import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.opencv.core.Core;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Main extends PApplet 
{
	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		PApplet.main("Main");
	}
	public void settings() 
	{
		size(600, 600);
	}

	private boolean isGenerationOver = false;
	private boolean isVizPreparationOver = false;
	private final int NUM_EXPERIMENTS = 3;
	private File[] outputFolders;
	private Generator generator;
	
	public void setup()
	{
		outputFolders = new File[NUM_EXPERIMENTS];
		for(int n = 0; n < NUM_EXPERIMENTS;n++)
		{
			File folder = new File(dataPath("") + "/out"+n+"/");
			outputFolders[n] = folder;
		}
		
		if(!isGenerationOver)
		{
			
			//Novelty Search Parameters
			int populationSize = 30;
			int numNearestNeighbors = 1; 
			int numberOfIterations = 20;
			int tournamentSize = 1;
			double initialSparsenessThreshold = 0.05;
			Random rand = new Random(42);
			generator = new TimeGenerator(this,rand);
			for(int n = 0; n < NUM_EXPERIMENTS;n++)
			{						
				NoveltySearch search = new NoveltySearch(generator, populationSize,numberOfIterations, numNearestNeighbors,initialSparsenessThreshold, tournamentSize);
				search.run();
				Population result = search.getPopulation();
				saveResults(generator, result,outputFolders[n]);
				//Here goes the code for what should change between experiments
				//TODO
				numNearestNeighbors += 2;
				System.out.println("Experiment "+(n + 1)+" Done");

			}
			isGenerationOver = true;
		}

	}

	private ArrayList<ArrayList<Alternative>> data;
	private int currentExperiment = 0;
	public void draw()
	{
		if(isGenerationOver && isVizPreparationOver != true)
		{
			//Get the stored results in setup() into a data collection to prepare for visualization
			data = new ArrayList<ArrayList<Alternative>>(NUM_EXPERIMENTS);
			for(int i = 0; i < outputFolders.length;i++)
			{
				data.add(new ArrayList<Alternative>());
				File[] alternatives = outputFolders[i].listFiles();
				for(int f = 0; f < alternatives.length;f++)
				{
					String[] params = alternatives[f].getName().split("\\.")[0].split("-");			
					PVector paramsVector = new PVector(toFLoat(params[0]),toFLoat(params[1]));				
					PImage image = loadImage(alternatives[f].getAbsolutePath());
					data.get(i).add(new Alternative(paramsVector,image));
				}
			}
			isVizPreparationOver = true;
			currentExperiment = 0;
		}
		if(keyPressed )
		{		
			currentExperiment = (currentExperiment+1)%NUM_EXPERIMENTS;
		}
		if(isVizPreparationOver)
		{
			clear();
			stroke(255);
			fill(255);
			ArrayList<Alternative> alterantives = data.get(currentExperiment);
			for(int i = 0 ; i < alterantives.size();i++)
			{	
				alterantives.get(i).draw(this);
			}
			text(currentExperiment+"", 50,50);
		}
		
	}
	
	/************************************ Utilities *************************************/						

	
	/**
	 * 
	 * @param str
	 * @return a float version of the passed string, @str is assumed to be in an integer form e.g. 452, 014 to which the result is 0.452 and 0.014
	 */
	private float toFLoat(String str)
	{	
		float f = Float.parseFloat(str)/((int)Math.pow(10,str.length()));
		return f;
	}
	
	private void deleteFolder(File folder) 
	{
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

	/**
	 * 
	 * @param gen
	 * @param population
	 * @param folder
	 * For every individual in @population , generates its phenotype/image and stores it in @folder. 
	 * Disclaimer: The folder contents are deleted if it already exists
	 */
	private void saveResults(Generator gen, Population population, File folder) 
	{
		//Clears the folder first
		deleteFolder(folder);
		for (Individual sol : population) 
		{
			Gene[] genes = sol.getGenes();
			String name = genes[0] + "-" + genes[1];
			gen.genotypeToPhenotype(genes).save(folder.getAbsolutePath() + "/"+ name + ".png");
		}
		/*
		//TODO Infinity?
		for (Individual sol : population) 
		{
			System.out.print(sol.getNovelty() + " ");
		}
		System.out.println();
		*/
	}
	
	/**
	 *	A class to represent the View of an individual. 
	 */
	public class Alternative
	{
		private static final int DIM = 5; //Size of plot points
		private PVector param;
		private PImage image;
		
		/**
		 * Genotypes are assumed to have two genes only.____________
		 * 
		 * param: PVector with x as first gene value, y as second gene value________
		 * image: PImage to be shown when the mouse is hovered over the plot point at the passed x,y of @param
		 */
		Alternative(PVector param, PImage image)
		{
			this.param = param;
			this.image = image;
		}
		void draw(PApplet ap)
		{
			//Hard coded ranges of each gene are mapped to [0,width or height] range
			float x = PApplet.map(param.x, 0.1f, 0.5f, 0, ap.width)    ;
			float y = PApplet.map(param.y, 0.0f, 0.04f, 0,  ap.height);
			ap.rect(x,y,DIM,DIM);
			if(ap.mouseX > x && ap.mouseX < x + DIM && ap.mouseY > y && ap.mouseY < y + DIM )
			{
				ap.pushMatrix();
				ap.translate(x,y);
				ap.scale(0.5f);
				float ix = 0,iy = 0;
				if(x + image.width/2 > ap.width)
				{
					ix -= image.width;
				}
				if(y + image.height/2 > ap.height)
				{
					iy -= image.height;
				}
				ap.image(image,ix,iy);
				ap.popMatrix();
				text(param.x + " : "+param.y,width-100,height-10);
			}
		}
	}
	
}
