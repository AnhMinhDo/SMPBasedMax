package smpBasedMax;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.analysis.interpolation.HermiteInterpolator;

public class Envelope {

    public static float[] yUpper (float[] signal, int np){
        float[] y = new float[signal.length];
        return y;
    };

    public static float[] SplineInterpolate(int[] x, float[] y, int numberOfDataPoints){
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
        PolynomialFunction functionPoly = interpolator.getPolynomials()[];
        // Compute the value for all the dataPoints
        double[] xFinalDouble = new double[xDouble.length];
        for (int i = 0; i < xDouble.length; i++) {
            xFinalDouble[i] = interpolator.value(i);
        }
        // Convert xFinalDouble from double[] to float[]
        float[] xReturn = new float[xFinalDouble.length];
        for (int i = 0; i < xReturn.length; i++) {
            xReturn[i] = (float) xFinalDouble[i];
        }
        return xReturn;
    }

}
