package smpBasedMax;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.interpolation.HermiteInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Envelope {
    public static float[] yUpper1D (float[] signal, int distance){
        int[] peakIdx = findPeak(signal, distance);
        float[] peakValues = ConvertUtil.extractElementsByIndices(signal, peakIdx);
        return splineInterpolate(peakIdx,peakValues,signal.length);
    }

    public static float[] yLower1D (float[] signal, int distance){
        int[] troughIdx = findTrough(signal, distance);
        float[] troughValues = ConvertUtil.extractElementsByIndices(signal, troughIdx);
        return splineInterpolate(troughIdx,troughValues,signal.length);
    }

    /**
     * perform Polynomial Interpolation base on the peaks of the signal
     * @param x integer array of peak indices in the original signal array
     * @param y float array of peak values
     * @param numberOfDataPoints Number of elements in the original signal array
     * @return new signal array that has been smoothed based on the given peaks
     */
    public static float[] splineInterpolate(int[] x, float[] y, int numberOfDataPoints){
        // Convert int[] to double[] and float[] to double[]
        double[] xDouble = new double[x.length];
        double[] yDouble = new double[y.length];
        for (int i = 0; i < x.length; i++) {
            xDouble[i] = (double) x[i];
            yDouble[i] = (double) y[i];
        }
        // Create the SplineInterpolator
        HermiteInterpolator interpolator = new HermiteInterpolator();
        for (int i = 0; i < xDouble.length; i++) {
            interpolator.addSamplePoint(xDouble[i],new double[]{yDouble[i]});
        }
        // get the polynomial function
        PolynomialFunction[] polynomials = interpolator.getPolynomials();
        PolynomialFunction polynomial = polynomials[0];

        // Compute the value for all the dataPoints
        double[] xFinalDouble = new double[numberOfDataPoints];
        for (int i = 0; i < numberOfDataPoints; i++) {
            xFinalDouble[i] = polynomial.value(i);
        }
        return ConvertUtil.convertToPrimitiveFloat(xFinalDouble);
    }

    /**
     * Find the indices of localMaxima in 1D array of pixels
     * @param x The float array of pixels
     * @param distance the distance in integer that peak indices are separated by at least
     * @return the indices of peaks that satisfied the distance condition
     */
    public static int[] findPeak(float[] x, int distance){
        Result localMax = findLocalMaxima(x);
        int[] peakIdx = localMax.midpoints;
        int[] leftEdges = localMax.leftEdges;
        int[] rightEdges = localMax.rightEdges;
        float[] peakValues = ConvertUtil.extractElementsByIndices(x, peakIdx);
        boolean[] satisfiedArray = selectPeakByDistance(peakIdx,peakValues,distance);
        int counterTrue = 0;
        for (int i = 0; i < satisfiedArray.length; i++) {
            if (satisfiedArray[i]) {
                counterTrue++;
            }
        }
        int[] peakIdxSatisfiedDistance = new int[counterTrue];
        int pointer = 0;
        for (int i = 0; i < satisfiedArray.length; i++) {
            if (satisfiedArray[i]) {
                peakIdxSatisfiedDistance[pointer] = peakIdx[i];
                pointer++;
            }
        }
        return peakIdxSatisfiedDistance;
    }

    /**
     * Find the indices of localMinima in 1D array of z-levels
     * @param x The float array of z-levels
     * @param distance the distance in integer that trough indices are separated by at least
     * @return the indices of troughs that satisfied the distance condition
     */
    public static int[] findTrough(float[] x, int distance){
        Result localMin = findLocalMinima(x);
        int[] troughIdx = localMin.midpoints;
        int[] leftEdges = localMin.leftEdges;
        int[] rightEdges = localMin.rightEdges;
        float[] troughValues = ConvertUtil.extractElementsByIndices(x, troughIdx);
        boolean[] satisfiedArray = selectTroughByDistance(troughIdx,troughValues,distance);
        int counterTrue = 0;
        for (int i = 0; i < satisfiedArray.length; i++) {
            if (satisfiedArray[i]) {
                counterTrue++;
            }
        }
        int[] troughIdxSatisfiedDistance = new int[counterTrue];
        int pointer = 0;
        for (int i = 0; i < satisfiedArray.length; i++) {
            if (satisfiedArray[i]) {
                troughIdxSatisfiedDistance[pointer] = troughIdx[i];
                pointer++;
            }
        }
        return troughIdxSatisfiedDistance;
    }

    /**
     * Finds local maxima in a 1D array.
     * A maxima are defined as one or more consecutive values that are surrounded by smaller values.
     *
     * @param x The array to search for local maxima.
     * @return A Result object containing indices of midpoints, left edges, and right edges of local maxima.
     */
    public static Result findLocalMaxima(float[] x) {
        int size = x.length;
        int maxSize = size / 2; // There can't be more maxima than half the size of the array

        // Preallocate arrays to store potential maxima information
        ArrayList<Integer> midpoints = new ArrayList<>(maxSize);
        ArrayList<Integer> leftEdges = new ArrayList<>(maxSize);
        ArrayList<Integer> rightEdges = new ArrayList<>(maxSize);

        int i = 1;          // Start at second element
        int iMax = size - 1; // Last element can't be a maxima

        while (i < iMax) {
            // Check if the previous element is smaller
            if (x[i - 1] < x[i]) {
                int iAhead = i + 1;

                // Find the next element that is not equal to x[i]
                while (iAhead < iMax && x[iAhead] == x[i]) {
                    iAhead++;
                }

                // Check if the next unequal element is smaller
                if (x[iAhead] < x[i]) {
                    // Store the indices
                    leftEdges.add(i);
                    rightEdges.add(iAhead - 1);
                    midpoints.add((i + (iAhead - 1)) / 2);

                    // Skip samples that can't be maxima
                    i = iAhead;
                }
            }
            i++;
        }

        // Convert lists to arrays
        int[] midpointsArray = midpoints.stream().mapToInt(Integer::intValue).toArray();
        int[] leftEdgesArray = leftEdges.stream().mapToInt(Integer::intValue).toArray();
        int[] rightEdgesArray = rightEdges.stream().mapToInt(Integer::intValue).toArray();

        return new Result(midpointsArray, leftEdgesArray, rightEdgesArray);
    }

    /**
     * Finds local minima in a 1D array.
     * A minima are defined as one or more consecutive values that are surrounded by larger values.
     *
     * @param x The array to search for local minima.
     * @return A Result object containing indices of midpoints, left edges, and right edges of local minima.
     */
    public static Result findLocalMinima(float[] x) {
        int size = x.length;
        int maxSize = size / 2; // There can't be more minima than half the size of the array

        // Preallocate arrays to store potential maxima information
        ArrayList<Integer> midpoints = new ArrayList<>(maxSize);
        ArrayList<Integer> leftEdges = new ArrayList<>(maxSize);
        ArrayList<Integer> rightEdges = new ArrayList<>(maxSize);

        int i = 1;          // Start at second element
        int iMax = size - 1; // Last element can't be a maxima

        while (i < iMax) {
            // Check if the previous element is smaller
            if (x[i - 1] > x[i]) {
                int iAhead = i + 1;

                // Find the next element that is not equal to x[i]
                while (iAhead < iMax && x[iAhead] == x[i]) {
                    iAhead++;
                }

                // Check if the next unequal element is larger
                if (x[iAhead] > x[i]) {
                    // Store the indices
                    leftEdges.add(i);
                    rightEdges.add(iAhead - 1);
                    midpoints.add((i + (iAhead - 1)) / 2);

                    // Skip samples that can't be minima
                    i = iAhead;
                }
            }
            i++;
        }
        // Convert lists to arrays
        int[] midpointsArray = midpoints.stream().mapToInt(Integer::intValue).toArray();
        int[] leftEdgesArray = leftEdges.stream().mapToInt(Integer::intValue).toArray();
        int[] rightEdgesArray = rightEdges.stream().mapToInt(Integer::intValue).toArray();

        return new Result(midpointsArray, leftEdgesArray, rightEdgesArray);
    }

    /**
     * A simple data structure to hold the result of the function findLocalMaxima
     */
    public static class Result {
        public final int[] midpoints;
        public final int[] leftEdges;
        public final int[] rightEdges;

        public Result(int[] midpoints, int[] leftEdges, int[] rightEdges) {
            this.midpoints = midpoints;
            this.leftEdges = leftEdges;
            this.rightEdges = rightEdges;
        }
    }

    /**
     * Helper function to select peaks based on a minimum distance requirement.
     * <p>
     * This method processes an array of peak indices (`peakIdx`) and their corresponding
     * values (`peakValue`) to determine which peaks should be kept or discarded. The
     * decision is based on a specified minimum distance between peaks. Peaks with a higher
     * value are given priority when there are conflicts, keeping them over lower-value peaks
     * if they fall within the specified distance.
     * </p>
     *
     * @param peakIdx    An array of integers representing the indices of detected peaks in a 1D signal.
     * @param peakValue  A float array representing the values or priorities of each corresponding peak.
     *                   Peaks with higher values have a higher priority when considering conflicts.
     * @param distance   An integer specifying the minimum distance required between peaks.
     *                   Peaks that are closer than this distance will be evaluated based on priority.
     * @return A boolean array of the same length as `peakIdx` where each entry indicates whether
     *         the corresponding peak should be kept (`true`) or discarded (`false`).
     */
    public static boolean[] selectPeakByDistance (int[] peakIdx, float[] peakValue, int distance){
        int peakIdxLength = peakIdx.length;
        boolean[] keep = new boolean[peakIdxLength];
        Arrays.fill(keep, true);

        int[] priorityToPosition = sortReturnPeakIndices(peakValue);
        // Highest priority first -> iterate in reverse order (decreasing)
        for (int i = peakIdxLength - 1; i >= 0; i--) {
            // Translate `i` to `j`
            int j = priorityToPosition[i];
            if (!keep[j]) {
                continue;  // Skip evaluation for peak already marked as "don't keep"
            }

            // Flag earlier peaks for removal until minimal distance is exceeded
            int k = j - 1;
            while (k >= 0 && peakIdx[j] - peakIdx[k] <= distance) {
                keep[k] = false;
                k--;
            }

            // Flag later peaks for removal until minimal distance is exceeded
            k = j + 1;
            while (k < peakIdxLength && peakIdx[k] - peakIdx[j] <= distance) {
                keep[k] = false;
                k++;
            }
        }
        return keep;
    }

    /**
     * Helper function to select troughs based on a minimum distance requirement.
     * <p>
     * This method processes an array of trough indices (`troughIdx`) and their corresponding
     * values (`troughValue`) to determine which trough should be kept or discarded. The
     * decision is based on a specified minimum distance between troughs. Troughs with a lower
     * value are given priority when there are conflicts, keeping them over higher-value trough
     * if they fall within the specified distance.
     * </p>
     *
     * @param troughIdx    An array of integers representing the indices of detected troughs in a 1D signal.
     * @param troughValue  A float array representing the values or priorities of each corresponding trough.
     *                   Troughs with lower values have a higher priority when considering conflicts.
     * @param distance   An integer specifying the minimum distance required between troughs.
     *                   Troughs that are closer than this distance will be evaluated based on priority.
     * @return A boolean array of the same length as `troughIdx` where each entry indicates whether
     *         the corresponding trough should be kept (`true`) or discarded (`false`).
     */
    public static boolean[] selectTroughByDistance (int[] troughIdx, float[] troughValue, int distance){
        int troughIdxLength = troughIdx.length;
        boolean[] keep = new boolean[troughIdxLength];
        Arrays.fill(keep, true);

        int[] priorityToPosition = sortReturnPeakIndices(troughValue);
        // Highest priority first -> iterate in forward order (increasing)
        for (int i = 0; i <= troughIdxLength - 1; i++) {
            // Translate `i` to `j`
            int j = priorityToPosition[i];
            if (!keep[j]) {
                continue;  // Skip evaluation for trough already marked as "don't keep"
            }

            // Flag earlier troughs for removal until minimal distance is exceeded
            int k = j - 1;
            while (k >= 0 && troughIdx[j] - troughIdx[k] <= distance) {
                keep[k] = false;
                k--;
            }

            // Flag later troughs for removal until minimal distance is exceeded
            k = j + 1;
            while (k < troughIdxLength && troughIdx[k] - troughIdx[j] <= distance) {
                keep[k] = false;
                k++;
            }
        }
        return keep;
    }
    /**
     * Sorts an array of peak or trough values and returns the original indices in the new sorted order.
     *
     * @param peakValues The array of peak values to be sorted
     * @return int[] An array of indices that reflects the sorted order of the peak or trough values
     */
    public static int[] sortReturnPeakIndices (float[] peakValues){
        Integer[] indices = new Integer[peakValues.length];
        for (int i = 0; i < peakValues.length; i++) {
            indices[i] = i;
        }
        // Sort indices based on the values in the float array
        Arrays.sort(indices, Comparator.comparingDouble(i -> peakValues[i]));

        return ConvertUtil.convertToPrimitiveInt(indices);
    }

}
