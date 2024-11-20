package smpBasedMax;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.interpolation.HermiteInterpolator;

public class Envelope {
    public static float[] yUpper1D (float[] signal, int distance){
        int[] peakIdx = SmpBasedMaxUtil.findPeak(signal, distance);
        float[] peakValues = ConvertUtil.extractElementsByIndices(signal, peakIdx);
        return splineInterpolate(peakIdx,peakValues,signal.length);
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
}
