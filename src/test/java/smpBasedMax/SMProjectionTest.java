package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SMProjectionTest {

    @Test
    void testRoundUpRemoveOutliers() {
        float[] arr1 = new float[]{1f,5f,9f,0.5f,3f};
        float[] arr2 = new float[]{0.1f,4f,1f,0.1f,7f};
        float[] resultArray = new float[5];
        float[] expected = new float[]{1f,5f,7f,1f,7f};
        SMProjection.roundUpRemoveOutliers(arr1,arr2,resultArray, 7);
        assertArrayEquals(expected, resultArray);
    }
}