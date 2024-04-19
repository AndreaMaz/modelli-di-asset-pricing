package it.univr.usefulmethodsarrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.numbers.core.Precision;
import org.jblas.util.Random;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;



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
	 * It returns the smallest element of a one-dimensional array of doubles
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
	 * @throws Exception if the two arrays have different lengths 
	 */
	public static double getMaxDifference(double[] firstArray, double[] secondArray) throws Exception {

		if (firstArray.length != secondArray.length) {
			throw new Exception();
		}

		return IntStream.range(0, firstArray.length)//the indices range from 0 to array.length-1
				.mapToDouble(i -> Math.abs(firstArray[i] - secondArray[i]))
				.max().getAsDouble();
	}

	/**
	 * It returns the index which maximizes the value of an arra, up to precision of two digits. In case of more elements
	 * of the array achieving the maximum, the smallest index is returned
	 * @param array
	 * @return the index which maximizes the value of an array
	 */
	public static int getMaxIndex(double[] array) {
		double maximum = getMax(array);

		int[] maximizingIndices = IntStream.range(0, array.length).filter(i -> Precision.round(array[i],4) == Precision.round(maximum,4)).toArray();
		return maximizingIndices[Random.nextInt(maximizingIndices.length)];
	}

	
	/**
	 * It returns the index which maximizes the value of an array. In case of more elements
	 * of the array achieving the maximum, index is chosen randomly
	 * @param array
	 * @return the index which maximizes the value of an array
	 */
	public static int getRandomMaximizingIndex(double[] array) {
		double maximum = getMax(array);

		int[] maximizingIndices = IntStream.range(0, array.length).filter(i -> Precision.round(array[i],4) == Precision.round(maximum,4)).toArray();
		return maximizingIndices[Random.nextInt(maximizingIndices.length)];
	}	
	
	
    /**
     * It returns the maximum element of a matrix
     * @param matrix
     * @return the maximum element of the matrix
     */
    public static double getMax(double[][] matrix) {
        double max = Double.NEGATIVE_INFINITY;
        for (double[] row : matrix) {
            for (double value : row) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }
	
    /**
     * It returns the indices that determine the maximum element of a matrix, up to the precision
     * of 4 digits. If more indices give the maximum, the method returns the one with smaller row
     * index, and in case the row index is the same, the one with smaller columns index. 
     * 
     * @param matrix
     * @return the maximum element of the matrix
     */
	public static int[] getMaximizingIndices(double[][] matrix) {
        // Find the maximum value in the matrix
        double maximum = getMax(matrix);

        // List to store indices of the maximum elements
        List<int[]> maximizingIndices = new ArrayList<>();

        // Iterate over the matrix to find all indices where the value equals maximum
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (Precision.round(matrix[i][j], 8) == Precision.round(maximum, 8)) {
                    maximizingIndices.add(new int[]{i, j});
                }
            }
        }
        return maximizingIndices.get(0);
    }

	/**
	 * It solves the linear system Ax=b
	 * @param matrix, A
	 * @param knownVector, b
	 * @return the solution x
	 */
	public static double[] solveLinearSystem(double[][] matrix, double[] knownVector) {

		//we use here the implementation of org.apache.commons.math3.linear
		RealMatrix newMatrix = MatrixUtils.createRealMatrix(matrix);
		DecompositionSolver solver = new LUDecomposition(newMatrix).getSolver();
		RealVector constants = new ArrayRealVector(knownVector, false);
		RealVector solution = solver.solve(constants);
		return solution.toArray();
	}
	
	
	/**
	 * It computes the difference between two matrices
	 * 
	 * @param firstMatrix
	 * @param secondMatrix
	 * @return the difference between firstMatrix and secondMatrix
	 * @throws Exception if the two matrices have different number of rows or columns
	 */
	public static double[][] differenceMatrices(double[][] firstMatrix, double[][] secondMatrix) throws Exception {
				
		if (firstMatrix.length != secondMatrix.length & firstMatrix[0].length != secondMatrix[0].length) {
			throw new Exception();
		}
		
		double[][] difference = new double[firstMatrix.length][firstMatrix[0].length];
		
		for (int rowIndex = 0; rowIndex <  firstMatrix.length; rowIndex ++) {
			for (int columnIndex = 0; columnIndex <  firstMatrix[0].length; columnIndex ++) {
				difference[rowIndex][columnIndex] = firstMatrix[rowIndex][columnIndex]-secondMatrix[rowIndex][columnIndex];
			}
		}
		return difference;
	}

	/**
	 * It computes and returns the average absolute value of the difference of two matrices
	 * @param firstMatrix
	 * @param secondMatrix
	 * @return the average absolute value of the difference of the two matrices
	 */
	public static double getAverageDifference(double[][] firstMatrix, double[][] secondMatrix) throws Exception {
		
		double[][] difference = differenceMatrices(firstMatrix, secondMatrix);
		
		double sum = 0; 
		
		for (int rowIndex = 0; rowIndex <  firstMatrix.length; rowIndex ++) {
			for (int columnIndex = 0; columnIndex <  firstMatrix[0].length; columnIndex ++) {
				sum += Math.abs(difference[rowIndex][columnIndex]);
			}
		}
		return sum/(firstMatrix.length*firstMatrix[0].length);
	}
	
	
	/**
	 * It computes and returns the maximum sum of the elements of the rows of the matrix which is the difference between
	 * the two matrices given in input, divided by the number of columns of the matrices.
	 * @param firstMatrix
	 * @param secondMatrix
	 * @return the maximum sum of the elements of the rows of the matrix which is the difference between
	 * 			the two matrices given in input, divided by the number of columns of the matrices
	 */
	public static double getNormDifference(double[][] firstMatrix, double[][] secondMatrix) throws Exception {

		RealMatrix newFirstMatrix = MatrixUtils.createRealMatrix(firstMatrix);
		RealMatrix newSecondMatrix = MatrixUtils.createRealMatrix(secondMatrix);

		int numberOfColumns = newFirstMatrix.getColumnDimension();

		return newFirstMatrix.subtract(newSecondMatrix).getNorm()/numberOfColumns;
	}
	
}
