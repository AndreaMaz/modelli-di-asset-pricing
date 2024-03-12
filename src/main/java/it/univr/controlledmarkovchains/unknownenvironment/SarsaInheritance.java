package it.univr.controlledmarkovchains.unknownenvironment;

import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;

public abstract class SarsaInheritance extends TemporalDifferenceLearning {

	public SarsaInheritance(double[] states, double[] rewardsAtStates, int numberOfEpisodes, double learningRate,
			double explorationParameter) {
		super(states, rewardsAtStates, numberOfEpisodes, learningRate, explorationParameter);
	}
	
	
	protected double getUpdate(int newStateIndex) {
			
		double[][] qValue = getQValue();
		
		int chosenActionIndex;
		
		if (generator.nextDouble()< getExplorationParameter()){
			int[] possibleActionsIndices = computePossibleActionsIndices(newStateIndex);
			chosenActionIndex = possibleActionsIndices[generator.nextInt(possibleActionsIndices.length)];
		} else {
			chosenActionIndex = UsefulMethodsForArrays.getMaxIndex(qValue[newStateIndex]);
		}

		
		return qValue[newStateIndex][chosenActionIndex];	
	}

}
