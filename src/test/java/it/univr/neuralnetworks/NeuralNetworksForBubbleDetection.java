package it.univr.neuralnetworks;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;


import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import it.univr.cevprices.CevPrices;

/**
 * This class uses the libraries deeplearning4j and nd4j in order to create and train a neural network which has to learn if
 * an underlying process is a strict local martingale (i.e., if it has a financial bubble) or a true martingale (i.e., it has
 * no bubble) based on the call prices it generates. 
 * In particular, here we consider processes X^i=(X^i_t)_{t >= 0} following the dynamics
 *   
 * dX^i_t = (X_i+d_i)^(beta_i),  (1)
 * X_0 = x_0,
 * 
 * where d_i and beta_i are random variables, uniformly distributed in [0,0.2] and [0.5, 1.5], respectively. These are displaced CEV
 * models, and they are strict local martingales if and only if beta_i>1. In this example, we have sigma = 1.0 and x_0=2.
 * 
 * The call prices generated by this model can be computed analytically, and we do that in the class it.univr.cevprices.CevPrices.
 * We take 15 equally-spaced strikes between 1.5 and 3.0, and 20 equally-spaced maturities from 1.0 to 2.0.  
 * 
 * With these data, we train a neural network with 2 hidden layers of 60 and 20 nodes, respectively, giving it prices generated
 * in half cases by true martingale and in half cases by strict local martingales. We computing the weights and the biases via the
 * Adam algorithm with learning rate equal to 0.001 and 10 epochs. 
 * 
 * We then test it for prices generated by processes following (1) with other displacements. 
 * 
 * @author Andrea Mazzon
 *
 */
public class NeuralNetworksForBubbleDetection {


