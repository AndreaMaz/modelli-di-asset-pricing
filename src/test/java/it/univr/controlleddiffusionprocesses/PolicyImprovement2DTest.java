package it.univr.controlleddiffusionprocesses;

import java.util.function.DoubleBinaryOperator;

import net.finmath.util.TriFunction;

/**
 * This class tests the implementation of the Policy Improvement Algorithm to numerically solve the Merton problem, whose analytic solution
 * is computed in Example 4.20 of the script.
 * 
 * @author Andrea Mazzon
 *
 */
public class PolicyImprovement2DTest {
	
	public static void main(String[] args) throws Exception {
		
		/*
		 * Parameters to compute the optimal control and the number beta, see the script.
		 * They will be used also to define the functions for the SDE and for the rewards
		 */
		double constantSigma = 0.25;
		double kappa = 0.3;
		double theta = 0.5;
		
		TriFunction<Double, Double, Double, Double> firstDriftFunctionWithControl = (q,S,a) -> -a;		
		TriFunction<Double, Double, Double, Double> secondDriftFunctionWithControl = (q,S,a) -> 0.0;
		TriFunction<Double, Double, Double, Double> firstDiffusionFunctionWithControl = (q,S,a) -> 0.0;
		TriFunction<Double, Double, Double, Double> secondDiffusionFunctionWithControl = (q,S,a) -> constantSigma;
		
		TriFunction<Double, Double, Double, Double> runningRewardFunction = (q,S,a) -> (S-kappa*a)*a;
		DoubleBinaryOperator finalRewardFunction = (q,S) -> q*S - theta * q*q;
		
		//definition of the intervals
		double leftEndControlInterval = 0.0;
		double rightEndControlInterval = 6;
		double controlStep = 0.1;
		
		double leftEndFirstSpaceInterval = 0;
		double rightEndFirstSpaceInterval = 4;
		double firstSpaceStep = 0.1;
		
		double leftEndSecondSpaceInterval = 0;
		double rightEndSecondSpaceInterval = 5;
		double secondSpaceStep = 0.1;
		
		double finalTime = 1.0;
		double timeStep = 0.005;
		
		TriFunction<Double, Double, Double, Double> valueFunction = (t, q, S) -> q*S + q*q*(-1/(1/theta+1/kappa*t));

		double time = 1.0;
		double q = 0.5;
		double S = 0.6;
		
		System.out.println("Value function analytic " +valueFunction.apply(time, q, S) );
		
		int maxNumberIterations = 5;
		
		PolicyImprovement2D optimizer = new PolicyImprovement2D(firstDriftFunctionWithControl, secondDriftFunctionWithControl,
				firstDiffusionFunctionWithControl, secondDiffusionFunctionWithControl,
				runningRewardFunction, finalRewardFunction,   leftEndControlInterval,  rightEndControlInterval,
				 controlStep,  leftEndFirstSpaceInterval,  rightEndFirstSpaceInterval,  firstSpaceStep,  leftEndSecondSpaceInterval, rightEndSecondSpaceInterval, 
				 secondSpaceStep,  finalTime,  timeStep,  maxNumberIterations);
		
		
		System.out.println("Computed value function " + optimizer.getValueFunctionAtTimeAndSpace(time, q, S));
		
		
	}
}
