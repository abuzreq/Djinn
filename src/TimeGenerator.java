import java.awt.Color;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import Jama.Matrix;
import processing.core.PApplet;
import processing.core.PImage;

public class TimeGenerator extends Generator {

	private Color[][][] data;
	private int[][] mask;
	private int WIDTH = 344, HEIGHT = 359, NUM_FRAMES = 78;

	TimeGenerator(PApplet ap, Random rand) {
		super(ap, rand);

		data = new Color[NUM_FRAMES][WIDTH][HEIGHT];
		mask = new int[WIDTH][HEIGHT];
		String dir = ap.dataPath("") + "/frames/";
		File imagesDir = new File(dir);
		String[] files = imagesDir.list();
		for (int k = 0; k < files.length; k++) {
			PImage img = ap.loadImage(dir + files[k]);
			// img.loadPixels();
			for (int i = 0; i < WIDTH; i++) {
				for (int j = 0; j < HEIGHT; j++) {
					data[k][i][j] = new Color(img.get(i, j));// pixels[i*WIDTH+j];
				}
			}
		}
	}

	@Override
	public int getNumGenes() {
		return 2;
	}

	@Override
	public int getNumBehaviours() {
		return WIDTH * HEIGHT;
	}

	@Override
	public double[] getBehaviour(Gene[] variables) {
		double falloff = variables[0].getValue();
		double increment = variables[1].getValue();
		PImage alterantive = genotypeToPhenotype(falloff, increment);
		// alterantive.filter(GRAY);
		// return copyFromIntArray(alterantive.pixels);

		return features(alterantive);
	}

	@Override
	public Gene[] generateGenotype() {
		return new Gene[] { new Gene(rand, 0.1, 0.5), new Gene(rand, 0, 0.04) };
	}

	@Override
	public PImage genotypeToPhenotype(Gene[] vars) {
		return genotypeToPhenotype(vars[0].getValue(), vars[1].getValue());
	}

	private PImage genotypeToPhenotype(double falloff, double increment) {
		ap.noiseDetail(16, (float) falloff);
		float xoff = 0;
		for (int x = 0; x < WIDTH; x++) {
			xoff += increment; // Increment xoff
			float yoff = 0.0f; // For every xoff, start yoff at 0
			for (int y = 0; y < HEIGHT; y++) {
				yoff += increment; // Increment yoff

				// Calculate noise and scale by 2 for more discernible effect
				int v = (int) (ap.noise(xoff, yoff) * 2 * NUM_FRAMES) % NUM_FRAMES;
				mask[x][y] = v;
			}
		}
		return sample(mask);
	}

	private PImage sample(int[][] mask) {
		Color[][] arr = new Color[WIDTH][HEIGHT];
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				arr[i][j] = data[mask[i][j]][i][j];
			}
		}
		PImage img = ap.createImage(WIDTH, HEIGHT, ap.GRAY);
		img.loadPixels();
		// println(img.pixels.length);
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				img.set(i, j, colorToInt(arr[i][j]));
				// img.pixels[j*WIDTH+i] = arr[i][j];
			}
		}
		img.updatePixels();
		return img;
	}

	private Size _winSize = new Size(64, 128);
	private Size _blockSize = new Size(16, 16);
	private Size _blockStride = new Size(8, 8);
	private Size _cellSize = new Size(8, 8);
	private HOGDescriptor hog = new HOGDescriptor(_winSize, _blockSize, _blockStride, _cellSize, 9);

	private double[] features(PImage img) {
		MatOfFloat descriptors = new MatOfFloat();

		/*
		 * Mat mat = new Mat(img.width,img.height, CvType.CV_8SC3); mat.put(0,
		 * 0, img.pixels);
		 */
		Mat mat = toMat(img);
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);

		Imgproc.resize(mat, mat, new Size(64, 128));
		hog.compute(mat, descriptors);
		// System.out.println(descriptors.height() + " * "+descriptors.width());
		return copyFromFloatArray(descriptors.toArray());
	}

	private Mat toMat(PImage image) {
		int w = image.width;
		int h = image.height;

		Mat mat = new Mat(h, w, CvType.CV_8UC4);
		byte[] data8 = new byte[w * h * 4];
		int[] data32 = new int[w * h];
		PApplet.arrayCopy(image.pixels, data32);

		ByteBuffer bBuf = ByteBuffer.allocate(w * h * 4);
		IntBuffer iBuf = bBuf.asIntBuffer();
		iBuf.put(data32);
		bBuf.get(data8);
		mat.put(0, 0, data8);

		return mat;
	}

	/************* Utils ******************************/

	private double[] copyFromIntArray(int[] source) {
		double[] dest = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}

	private double[] copyFromFloatArray(float[] source) {
		double[] dest = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}

	private int colorToInt(Color color) {
		return color.getRGB();
	}

	private double sum(double[] arr) {
		float sum = 0;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
		}
		return sum;
	}

}
