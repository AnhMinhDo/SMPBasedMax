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
}
