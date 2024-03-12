package it.univr.controlledmarkovchains.unknownenvironment;

import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;

public abstract class QLearningInheritance extends TemporalDifferenceLearning {

	public QLearningInheritance(double[] states, double[] rewardsAtStates, int numberOfEpisodes, double learningRate,
			double explorationParameter) {
		super(states, rewardsAtStates, numberOfEpisodes, learningRate, explorationParameter);
	}
	
	
	protected double getUpdate(int newStateIndex) {
		
		
		int[] possibleNewActionsIndices = computePossibleActionsIndices(newStateIndex);
		
		double[][] qValue = getQValue();
		
		double[]  valuesForPossibleActions = new double[possibleNewActionsIndices.length];
		
		for (int indexForNewStates = 0; indexForNewStates< valuesForPossibleActions.length; indexForNewStates ++ ) {
			valuesForPossibleActions[indexForNewStates] = qValue[newStateIndex][possibleNewActionsIndices[indexForNewStates]];
		}
		
		double maximumForGivenStateIndex = UsefulMethodsForArrays.getMax(valuesForPossibleActions);		
		
		return maximumForGivenStateIndex;	
	}

}
