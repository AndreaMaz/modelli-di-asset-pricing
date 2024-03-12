package it.univr.controlledmarkovchains.unknownenvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;

public abstract class TemporalDifferenceLearning {

	private double[] rewardsAtStates;


	private double[] optimalValues;
	private int[] optimalPolicyIndices;

	private double[][] qValue;

	private int numberOfStates;

	private int numberOfEpisodes;	
	private double learningRate;
	private double explorationParameter;


	protected Random generator = new Random();


	private ArrayList<List<Double>> updatedOptimalValues = new ArrayList<List<Double>>();

	public TemporalDifferenceLearning(double[] states, double[] rewardsAtStates, int numberOfEpisodes, double learningRate, double explorationParameter) {
		this.rewardsAtStates = rewardsAtStates;
		numberOfStates = states.length;
		this.numberOfEpisodes = numberOfEpisodes; 
		this.learningRate = learningRate;
		this.explorationParameter = explorationParameter;
	}


	private void generateOptimalValueAndPolicy() {

		int numberOfActions = getNumberOfActions();

		optimalValues = new double[numberOfStates - 1];
		optimalPolicyIndices = new int[numberOfStates - 1];
		
		qValue = new double[numberOfStates][numberOfActions];
		
		for (int columnIndex = 0; columnIndex < numberOfActions; columnIndex ++) {
			for (int rowIndex = 0; rowIndex < numberOfStates; rowIndex ++) {
				qValue[rowIndex][columnIndex]=rewardsAtStates[rowIndex];
			}
		}


		for (int episodeIndex = 0; episodeIndex < numberOfEpisodes; episodeIndex ++) {

			int stateIndex = generator.nextInt(numberOfStates-2)+1;
			
			int chosenActionIndex;

			while (true) {

				if (generator.nextDouble()< explorationParameter){
					int[] possibleActionsIndices = computePossibleActionsIndices(stateIndex);
					chosenActionIndex = possibleActionsIndices[generator.nextInt(possibleActionsIndices.length)];
				} else {
					chosenActionIndex = UsefulMethodsForArrays.getMaxIndex(qValue[stateIndex]);
				}

				int newStateIndex = generateStateIndex(stateIndex, chosenActionIndex);
				
				
				if (checkIfEnded(newStateIndex)) {
					qValue[stateIndex][chosenActionIndex] = qValue[stateIndex][chosenActionIndex] +
							learningRate * (rewardsAtStates[newStateIndex]-qValue[stateIndex][chosenActionIndex]) ;
					break;
				}
				
				double update = getUpdate(newStateIndex);
				//System.out.println(update);
				
				
				qValue[stateIndex][chosenActionIndex] = qValue[stateIndex][chosenActionIndex] +
						learningRate * (update-qValue[stateIndex][chosenActionIndex]) ;
								
				
				stateIndex = newStateIndex;
			}
		}
		
		
		for (int stateIndexAtTheEnd = 1; stateIndexAtTheEnd < numberOfStates; stateIndexAtTheEnd ++) {
			optimalValues[stateIndexAtTheEnd - 1] = UsefulMethodsForArrays.getMax(qValue[stateIndexAtTheEnd]);
			optimalPolicyIndices[stateIndexAtTheEnd - 1] = UsefulMethodsForArrays.getMaxIndex(qValue[stateIndexAtTheEnd]);
		}
	}
	
	
	public double[] getOptimalValues() {
		if (qValue == null) {
			generateOptimalValueAndPolicy();
		}
		return optimalValues.clone();
	}

	public int[] getOptimalPolicy() {
		if (optimalValues == null) {
			generateOptimalValueAndPolicy();
		}
		return optimalPolicyIndices.clone();
	}


	public ArrayList<List<Double>> getUpdatedOptimalValues() {
		if (optimalValues == null) {
			generateOptimalValueAndPolicy();
		}
		return updatedOptimalValues;
	}
	
	
	protected double[][] getQValue(){
		if (qValue == null) {
			generateOptimalValueAndPolicy();
		}
		return qValue.clone();
	}
	
	
	protected double getExplorationParameter() {
		return explorationParameter;
	}
	
	protected abstract double getUpdate(int stateIndex);

	protected abstract int getNumberOfActions();

	protected abstract int[] computePossibleActionsIndices(int stateIndex);

	protected abstract int generateStateIndex(int oldStateIndex, int actionIndex); 

	
	protected abstract boolean checkIfEnded(int stateIndex);


}
