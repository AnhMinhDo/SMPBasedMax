package smpBasedMax;


import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.RankFilters;

import java.util.Arrays;


public class SMPProjection {
    private ImagePlus MIPz_map;
    private FloatProcessor SMPz_map;
    private int numberOfRows;
    private int numberOfColumns;
    private FloatProcessor envMax;
    private int distance;
    private int numberOfSlices;
    private int radius;


    enum OutPutType {
        SMP_IMAGE,
        SMPZ_MAP
    }

    public SMPProjection(ImagePlus MIPz_map, int distance, int numberOfSlices, int radius) {
        this.MIPz_map = MIPz_map;
        this.numberOfRows = MIPz_map.getHeight();
        this.numberOfColumns = MIPz_map.getWidth();
        this.envMax = new FloatProcessor(numberOfColumns, numberOfRows);
        this.distance = distance;
        this.numberOfSlices = numberOfSlices;
        this.radius = radius;
    }

    public ImagePlus getMIPz_map(String title) {
        this.placeSmoothSheet();
        int width = this.MIPz_map.getWidth();
        int height = this.MIPz_map.getHeight();
        float[] pixels = (float[])this.envMax.getPixels();
        ImageProcessor oip;
        // Create output image consistent w/ type of input image.
        int size = pixels.length;
        oip = this.MIPz_map.getProcessor().createProcessor(width,height);
        short[] pixels16 = (short[])oip.getPixels();
        for(int i=0; i<size; i++) {
            pixels16[i] = (short) (pixels[i] + 0.5f);
        }
        // Adjust for display.
        // Calling this on non-ByteProcessors ensures image
        // processor is set up to correctly display image.
        oip.resetMinAndMax();
        return new ImagePlus(title, oip);
    }

    public void placeSmoothSheet () {
        // get the references to the float array of ImageProcessor Object
        float[] env1Up = new float[numberOfRows * numberOfColumns];
        float[] env2Up = new float[numberOfRows * numberOfColumns];
        float[] envMaxzValues = (float[])this.envMax.getPixels();
        // get a copy of original ImagePlus Object
        ImageProcessor MIPz_mapIP = this.MIPz_map.getProcessor();
        short[] MIPz_mapzValuesShort = (short[])MIPz_mapIP.getPixels();
        float[] MIPz_mapzValues = new float[MIPz_mapzValuesShort.length];
        for(int i=0; i<MIPz_mapzValuesShort.length; i++) {
            MIPz_mapzValues[i] = MIPz_mapzValuesShort[i];
        }
        // Transpose the original float array
        float[] MIP_zmapzValuesTransposed = ConvertUtil.transpose1D(MIPz_mapzValues, numberOfRows, numberOfColumns);
        float[] env2UpTransposed = new float[MIP_zmapzValuesTransposed.length];
        // Perform Interpolation for original array
        for (int i = 0; i < MIPz_mapzValues.length; i+= numberOfColumns) {
            float[] interpolatedZvalues = Envelope.yUpper1D(Arrays.copyOfRange(MIPz_mapzValues,i,i+numberOfColumns),this.distance);
            System.arraycopy(interpolatedZvalues,0,env1Up,i,numberOfColumns);
        }
        // Perform Interpolation for transposed original array
        for (int i = 0; i < MIP_zmapzValuesTransposed.length; i+= numberOfRows) {
            float[] interpolatedZvalues = Envelope.yUpper1D(Arrays.copyOfRange(MIP_zmapzValuesTransposed,i,i+numberOfRows),this.distance);
            System.arraycopy(interpolatedZvalues,0,env2UpTransposed,i,numberOfRows);
        }
        // transpose the env2UpTransposed to revert back to original size
        float[] env2UpTransposedTransposed = ConvertUtil.transpose1D(env2UpTransposed, numberOfColumns, numberOfRows);
        System.arraycopy(env2UpTransposedTransposed, 0, env2Up, 0, env2UpTransposedTransposed.length);
        // remove outliers and round up
        roundUpRemoveOutliers(env1Up,env2Up,envMaxzValues,numberOfSlices);
        // Perform median filter
        RankFilters rf = new RankFilters();
        // Set the filter type to Median and the radius to radius, other parameter is set according to the default in RankFilter class
        rf.rank(this.envMax,this.radius,RankFilters.MEDIAN,0,50f,false,false);
    }

    public static void roundUpRemoveOutliers (float[] floatArray1, float[] floatArray2, float[] referenceToResultArray, int numberOfSlices) {
        for (int i = 0; i < floatArray1.length; i++) {
            referenceToResultArray[i] = Math.round(Math.max(floatArray1[i], floatArray2[i]));
            if (referenceToResultArray[i] > numberOfSlices) {
                referenceToResultArray[i] = numberOfSlices;
            } else if (referenceToResultArray[i] < 1) {
                referenceToResultArray[i] = 1;
            }
        }
    }


}
