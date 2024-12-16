package smpBasedMax;

import ij.ImagePlus;
import ij.ImageStack;

public class ConvertUtil {

    /**
     * Converts a timeSeries to  Stack, remove the time Dimension
     * @param hyperstack ImagePlus object of timeSeries
     * @return ImagePlus
     */
    public static ImagePlus convertTimeSeriesToStack(ImagePlus hyperstack) {
        // Get dimensions
        int nChannels = hyperstack.getNChannels();
        int nSlices = hyperstack.getNSlices();
        int nFrames = hyperstack.getNFrames();
        // Create a new stack to combine slices
        ImageStack combinedStack = new ImageStack(hyperstack.getWidth(), hyperstack.getHeight());

        // Iterate over time frames
        for (int t = 1; t <= nFrames; t++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int z = 1; z <= nSlices; z++) {
                    // Extract the slice for each (channel, slice, frame)
                    int index = hyperstack.getStackIndex(c, z, t);
                    combinedStack.addSlice(hyperstack.getStack().getProcessor(index));
                }
            }
        }
        // Create a new ImagePlus for the combined stack
        return new ImagePlus(hyperstack.getTitle(), combinedStack);
    }

    /**
     * Converts an Integer[] array to int[] array
     * @param integers The Integer array
     * @return int[] The primitive array
     */
    public static int[] convertToPrimitiveInt (Integer[] integers) {
        int[] result = new int[integers.length];
        for (int i = 0; i < integers.length; i++) {
            result[i] = integers[i];
        }
        return result;
    }

    /**
     * Convert a Double[] to float[]
     * @param doubleArray the double array
     * @return float[] the primitive float array
     */
    public static float[] convertToPrimitiveFloat (double[] doubleArray) {
        float[] result = new float[doubleArray.length];
        for (int i = 0; i < doubleArray.length; i++) {
            result[i] = (float) doubleArray[i];
        }
        return result;
    }

    public static float[] transpose1D(float[] array, int rows, int cols) {
        float[] transposed = new float[rows * cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Map the original 1D index to the transposed 1D index
                transposed[c * rows + r] = array[r * cols + c];
            }
        }
        return transposed;
    }

    public static float[] extractElementsByIndices(float[] original, int[] indices) {
        float[] newArray = new float[indices.length];
        for (int i = 0; i < indices.length; i++) {
            newArray[i] = original[indices[i]];
        }
        return newArray;
    }

}
