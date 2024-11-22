package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EnvelopeTest {
    @Test
    void testyUpper1D(){
        float[] signalValues = new float[]{1f,3f,1f,2f,1f,4f,4f,1f};
        int distance = 1;
        float[] actual = Envelope.yUpper1D(signalValues, distance);
        float[] expected = new float[]{4.6250f, 3.0000f, 2.1250f, 2.0000f,2.6250f, 4.0000f,6.1250f,9.0000f};
        assertArrayEquals(expected, actual);
    }

    @Test
    void testSplineInterpolation() {
        float[] signalValues = new float[]{1f,3f,1f,2f,1f,4f,4f,1f};
        int[] peakIdx = new int[]{1,3,5};
        float[] peakValue = new float[]{3f,2f,4f};
        float [] expected = new float[]{4.6250f, 3.0000f, 2.1250f, 2.0000f,2.6250f, 4.0000f,6.1250f,9.0000f};
        float[] actual = Envelope.splineInterpolate(peakIdx,peakValue, signalValues.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFindPeaks(){
        float[] input = {1.0f, 3.0f, 1.0f, 2.0f, 1.0f, 4.0f, 4.0f, 1.0f};
        int distance = 1;
        int[] output1 = Envelope.findPeak(input, distance);
        assertArrayEquals(output1, new int[]{1, 3, 5});

        float[] input2 = {1.0f, 3.0f, 1.0f, 2.0f, 1.0f, 4.0f, 4.0f, 1.0f};
        int distance2 = 2;
        int[] output2 = Envelope.findPeak(input2, distance2);
        assertArrayEquals(output2, new int[]{1, 5});

    }

    @Test
    public void testFindLocalMaxima(){

        // test single peak
        float[] input = {1.0f, 2.0f, 1.0f};
        Envelope.Result result = Envelope.findLocalMaxima(input);
        assertArrayEquals(new int[]{1}, result.midpoints);
        assertArrayEquals(new int[]{1}, result.leftEdges);
        assertArrayEquals(new int[]{1}, result.rightEdges);

        // test multiple peaks
        float[] input2 = {1.0f, 3.0f, 1.0f, 2.0f, 1.0f, 4.0f, 4.0f, 1.0f};
        Envelope.Result result2 = Envelope.findLocalMaxima(input2);

        assertArrayEquals(new int[]{1, 3, 5}, result2.midpoints) ;
        assertArrayEquals(new int[]{1, 3, 5}, result2.leftEdges);
        assertArrayEquals(new int[]{1, 3, 6}, result2.rightEdges);
    }

    @Test
    public void testFindLocalMinima(){

        // test single peak
        float[] input = {2.0f, 1.0f, 2.0f};
        Envelope.Result result = Envelope.findLocalMinima(input);
        assertArrayEquals(new int[]{1}, result.midpoints);
        assertArrayEquals(new int[]{1}, result.leftEdges);
        assertArrayEquals(new int[]{1}, result.rightEdges);

        // test multiple peaks
        float[] input2 = {4.0f, 2.0f, 4.0f, 3.0f, 4.0f, 1.0f, 1.0f, 4.0f};
        Envelope.Result result2 = Envelope.findLocalMinima(input2);

        assertArrayEquals(new int[]{1, 3, 5}, result2.midpoints) ;
        assertArrayEquals(new int[]{1, 3, 5}, result2.leftEdges);
        assertArrayEquals(new int[]{1, 3, 6}, result2.rightEdges);
    }



    @Test
    public void testSortReturnPeakIndices() {

        float[] peakValues = {1.2f, 3.4f, 2.5f};

        int[] result = Envelope.sortReturnPeakIndices(peakValues);

        // Assert that the indices are sorted based on peakValues
        assertArrayEquals(new int[]{0, 2, 1}, result);
    }

    @Test
    public void testSelectPeakByDistance() {
        // Test case 1: Basic example with distinct peaks and sufficient distance
        int[] peaks1 = {1, 3, 7, 10, 14};
        float[] priority1 = {0.9f, 0.5f, 0.8f, 0.6f, 0.3f};
        int distance1 = 3;
        boolean[] expected1 = {true, false, true, false, true};

        boolean[] result1 = Envelope.selectPeakByDistance(peaks1, priority1, distance1);
        assertArrayEquals(expected1, result1);

        // Test case 2: Peaks with very close distances
        int[] peaks2 = {1, 2, 3, 4, 5};
        float[] priority2 = {0.1f, 0.9f, 0.5f, 0.3f, 0.7f};
        int distance2 = 2;
        boolean[] expected2 = {false, true, false, false, true};

        boolean[] result2 = Envelope.selectPeakByDistance(peaks2, priority2, distance2);
        assertArrayEquals(expected2, result2);

        // Test case 3: Peaks with large distance
        int[] peaks3 = {2, 5, 10, 15, 20};
        float[] priority3 = {0.8f, 0.4f, 0.9f, 0.3f, 0.7f};
        int distance3 = 5;
        boolean[] expected3 = {true, false, true, false, true};

        boolean[] result3 = Envelope.selectPeakByDistance(peaks3, priority3, distance3);
        assertArrayEquals(expected3, result3);

        // Test case 4: Single peak
        int[] peaks4 = {5};
        float[] priority4 = {1.0f};
        int distance4 = 1;
        boolean[] expected4 = {true};

        boolean[] result4 = Envelope.selectPeakByDistance(peaks4, priority4, distance4);
        assertArrayEquals(expected4, result4);

        // Test case 5: No peaks
        int[] peaks5 = {};
        float[] priority5 = {};
        int distance5 = 2;
        boolean[] expected5 = {};

        boolean[] result5 = Envelope.selectPeakByDistance(peaks5, priority5, distance5);
        assertArrayEquals(expected5, result5);
    }

    @Test
    public void testSelectTroughByDistance() {
        // Test case 1: Basic example with distinct troughs and sufficient distance
        int[] troughs1 = {1, 3, 7, 10, 14};
        float[] priority1 = {0.9f, 0.5f, 0.8f, 0.6f, 0.3f};
        int distance1 = 3;
        boolean[] expected1 = {false, true, false, true, true};

        boolean[] result1 = Envelope.selectTroughByDistance(troughs1, priority1, distance1);
        assertArrayEquals(expected1, result1);

        // Test case 2: Peaks with very close distances
        int[] peaks2 = {1, 2, 3, 4, 5};
        float[] priority2 = {0.1f, 0.9f, 0.5f, 0.3f, 0.7f};
        int distance2 = 2;
        boolean[] expected2 = {true, false, false, true, false};

        boolean[] result2 = Envelope.selectTroughByDistance(peaks2, priority2, distance2);
        assertArrayEquals(expected2, result2);

        // Test case 3: Troughs with large distance
        int[] troughs3 = {2, 5, 10, 15, 20};
        float[] priority3 = {0.8f, 0.4f, 0.9f, 0.3f, 0.7f};
        int distance3 = 5;
        boolean[] expected3 = {false, true, false, true, false};

        boolean[] result3 = Envelope.selectTroughByDistance(troughs3, priority3, distance3);
        assertArrayEquals(expected3, result3);

        // Test case 4: Single trough
        int[] troughs4 = {5};
        float[] priority4 = {1.0f};
        int distance4 = 1;
        boolean[] expected4 = {true};

        boolean[] result4 = Envelope.selectTroughByDistance(troughs4, priority4, distance4);
        assertArrayEquals(expected4, result4);

        // Test case 5: No troughs
        int[] troughs5 = {};
        float[] priority5 = {};
        int distance5 = 2;
        boolean[] expected5 = {};

        boolean[] result5 = Envelope.selectTroughByDistance(troughs5, priority5, distance5);
        assertArrayEquals(expected5, result5);
    }
}
