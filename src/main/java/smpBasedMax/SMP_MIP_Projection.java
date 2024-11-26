package smpBasedMax;

import ij.ImagePlus;
import ij.ImageStack;


public class SMP_MIP_Projection {
    private  ImagePlus SMPz_map;
    private ImagePlus OriginalImage;
    private  int numberOfRows;
    private  int numberOfColumns;
    private  int numberOfSlices;
    private SMP_MIP_Projection.ZStackDirection zStackDirect;
    private int depth;
    private int[] max_z;
    private int[] min_z;

    public enum OutPutType {
        SMP_IMAGE, SMPZ_MAP
    }

    public enum ZStackDirection {
        OUT, IN
    }

    public SMP_MIP_Projection(ImagePlus OriginalImage,
                              ImagePlus SMPz_map,
                              int Depth,
                              SMP_MIP_Projection.ZStackDirection zStackDirection){
        this.OriginalImage = OriginalImage;
        this.SMPz_map = SMPz_map;
        this.zStackDirect = zStackDirection;
        this.numberOfRows = OriginalImage.getHeight();
        this.numberOfColumns = OriginalImage.getWidth();
        this.numberOfSlices = OriginalImage.getNSlices();
        this.depth = Depth;
        this.max_z = new int[numberOfRows*numberOfColumns];
        this.min_z = new int[numberOfRows*numberOfColumns];
    }

    public static ImageStack smp_mipProjection(ImagePlus originalImage,
                                               ImagePlus SMPz_map,
                                               int Depth,
                                               SMP_MIP_Projection.ZStackDirection zStackDirection,
                                               int[] max_z,
                                               int[] min_z,
                                               int numberOfSlices){
        ImageStack smp_mipImageStack = ImageStack.create(originalImage.getWidth(),
                originalImage.getHeight(),
                originalImage.getNSlices(),
                16);
        ImageStack originalImageStack = originalImage.getStack();
        setMinMaxZmap(SMPz_map, Depth, zStackDirection, max_z, min_z, numberOfSlices);
        for (int currentSlice = 1; currentSlice <= originalImage.getNSlices(); currentSlice++) {
            chooseSatisfiedPixels((short[])originalImageStack.getPixels(currentSlice),
                    (short[])smp_mipImageStack.getPixels(currentSlice),
                    max_z, min_z,zStackDirection, Depth, currentSlice);
        }
        return smp_mipImageStack;
    }

    public static void setMinMaxZmap(ImagePlus SMPz_map,
                                     int Depth,
                                     SMP_MIP_Projection.ZStackDirection zStackDirection,
                                     int[] max_z,
                                     int[] min_z,
                                     int numberOfSlices){
        short[] SMPz_mapValueArray = (short[]) SMPz_map.getProcessor().getPixels();
        if(zStackDirection == ZStackDirection.OUT){
            for (int i = 0; i < SMPz_mapValueArray.length; i++) {
                max_z[i] = SMPz_mapValueArray[i];
                int lowerBound = max_z[i] - Depth;
                if (lowerBound <= 1) {
                    min_z[i] = 1;
                } else {
                    min_z[i] = lowerBound;
                }
            }
        } else if (zStackDirection == ZStackDirection.IN) {
            for (int i = 0; i < SMPz_mapValueArray.length; i++) {
                min_z[i] = SMPz_mapValueArray[i];
                int upperBound = min_z[i] + Depth;
                if (upperBound >= numberOfSlices) {
                    max_z[i] = numberOfSlices;
                } else {
                    max_z[i] = upperBound;
                }
            }
        }
    }

    public static void chooseSatisfiedPixels (short[] originalPixelArray,
                                              short[] newPixelArray,
                                              int[] max_z,
                                              int[] min_z,
                                              ZStackDirection zStackDirection,
                                              int Depth,
                                              int currentSlice) {
        if (Depth >= 0){
            for (int i = 0; i < originalPixelArray.length; i++) {
                if (currentSlice >= min_z[i] && currentSlice <= max_z[i]){
                    newPixelArray[i] = originalPixelArray[i];
                } else {
                    newPixelArray[i] = Short.MIN_VALUE;
                }
            }
        } else {
            for (int i = 0; i < originalPixelArray.length; i++) {
                if (currentSlice >= max_z[i] && currentSlice <= min_z[i]){
                    newPixelArray[i] = originalPixelArray[i];
                } else {
                    newPixelArray[i] = Short.MIN_VALUE;
                }
            }
        }
    }


}
