package it.univr.controlledmarkovchains.valueiteration;


import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import net.finmath.plots.Named;
import net.finmath.plots.Plot2D;

/**
 * This class tests the implementation of ValueIteration and its derived class CoinGamblerProblem.
 * 
 * @author Andrea Mazzon
 *
 */
public class CoinGamblerProblemTest {
	
	public static void main(String[] args) {
		
		
		double requiredPrecision = 1e-9;

		double headProbability = 0.4;
		
		int moneyToWin = 100;
		
		double discountFactor = 1.0;//no discount
		
		ValueIteration problemSolver = new CoinGamblerProblem(discountFactor, requiredPrecision, headProbability, moneyToWin);

		double[] valueFunctions = problemSolver.getValueFunctions();
		
		final Plot2D plotValueFunctions = new Plot2D(1, moneyToWin-1, moneyToWin-1, Arrays.asList(
				new Named<DoubleUnaryOperator>("Value function", x -> valueFunctions[(int) x])));
		
		plotValueFunctions.setXAxisLabel("State");
		plotValueFunctions.setYAxisLabel("Value function");
		
		plotValueFunctions.show();
		
		double[] optimalActions = problemSolver.getOptimalActions();
			
		
		final Plot2D plotOptimalPolicy = new Plot2D(1, moneyToWin-1, moneyToWin-1, Arrays.asList(
				new Named<DoubleUnaryOperator>("Optimal action", x -> optimalActions[(int) x])));
		
		plotOptimalPolicy.setXAxisLabel("State");
		plotOptimalPolicy.setYAxisLabel("Optimal money investment on head");
		
		plotOptimalPolicy.show();
		
	}

}
