package it.univr.controlledmarkovchains.valueiteration;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.IntStream;

/**
 * The main contribution of this class is to provide the solution of the gambler problem when the probability of getting head
 * is known. It does it by extending the class ValueIteration, providing the implementation of its abstract methods.
 * 
 * @author Andrea Mazzon
 *
 */
public class CoinGamblerProblem extends ValueIteration {

	
	private double headProbability;
	
	private int moneyToWin;
	
	
	/**
	 * It constructs an object to compute the solution of the gambler problem with known head probability.
	 * 
	 * @param discountFactor: the discount factor gamma in the notes
	 * @param requiredPrecision,he iterations stop when the absolute value of the difference between the new and past
	 * 		  values of the value function is smaller than requiredPrecision for all the entries
	 * @param headProbability, the probability to get head
	 * @param moneyToWin, the amount that the capital process must hit in order for the gambler to win the bet
	 */
	public CoinGamblerProblem(double discountFactor, double requiredPrecision, double headProbability, int moneyToWin) {
		super( IntStream.range(0, moneyToWin+1).asDoubleStream().toArray(), //the vector (0,1,2,...,moneyToWin)
				//the vector (0,0,0,...,0,1)
				IntStream.concat(IntStream.generate(() -> 0).limit(moneyToWin), IntStream.of(1) ).asDoubleStream().toArray(), 
				new int[] {0, moneyToWin}, //the absorbing states
				discountFactor, 
				(x,y) -> 0, //the running reward: zero in our case
				requiredPrecision);
		this.headProbability = headProbability;
		this.moneyToWin = moneyToWin;
	}

	
	@Override
	protected double[] computeActions(double state) {
		/*
		 * Possible actions are (1,2,..,n) where n is the minimum between the capital (we cannot go negative) and the capital
		 * needed to reach moneyToWin (it does not make sense to invest more). We write +1 because the second number in range
		 * is exclusive
		 */
        int[] actions = IntStream.range(1, (int) (Math.min(state, moneyToWin - state) + 1)).toArray();
        
        //we have to convert them to double, as actions doubles for a general problem
		return Arrays.stream(actions).mapToDouble(x -> (double) x).toArray();
	}

	@Override
	protected double[] computeExpectedReturnsForStateAndActions(double state, double[] actions) {
		
		DoubleBinaryOperator runningRewardFunction = getRunningRewardFunction();
		
		double[] oldStateValues = getOldValuesFunctions();

		double[] actionReturns = new double[actions.length];
        for (int actionIndex = 0; actionIndex < actions.length; actionIndex ++ ) {
        	actionReturns[actionIndex]=runningRewardFunction.applyAsDouble(state, actions[actionIndex]) + //the running reward (deterministic)
        			//the expected value at the next step given the chosen action and the current state
        			headProbability * oldStateValues[(int) (state + actions[actionIndex])]
        				 + (1 - headProbability) * oldStateValues[(int) (state - actions[actionIndex])];      	
        }
		return actionReturns;
	}	

}
