package smpBasedMax;

public class ConvertUtil {

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
