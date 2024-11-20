package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConvertUtilTest {
    @Test
    void testConvertToPrimitiveInt() {
        // Test case 1: Normal case with Integer array
        Integer[] input = {1, 2, 3, 4};
        int[] expected = {1, 2, 3, 4};
        int[] actual = ConvertUtil.convertToPrimitiveInt(input);
        assertArrayEquals(expected, actual);

        // Test case 2: Empty array
        Integer[] emptyInput = {};
        int[] emptyExpected = {};
        int[] emptyActual = ConvertUtil.convertToPrimitiveInt(emptyInput);
        assertArrayEquals(emptyExpected, emptyActual);
    }
    @Test
    void testExtractElementsByIndices(){
        int[] inputIdx1 = new int[]{0, 2, 5, 7};
        float[] inputValues1 = new float[]{1f,2f,3f,4f,5f, 6f, 7f, 8f};
        float[] expected = new float[]{1f, 3f, 6f, 8f};
        float[] actual = ConvertUtil.extractElementsByIndices(inputValues1, inputIdx1);
        assertArrayEquals(expected, actual);
    }

}