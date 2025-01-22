package smpBasedMax;


import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;


public class SMProjection {
    private final ImagePlus MIPz_map;
    private final ImagePlus originalImage;
    private final int numberOfRows;
    private final int numberOfColumns;
    private FloatProcessor envMax;
    private float[] envMaxzValues;
    private final int distance;
    private final int numberOfSlices;
    private final int radius;
    private final ZStackDirection zStackDirect;
    private int offSet;
    private MaxIntensityProjection projector;


    public SMProjection(ImagePlus originalImage,
                        ImagePlus MIPz_map,
                        int distance,
                        int radius,
                        ZStackDirection zStackDirection,
                        int offSet) {
        this.originalImage = originalImage;
        this.MIPz_map = MIPz_map;
        this.numberOfRows = MIPz_map.getHeight();
        this.numberOfColumns = MIPz_map.getWidth();
        this.envMax = new FloatProcessor(numberOfColumns, numberOfRows);
        this.distance = distance;
        this.numberOfSlices = originalImage.getNSlices();
        this.radius = radius;
        this.zStackDirect = zStackDirection;
        this.offSet = offSet;
    }

    public ImagePlus doSMProjection() {
        placeSmoothSheet();
        this.envMaxzValues = (float[])this.envMax.getPixels();
        ImageStack imageStackAfterApplySmoothSheet = smpProjection(this.originalImage,envMaxzValues, this.offSet);
        ImagePlus imageAfterApplySmoothSheet = new ImagePlus(this.originalImage.getTitle(), imageStackAfterApplySmoothSheet);
        this.projector = new MaxIntensityProjection(imageAfterApplySmoothSheet);
        return this.projector.doProjection();
    }

    public ImagePlus getSMPZmap(){
        return projector.getZmap();
    }

    public float[] getEnvMax(){return Arrays.copyOf(this.envMaxzValues, this.envMaxzValues.length);}

    public void placeSmoothSheet () {
        // get the references to the float array of ImageProcessor Object
        float[] envMaxzValues = (float[])this.envMax.getPixels();
        // get a copy of original value Array of ImagePlus Object and transform to float[]
        float[] MIPz_mapzValues = SMProjection.getCopyAndTransformToFloatOfValuesArrayOfImagePlusObject(this.MIPz_map);
        // Perform interpolation
        interpolateFloatArray(envMaxzValues,MIPz_mapzValues,
                this.zStackDirect,
                this.numberOfColumns,
                this.numberOfRows,
                this.distance,
                this.numberOfSlices);
        // Perform median filter
        RankFilters rf = new RankFilters();
        // Set the filter type to Median and the radius to radius, other parameter is set according to the default in RankFilter class
        rf.rank(this.envMax,this.radius,RankFilters.MEDIAN,0,50f,false,false);
    }

    public static ImageStack smpProjection (ImagePlus inputImage,
                                            float[] optimalSmoothSheet,
                                            int offSet){
        ImageStack smpImageStack = ImageStack.create(inputImage.getWidth(),
                inputImage.getHeight(),
                inputImage.getNSlices(),
                16);
        ImageStack originalImageStack = inputImage.getStack();
        int fuzziness = 1;
        float[] lowerBound = new float[optimalSmoothSheet.length];
        float[] upperBound = new float[optimalSmoothSheet.length];
        for (int i = 0; i < optimalSmoothSheet.length; i++) {
            lowerBound[i] = optimalSmoothSheet[i]-fuzziness+offSet;
            upperBound[i] = optimalSmoothSheet[i]+fuzziness+offSet;
        }
        for (int currentSlice = 1; currentSlice <= inputImage.getNSlices(); currentSlice++) {
            chooseQualifiedPixels((short[])originalImageStack.getPixels(currentSlice),
                    (short[])smpImageStack.getPixels(currentSlice),
                    lowerBound,
                    upperBound,
                    currentSlice);
        }
        return smpImageStack;
    }

    public static float[] getCopyAndTransformToFloatOfValuesArrayOfImagePlusObject(ImagePlus imp){
        ImageProcessor impIP = imp.getProcessor();
        short[] impValuesShort = (short[])impIP.getPixels();
        float[] impValuesFloat = new float[impValuesShort.length];
        for(int i=0; i<impValuesShort.length; i++) {
            impValuesFloat[i] = impValuesShort[i];
        }
        return impValuesFloat;
    }

