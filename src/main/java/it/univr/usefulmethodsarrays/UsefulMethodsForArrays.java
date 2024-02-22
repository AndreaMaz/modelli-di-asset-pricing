package it.univr.usefulmethodsarrays;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.numbers.core.Precision;
import org.jblas.util.Random;

public class UsefulMethodsForArrays {


	/**
	 * It returns the biggest element of a one-dimensional array of doubles
	 *
	 * @param vector the one-dimensional array
	 * @return the biggest element of the one-dimensional array
	 */
	public static double getMax(double[] array) {
		double maximum = Arrays.stream(array).max().getAsDouble();
		return maximum;
	}


	/**
	 * It returns the biggest element of a one-dimensional array of doubles
	 *
	 * @param vector the one-dimensional array
	 * @return the smallest element of the one-dimensional array
	 */
	public static double getMin(double[] array) {
		double minimum = Arrays.stream(array).min().getAsDouble();
		return minimum;
	}
	
	/**
	 * It computes and returns the maximum absolute difference between two arrays
	 * @param firstArray
	 * @param secondArray
	 * @return the maximum absolute difference between firstArray and secondArray
	 */
	public static double getMaxDifference(double[] firstArray, double[] secondArray) {
		return IntStream.range(0, firstArray.length)//the indices range from 0 to array.length-1
				.mapToDouble(i -> Math.abs(firstArray[i] - secondArray[i]))
				.max().getAsDouble();
	}

	/**
	 * It returns the index which maximizes the value of an array. In case of more elements
	 * of the array achieving the maximum, the first index is returned
	 * @param array
	 * @return the index which maximizes the value of an array
	 */
	public static int getMaxIndex(double[] array) {
		return IntStream.range(0, array.length)//the indices range from 0 to array.length-1
				.reduce((i, j) -> Precision.round(array[i],4) >= Precision.round(array[j],4) ? i : j)
				.getAsInt();
	}
	
	
	/**
	 * It returns the index which maximizes the value of an array. In case of more elements
	 * of the array achieving the maximum, index is chosen randomly
	 * @param array
	 * @return the index which maximizes the value of an array
	 */
	public static int getRandomMaximizingIndex(double[] array) {
		double maximum = getMax(array);

        int[] maximizingIndices = IntStream.range(0, array.length).filter(i -> array[i] == maximum).toArray();
        return maximizingIndices[Random.nextInt(maximizingIndices.length)];
        
	}	
}