	public static void main(String[] args) {

		//we use it to print the bubble probabilities
		DecimalFormat formatter = new DecimalFormat("0.00");
		
		//we use it to generate the random displacements d_i and exponents beta_i 
		Random randomGenerator = new Random();

		//we train the network with prices generated by 10000 underlyings; 5000 strict local martingales, 5000 martingales
		int numberOfStrictLocalAndTrueMartingalesPerModelTraining = 5000;
		
		//we then make it predict the nature (martingale or strict local martingale) of 20 underlyings, based on the price they generate
		int numberOfStrictLocalAndTrueMartingalesPerModelTesting = 10;

		double finalTime = 2.0;
		double initialValue = 2.0;

		//we generate the strikes..
		double smallestStrike = 1.5;
		double biggestStrike = 3.0;
		double strikeStep = 0.1;
		double[] strikes = DoubleStream.iterate(smallestStrike, s -> s <= biggestStrike, s -> s + strikeStep).toArray();

		//..and the maturities
		double smallestMaturity = 1.0;
		double biggestMaturity = finalTime;
		double maturityStep = 0.05;
		double[] maturities = DoubleStream.iterate(smallestMaturity, s -> s <= biggestMaturity, s -> s + maturityStep).toArray();

		int numberOfStrikes = strikes.length;
		int numberOfMaturities = maturities.length;

		//the interval where we generate the random exponents for the true martingales 
		double exponentSmallTrueMartingale = 0.5;
		double exponentBigTrueMartingale = 1.0;

		double exponentsRangeTrueMartingale = exponentBigTrueMartingale - exponentSmallTrueMartingale;

		//the interval where we generate the random exponents for the strict local martingales 
		double exponentSmallStrictLocalMartingale = 1.1;
		double exponentBigStrictLocalMartingale = 1.5;

		double exponentsRangeStrictLocalMartingale = exponentBigStrictLocalMartingale - exponentSmallStrictLocalMartingale;

		//the interval where we generate the random displcaments for both true martingales and strict local martingales
		double displacementSmall = 0;
		double displacementBig = 0.2;
		double sigma = 1;

		//rows are prices generated by a given underlying, for all strikes and maturities
		double[][] pricesForTraining = new double[2*numberOfStrictLocalAndTrueMartingalesPerModelTraining][numberOfStrikes*numberOfMaturities];

		/*
		 * They will be 1 for strict local martingales, 0 for martingales. So we hope that the network "learns the rule" that associates
		 * prices to these labels. We define it as a matrix with only one columns to make it compatible with the output of the network 
		 */
		double[][] labelsForTraining = new double[2*numberOfStrictLocalAndTrueMartingalesPerModelTraining][1];

		//we fill the matrix
		for (int trainingDataIndex = 0; trainingDataIndex < numberOfStrictLocalAndTrueMartingalesPerModelTraining; trainingDataIndex++) {
			
			//generations of exponents in the intervals of true martingales and strict local martingales, respectively
			double exponentTrueMartingale = exponentSmallTrueMartingale + exponentsRangeTrueMartingale*randomGenerator.nextDouble();
			double exponentStrictLocalMartingale = exponentSmallStrictLocalMartingale + exponentsRangeStrictLocalMartingale*randomGenerator.nextDouble();

			//generations of displacements for the true martingale and the strict local martingale, respectively (same interval, different generation)
			double displacementTrueMartingale = displacementSmall + displacementBig*randomGenerator.nextDouble();
			double displacementStrictLocalMartingale = displacementSmall + displacementBig*randomGenerator.nextDouble();

			//we compute the prices for the given exponents and displacements, and all combinations of strikes and maturities 
			for (int maturityIndex = 0; maturityIndex < numberOfMaturities; maturityIndex ++) {
				for (int strikeIndex = 0; strikeIndex < numberOfStrikes; strikeIndex ++) {
					
					
					double callPriceForTrueMartingale = CevPrices.CEVPriceCallForExponentSmallerEqualOne(initialValue + displacementTrueMartingale, sigma,
							exponentTrueMartingale, maturities[maturityIndex], strikes[strikeIndex] + displacementTrueMartingale);
					
					double callPriceForStrictLocalMartingale = CevPrices.CEVPriceCallForExponentBiggerThanOne(initialValue + displacementStrictLocalMartingale, sigma,
							exponentStrictLocalMartingale, maturities[maturityIndex], strikes[strikeIndex] + displacementStrictLocalMartingale);

					/*
					 * We place them in two consecutive rows: so rows with even indices have the prices generated by the true martingales whereas rows with
					 * odd indices have prices generated by strict local martingales. 
					 */
					pricesForTraining[2*trainingDataIndex][maturityIndex * numberOfStrikes + strikeIndex] =  callPriceForTrueMartingale;
					pricesForTraining[2*trainingDataIndex + 1][maturityIndex * numberOfStrikes + strikeIndex] =  callPriceForStrictLocalMartingale;

				}
			}
			/*
			 * We place them in two consecutive rows: so rows with even indices have label 0 (they are true martingales) whereas rows with odd indices
			 * have label 1 (they are strict local martingales). 
			 */
			labelsForTraining[2*trainingDataIndex][0] = 0;
			labelsForTraining[2*trainingDataIndex + 1][0] = 1;
		}

		//we transform the two matrices in objects that can be used to create a DataSet to train the network that we construct below
		INDArray pricesDataForTraining = Nd4j.create(pricesForTraining);
		INDArray labelsDataForTraining = Nd4j.create(labelsForTraining);

		//this is the DataSet to train the network that we construct below
		DataSet trainingData = new DataSet(pricesDataForTraining, labelsDataForTraining);
		
		double learningRate = 0.001;
		MultiLayerConfiguration layersConstruction = new NeuralNetConfiguration.Builder()
				.seed(1897) // we need a seed because the weights, the biases and the mini-matvhes are selected randomly
				.updater(new Adam(learningRate)) //we specify how we want to update the optimal coefficients
				.list() //now we want to specify the layers after the first one, which is simply given by the inputs.
				//the following is the first hidden layer:
				.layer(new DenseLayer.Builder()
						.nIn(numberOfMaturities * numberOfStrikes)//this is the number of inputs it receives
						.nOut(60) //it has 60 nodes (nOut can be thought as the number of nodes)
						.activation(Activation.RELU) //it has ReLU activation function
						.build()) 
				//the following is the second hidden layer:
				.layer(new DenseLayer.Builder()
						.nIn(60)//this is the number of inputs it receives: 60, as the nodes of the first hidden layer
						.nOut(20)//it has 20 nodes (nOut can be thought as the number of nodes)
						.activation(Activation.RELU) //it has ReLU activation function
						.build())
				//the following is the output layer:
				.layer(new OutputLayer.Builder()
						.lossFunction(LossFunction.XENT)//XENT is the cross-entropy loss function: often used for classification
						/*
						 * It has sigmoid activation function: it means that it outputs a number that we can interpret as the
						 * probability that the input prices are generated by a strict local martingale
						 */
						.activation(Activation.SIGMOID)
						.nIn(20)//this is the number of inputs it receives: 20, as the nodes of the first hidden layer
						.nOut(1)//this is the number of nodes: 1, as there is only one output
						.build())
				.build();

		//we construct a neural network out of these layers..
		MultiLayerNetwork network = new MultiLayerNetwork(layersConstruction);
		//..and we make it ready to be trained
		network.init();

		//we then train it iteratively, for any epoch
        int numberOfEpochs = 30; // Specify the number of epochs
        for (int i = 0; i < numberOfEpochs; i++) {
        	network.fit(trainingData);
        }
		
        /*
         * Now we generate the prices to give to the trained network to give a probability that the underlyings that have generated them are
         * strict local martingales instead of true martingales
         */
        double[][] pricesForTesting = new double[2*numberOfStrictLocalAndTrueMartingalesPerModelTesting][numberOfStrikes*numberOfMaturities];
		
        /*
		 * They will be 1 for strict local martingales, 0 for martingales. So we hope that the network "learns the rule" that associates
		 * prices to these labels. We define it as a matrix with only one columns to make it compatible with the output of the network 
		 */
		double[][] labelsForTesting = new double[2*numberOfStrictLocalAndTrueMartingalesPerModelTesting][1];
        
        
        //we fill the matrix of the prices for the testing
		for (int testingDataIndex = 0; testingDataIndex < numberOfStrictLocalAndTrueMartingalesPerModelTesting; testingDataIndex++) {
			
			//generations of exponents in the intervals of true martingales and strict local martingales, respectively
			double exponentTrueMartingale = exponentSmallTrueMartingale + exponentsRangeTrueMartingale*randomGenerator.nextDouble();
			double exponentStrictLocalMartingale = exponentSmallStrictLocalMartingale + exponentsRangeStrictLocalMartingale*randomGenerator.nextDouble();

			//generations of displacements for the true martingale and the strict local martingale, respectively (same interval, different generation)
			double displacementTrueMartingale = displacementSmall + displacementBig*randomGenerator.nextDouble();
			double displacementStrictLocalMartingale = displacementSmall + displacementBig*randomGenerator.nextDouble();

			//we compute the prices for the given exponents and displacements, and all combinations of strikes and maturities
			for (int maturityIndex = 0; maturityIndex < numberOfMaturities; maturityIndex ++) {
				for (int strikeIndex = 0; strikeIndex < numberOfStrikes; strikeIndex ++) {
					double callPriceForTrueMartingale = CevPrices.CEVPriceCallForExponentSmallerEqualOne(initialValue + displacementTrueMartingale, sigma,
							exponentTrueMartingale, maturities[maturityIndex], strikes[strikeIndex] + displacementTrueMartingale);
					double callPriceForStrictLocalMartingale = CevPrices.CEVPriceCallForExponentBiggerThanOne(initialValue + displacementStrictLocalMartingale, sigma,
							exponentStrictLocalMartingale, maturities[maturityIndex], strikes[strikeIndex] + displacementStrictLocalMartingale);

					/*
					 * We place them in two consecutive rows: so rows with even indices have the prices generated by the true martingales whereas rows with
					 * odd indices have prices generated by strict local martingales. 
					 */
					pricesForTesting[2*testingDataIndex][maturityIndex * numberOfStrikes + strikeIndex] =  callPriceForTrueMartingale;
					pricesForTesting[2*testingDataIndex + 1][maturityIndex * numberOfStrikes + strikeIndex] =  callPriceForStrictLocalMartingale;
				}
			}
			labelsForTesting[2*testingDataIndex][0] = 0;
			labelsForTesting[2*testingDataIndex + 1][0] = 1;
		}	
		
		//we trasnform the matrix in an object that can be given to the trained network to produce its predictions
		INDArray pricesDataForTesting = Nd4j.create(pricesForTesting);
		
		//the true labels: we want to compare them with the probabilities given by the network
		INDArray labelsDataForTesting = Nd4j.create(labelsForTesting);

		DataSet testingData = new DataSet(pricesDataForTesting, labelsDataForTesting);
		
		//we print the score for both the training and testing set: the closer to 1, the better
		System.out.println("Score for training:");
		System.out.println(formatter.format(1-network.score(trainingData)));

		System.out.println();
		
		System.out.println("Score for testing:");
		System.out.println(formatter.format(1-network.score(testingData)));

		System.out.println();
		
		//we get the the predictions of the network..
		INDArray predictionsAsINDArray = network.output(pricesDataForTesting);
		
		//and we transform them into a double array
		double[] predictions = predictionsAsINDArray.toDoubleVector();
		
		System.out.println("Probabilities outputs that the underlyings are strict local martingales when they are true martingales:");
		
		for (int indexForTrueMartingales = 0; indexForTrueMartingales<numberOfStrictLocalAndTrueMartingalesPerModelTesting; indexForTrueMartingales++) {
			System.out.println(formatter.format(predictions[2*indexForTrueMartingales]));
		}
		    
		System.out.println();
		
		System.out.println("Probabilities outputs that the underlyings are strict local martingales when they are strict local martingales:");

		for (int indexForStrictLocalMartingales = 0; indexForStrictLocalMartingales<numberOfStrictLocalAndTrueMartingalesPerModelTesting; indexForStrictLocalMartingales++) {
			System.out.println(formatter.format(predictions[2*indexForStrictLocalMartingales+1]));
		}
		
	}

}
