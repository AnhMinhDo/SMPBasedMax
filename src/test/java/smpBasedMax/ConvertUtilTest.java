package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static smpBasedMax.ConvertUtil.transpose1D;

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

    @Test
    public void testTranspose1D() {
        // Test Case 1
        float[] input1 = {1f, 2f, 3f, 4f, 5f, 6f};
        int rows1 = 2;
        int cols1 = 3;
        float[] expected1 = {1f, 4f, 2f, 5f, 3f, 6f}; // Transposed 3x2
        assertArrayEquals(expected1, transpose1D(input1, rows1, cols1), 0.0001f);

        // Test Case 2
        float[] input2 = {1f, 2f, 3f, 4f};
        int rows2 = 2;
        int cols2 = 2;
        float[] expected2 = {1f, 3f, 2f, 4f}; // Transposed 2x2
        assertArrayEquals(expected2, transpose1D(input2, rows2, cols2), 0.0001f);

        // Test Case 3
        float[] input3 = {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f};
        int rows3 = 3;
        int cols3 = 3;
        float[] expected3 = {1f, 4f, 7f, 2f, 5f, 8f, 3f, 6f, 9f}; // Transposed 3x3
        assertArrayEquals(expected3, transpose1D(input3, rows3, cols3), 0.0001f);

        // Test Case 4
        float[] input4 = {1f, 4f, 2f, 5f, 3f, 6f};
        int rows4 = 3;
        int cols4 = 2;
        float[] expected4 = {1f, 2f, 3f, 4f, 5f, 6f}; // Transposed 3x2
        assertArrayEquals(expected4, transpose1D(input4, rows4, cols4), 0.0001f);

    }

}