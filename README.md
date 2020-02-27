# Djinn
An experiment on exploring the generative space of a visual arts generator using Novelty Search.

## Generative Art
![Noisy GIF](https://github.com/abuzreq/Djinn/blob/master/time.gif)

Inspired by a [https://gfycat.com/concerneddecisivecockatoo](gif of an aging woman), I wanted to find a way to juxtapose the images from different ages together. In Noisy GIF, I stack the gif's frames as a 3D matrix (movie?) then sample accross the matrix using a greyscale 2D Perlin Noise image. This was favoured over using an RNG because Perlin noise created contigious chunks of light/dark regions which made it possible to have just enough of each age. Moving the mouse around directly manipulates two parameters of the Perlin noise.

## Searching for what is 'interesting'
![Noisy GIF](https://github.com/abuzreq/Djinn/blob/master/djinn.gif)

When looking at the Noisy GIF project above, you can explore its generative space by moving the mouse around looking for something 'interesting'. I was interested in automating/semi-automating this process. An optimization algorithm fails in this case because it is hard to quantify interestingness. Djinn aims at supporting the process of exploring the space of a visual art generator by sampling it through Novelty Search. The purpose is to find as many distinct/novel points in that space as possible. This is done by comparing any newly generated piece against others based on their visual features (I am using Histogram of Oriented Gradients (HOG)).

## Setup:
1) Open Eclipse IDE
2) Create a project named Djinn
3) Copy the contents of this reposotitry into that folder
4) Follow the intructions here: 
https://docs.opencv.org/2.4/doc/tutorials/introduction/java_eclipse/java_eclipse.html
the opencv Jar and native library folder are located in the project under 
Djinn\lib\opencv\opencv-343.jar
and 
Djinn\lib\opencv\x64 
respectively
5) Read the top note in the file Djinn.java


