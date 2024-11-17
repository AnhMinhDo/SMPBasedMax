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

}