package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmpBasedMaxUtilTest {
    @Test
    public void testFindPeaks(){
        float[] input = {1.0f, 3.0f, 1.0f, 2.0f, 1.0f, 4.0f, 4.0f, 1.0f};
        int distance = 1;
        int[] output1 = SmpBasedMaxUtil.findPeak(input, distance);
        assertArrayEquals(output1, new int[]{1, 3, 5});

        float[] input2 = {1.0f, 3.0f, 1.0f, 2.0f, 1.0f, 4.0f, 4.0f, 1.0f};
        int distance2 = 2;
        int[] output2 = SmpBasedMaxUtil.findPeak(input2, distance2);
        assertArrayEquals(output2, new int[]{1, 5});

    }

    @Test
    public void testFindLocalMaxima(){

        // test single peak
        float[] input = {1.0f, 2.0f, 1.0f};
        SmpBasedMaxUtil.Result result = SmpBasedMaxUtil.findLocalMaxima(input);
        assertArrayEquals(new int[]{1}, result.midpoints);
        assertArrayEquals(new int[]{1}, result.leftEdges);
        assertArrayEquals(new int[]{1}, result.rightEdges);

        // test multiple peaks
        float[] input2 = {1.0f, 3.0f, 1.0f, 2.0f, 1.0f, 4.0f, 4.0f, 1.0f};
        SmpBasedMaxUtil.Result result2 = SmpBasedMaxUtil.findLocalMaxima(input2);

        assertArrayEquals(new int[]{1, 3, 5}, result2.midpoints) ;
        assertArrayEquals(new int[]{1, 3, 5}, result2.leftEdges);
        assertArrayEquals(new int[]{1, 3, 6}, result2.rightEdges);
    }

    @Test
    public void testSortReturnPeakIndices() {

        float[] peakValues = {1.2f, 3.4f, 2.5f};

        int[] result = SmpBasedMaxUtil.sortReturnPeakIndices(peakValues);

        // Assert that the indices are sorted based on peakValues
        assertArrayEquals(new int[]{0, 2, 1}, result);
    }

    @Test
    public void testSelectByPeakDistance() {
        // Test case 1: Basic example with distinct peaks and sufficient distance
        int[] peaks1 = {1, 3, 7, 10, 14};
        float[] priority1 = {0.9f, 0.5f, 0.8f, 0.6f, 0.3f};
        int distance1 = 3;
        boolean[] expected1 = {true, false, true, false, true};

        boolean[] result1 = SmpBasedMaxUtil.selectPeakByDistance(peaks1, priority1, distance1);
        assertArrayEquals(expected1, result1);

        // Test case 2: Peaks with very close distances
        int[] peaks2 = {1, 2, 3, 4, 5};
        float[] priority2 = {0.1f, 0.9f, 0.5f, 0.3f, 0.7f};
        int distance2 = 2;
        boolean[] expected2 = {false, true, false, false, true};

        boolean[] result2 = SmpBasedMaxUtil.selectPeakByDistance(peaks2, priority2, distance2);
        assertArrayEquals(expected2, result2);

        // Test case 3: Peaks with large distance, should keep all
        int[] peaks3 = {2, 5, 10, 15, 20};
        float[] priority3 = {0.8f, 0.4f, 0.9f, 0.3f, 0.7f};
        int distance3 = 5;
        boolean[] expected3 = {true, false, true, false, true};

        boolean[] result3 = SmpBasedMaxUtil.selectPeakByDistance(peaks3, priority3, distance3);
        assertArrayEquals(expected3, result3);

        // Test case 4: Single peak
        int[] peaks4 = {5};
        float[] priority4 = {1.0f};
        int distance4 = 1;
        boolean[] expected4 = {true};

        boolean[] result4 = SmpBasedMaxUtil.selectPeakByDistance(peaks4, priority4, distance4);
        assertArrayEquals(expected4, result4);

        // Test case 5: No peaks
        int[] peaks5 = {};
        float[] priority5 = {};
        int distance5 = 2;
        boolean[] expected5 = {};

        boolean[] result5 = SmpBasedMaxUtil.selectPeakByDistance(peaks5, priority5, distance5);
        assertArrayEquals(expected5, result5);
    }

}