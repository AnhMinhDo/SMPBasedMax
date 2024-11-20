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

    public static double[] transpose1D(double[] array, int rows, int cols) {
        double[] transposed = new double[rows * cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Map the original 1D index to the transposed 1D index
                transposed[c * rows + r] = array[r * cols + c];
            }
        }
        return transposed;
    }
}
