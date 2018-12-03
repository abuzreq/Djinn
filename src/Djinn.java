import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.opencv.core.Core;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;



/** READ ME

Toggle isGenerationOver to false if you want the novelty search to run,
if you only want to visualize the results of a previous run, 
then change @targetFolder to the folder that contains the results.
Each run can be made of a number of experiments equal to @NUM_EXPERIMENTS each of these will result in
a folder by the name: outN where N is the number of the experiment starting from 0.
All these outN folders are located in the indicated @targetFolder. 

The visualization reads the data inside the @targetFolder and shows it as a scatter plot. 
To move between the visualizations of each experiment just press any key

**/
public class Djinn extends PApplet 
{
	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		PApplet.main("Djinn");
	}
	public void settings() 
	{
		size(800, 800);
	}

	
	
	private boolean isGenerationOver = true;
	private boolean isVizPreparationOver = false;
	private final int NUM_EXPERIMENTS = 1;
	private File[] outputFolders;
	private Generator generator;
	String targetFolder = "OverDiffHOG";
	
	public void setup()
	{
		outputFolders = new File[NUM_EXPERIMENTS];
		for(int n = 0; n < NUM_EXPERIMENTS;n++)
		{
			File folder = new File(dataPath("") + "/"+targetFolder+"/out"+n+"/");
			outputFolders[n] = folder;
		}
		
		if(!isGenerationOver)
		{		
			//Novelty Search Parameters
			int populationSize = 50;
			int numNearestNeighbors = 15; 
			int numberOfIterations = 100;
			int tournamentSize = 3;
			double initialSparsenessThreshold = 10;
			Random rand = new Random(42);
			generator = new TimeGenerator(this,rand);
			for(int n = 0; n < NUM_EXPERIMENTS;n++)
			{						
				NoveltySearch search = new NoveltySearch(generator, populationSize,numberOfIterations, numNearestNeighbors,initialSparsenessThreshold, tournamentSize);
				search.run();
				Population result = search.getPopulation();
				saveResults(generator, result,outputFolders[n]);
				
				//TODO Here goes the code for what want to be changed between experiments e.g. numberOfIterations += 20
				System.out.println("Experiment "+(n + 1)+" Done");			
			}
			isGenerationOver = true;
		}
	}

	private ArrayList<ArrayList<Alternative>> data;
	private int currentExperiment = 0;
	
	int lastTime= 0 ,time = 0;
	public void draw()
	{
		time += millis()/1000;
		if(isGenerationOver && isVizPreparationOver != true)
		{
			//Get the stored results in setup() into a data collection @data to prepare for visualization
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
					data.get(i).add(new Alternative(paramsVector,image,this.width,this.height));
				}
			}
			isVizPreparationOver = true;
			currentExperiment = 0;
		}
		if(keyPressed && (time - lastTime) > 1000)
		{		
			lastTime = time;
			currentExperiment = (currentExperiment+1)%NUM_EXPERIMENTS;
		}
		if(isVizPreparationOver)
		{
			ArrayList<Alternative> alterantives = data.get(currentExperiment);
			
			background(250,250,250);
			//Ahmed: The outline of the points was chosen to be black and different from the fill color
			//so that it would be possible to distinguish points that are overlaying on each other.
			stroke(0);
			fill(255);
			for(int i = 0 ; i < alterantives.size();i++)
			{	
				alterantives.get(i).drawPoint(this);
			}			
			for(int i = 0 ; i < alterantives.size();i++)
			{	
				alterantives.get(i).drawImage(this);
			}
			fill(0);
			text(currentExperiment+"", 5,10);
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
	 * the imags are stored in this format [first gene value]-[second gene value].png 
	 * e.g. 204-001.png 
	 * note that only the digits after the decimal point are store (no integer values) see Gene.toString() for details
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
	}
	
	/**
	 *	A class to represent the View of an individual. 
	 */
	public class Alternative
	{
		private static final int DIM = 8; //Size of plot points
		private PVector param;
		private PImage image;
		private float x, y; // param x and y in window's coordinates
		/**
		 * Genotypes are assumed to have two genes only.____________
		 * 
		 * param: PVector with x as first gene value, y as second gene value________
		 * image: PImage to be shown when the mouse is hovered over the plot point at the passed x,y of @param
		 */
		Alternative(PVector param, PImage image, float width, float height)
		{
			this.param = param;
			this.image = image;
			x = PApplet.map(param.x, TimeGenerator.var1Low, TimeGenerator.var1High, 0, width);
			y = PApplet.map(param.y, TimeGenerator.var2Low, TimeGenerator.var2High, 0, height);
		}
		void drawPoint(PApplet ap)
		{
			ap.rect(x,y,DIM,DIM);			
		}
		void drawImage(PApplet ap)
		{
			if(ap.mouseX > x && ap.mouseX < x + DIM && ap.mouseY > y && ap.mouseY < y + DIM )
			{
				ap.pushMatrix();
				ap.translate(x,y);
				ap.scale(0.75f);
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
				fill(0);
				text(param.x + " : "+param.y,width-100,height-10);
			}
		}
	}
	
}
