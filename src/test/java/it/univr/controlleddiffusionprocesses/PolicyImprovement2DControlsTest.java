package it.univr.controlleddiffusionprocesses;

import java.text.DecimalFormat;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import net.finmath.util.TriFunction;

/**
 * This class tests the implementation of the Policy Improvement Algorithm to numerically solve the Merton problem, whose analytic solution
 * is computed in Example 4.20 of the script.
 * 
 * @author Andrea Mazzon
 *
 */
public class PolicyImprovement2DControlsTest {
	
	public static void main(String[] args) throws Exception {
		
		DecimalFormat formatterForValue = new DecimalFormat("0.000");
		
		/*
		 * Parameters to compute the optimal control and the number beta, see the script.
		 * They will be used also to define the functions for the SDE and for the rewards
		 */
		double interestRate = 0.2;
		double constantDrift = 0.3;
		double constantSigma = 0.25;
		
		double exponentForFinalRewardFunction = 0.5;		
		
		
		//functions for the SDE
		TriFunction<Double, Double, Double, Double> driftFunctionWithControl = (x,a,k) -> x*(a*(constantDrift-interestRate)+interestRate-k);
		TriFunction<Double, Double, Double, Double> diffusionFunctionWithControl = (x,a,k) -> x*a*constantSigma;
		
		//functions for the rewards
		TriFunction<Double, Double, Double, Double> runningRewardFunction = (x,a,k) -> Math.pow(x*k,exponentForFinalRewardFunction);
		DoubleUnaryOperator finalRewardFunction = x -> Math.pow(x,exponentForFinalRewardFunction);
		
		//function for the left border. In our case, the left border is zero
		DoubleBinaryOperator functionLeft = (t, x) -> 0.0;
		
		//definition of the intervals
		double leftEndSecondControlInterval = 0.0;
		double rightEndSecondControlInterval = 10;
		double controlStep = 0.1;
		
		//definition of the intervals
		double leftEndFirstControlInterval = 0.0;
		double rightEndFirstControlInterval = 6;
		
		double leftEndSpaceInterval = 0.0;
		double rightEndSpaceInterval = 10;
		double spaceStep = 0.1;
		
		double finalTime = 3.0;
		double timeStep = 0.1;
		
		double requiredPrecision = 0.1;
		int maxNumberIterations = 4;
		
		PolicyImprovement2DControls optimizer = new PolicyImprovement2DControls(driftFunctionWithControl, diffusionFunctionWithControl, runningRewardFunction,
				finalRewardFunction, functionLeft, leftEndFirstControlInterval,  rightEndFirstControlInterval,  controlStep,leftEndSecondControlInterval, 
				rightEndSecondControlInterval,  controlStep,  leftEndSpaceInterval, rightEndSpaceInterval,  spaceStep,  finalTime,  timeStep, requiredPrecision,
				maxNumberIterations);
		
		double A = (constantDrift-interestRate)*(constantDrift-interestRate)/(2*constantSigma*constantSigma)*exponentForFinalRewardFunction/(1-exponentForFinalRewardFunction)
				+interestRate*exponentForFinalRewardFunction;
				
		
		//we check the value function and the control at every combination of these time and space values
		double[] timeToCheck = {0.2, 0.4, 0.6, 0.8, 1.0};
		
		double[] spaceToCheck = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0}; 
		
		
		for (double time : timeToCheck) {
			for (double space : spaceToCheck) {
				
				System.out.println("Time: " + time + " Space: " + space);
				
				double valueFunction = Math.pow((1+(1-exponentForFinalRewardFunction)/A)*Math.exp(time*A/(1-exponentForFinalRewardFunction))-(1-exponentForFinalRewardFunction)/A
						,1-exponentForFinalRewardFunction)*Math.pow(space, exponentForFinalRewardFunction);
				
				System.out.println("Analytic value function " +  formatterForValue.format(valueFunction));

				double value = optimizer.getValueFunctionAtTimeAndSpace(time, space);
								
				System.out.println("Approximated value function " + formatterForValue.format(value));
				
				System.out.println();
			}
		}		
	}
}