    public static void interpolateFloatArray(float[] referenceToFloatArrayContainImageValues,
                                          float[] copyOfOriginalArrayValue,
                                          ZStackDirection zStackDirection,
                                          int numberOfColumns,
                                          int numberOfRows,
                                          int distance,
                                          int numberOfSlices) {
        // When z-stack direction out of the tissue
        if (zStackDirection == ZStackDirection.OUT) {
                float[] env1Up = new float[numberOfRows * numberOfColumns];
                float[] MIP_zmapzValuesTransposed = ConvertUtil.transpose1D(copyOfOriginalArrayValue, numberOfRows, numberOfColumns);
                float[] env2UpTransposed = new float[MIP_zmapzValuesTransposed.length];
                // Perform Interpolation for original array
                for (int i = 0; i < copyOfOriginalArrayValue.length; i += numberOfColumns) {
                    float[] interpolatedZvalues = Envelope.yUpper1D(Arrays.copyOfRange(copyOfOriginalArrayValue, i, i + numberOfColumns), distance);
                    System.arraycopy(interpolatedZvalues, 0, env1Up, i, numberOfColumns);
                }
                // Perform Interpolation for transposed original array
                for (int i = 0; i < MIP_zmapzValuesTransposed.length; i += numberOfRows) {
                    float[] interpolatedZvalues = Envelope.yUpper1D(Arrays.copyOfRange(MIP_zmapzValuesTransposed, i, i + numberOfRows), distance);
                    System.arraycopy(interpolatedZvalues, 0, env2UpTransposed, i, numberOfRows);
                }
                // transpose the env2UpTransposed to revert back to original size
                float[] env2Up = ConvertUtil.transpose1D(env2UpTransposed, numberOfColumns, numberOfRows);
                // remove outliers and round up
                roundUpRemoveOutliers(env1Up, env2Up, referenceToFloatArrayContainImageValues, numberOfSlices);
            }
        // When z-stack direction into the tissue
        else if (zStackDirection == ZStackDirection.IN) {
                float[] env1Low = new float[numberOfRows * numberOfColumns];
                float[] MIP_zmapzValuesTransposed = ConvertUtil.transpose1D(copyOfOriginalArrayValue, numberOfRows, numberOfColumns);
                float[] env2LowTransposed = new float[MIP_zmapzValuesTransposed.length];
                // Perform Interpolation for original array
                for (int i = 0; i < copyOfOriginalArrayValue.length; i += numberOfColumns) {
                    float[] interpolatedZvalues = Envelope.yLower1D(Arrays.copyOfRange(copyOfOriginalArrayValue, i, i + numberOfColumns), distance);
                    System.arraycopy(interpolatedZvalues, 0, env1Low, i, numberOfColumns);
                }
                // Perform Interpolation for transposed original array
                for (int i = 0; i < MIP_zmapzValuesTransposed.length; i += numberOfRows) {
                    float[] interpolatedZvalues = Envelope.yLower1D(Arrays.copyOfRange(MIP_zmapzValuesTransposed, i, i + numberOfRows), distance);
                    System.arraycopy(interpolatedZvalues, 0, env2LowTransposed, i, numberOfRows);
                }
                // transpose the env2LowTransposed to revert back to original size
                float[] env2Low = ConvertUtil.transpose1D(env2LowTransposed, numberOfColumns, numberOfRows);
                // remove outliers and round up
                roundUpRemoveOutliers(env1Low, env2Low, referenceToFloatArrayContainImageValues, numberOfSlices);
            }
        }

    public static void roundUpRemoveOutliers (float[] floatArray1,
                                              float[] floatArray2,
                                              float[] referenceToResultArray,
                                              int numberOfSlices) {
        for (int i = 0; i < floatArray1.length; i++) {
            referenceToResultArray[i] = Math.round(Math.max(floatArray1[i], floatArray2[i]));
            if (referenceToResultArray[i] > numberOfSlices) {
                referenceToResultArray[i] = numberOfSlices;
            } else if (referenceToResultArray[i] < 1) {
                referenceToResultArray[i] = 1;
            }
        }
    }

    public static void chooseQualifiedPixels(short[] originalPixelArray,
                                             short[] newPixelArray,
                                             float[] lowerBoundArray,
                                             float[] upperBoundArray,
                                             int currentSlice) {
        for (int i = 0; i < lowerBoundArray.length; i++) {
            if (lowerBoundArray[i] <= currentSlice && upperBoundArray[i] >= currentSlice) {
                newPixelArray[i] = originalPixelArray[i];
            } else {
                newPixelArray[i] = 0;
            }
        }
    }

}
















