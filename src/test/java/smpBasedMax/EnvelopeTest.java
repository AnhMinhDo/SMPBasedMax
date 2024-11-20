package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EnvelopeTest {
    @Test
    void testSplineInterpolation() {
        float[] signalValues = new float[]{1f,3f,1f,2f,1f,4f,4f,1f};
        int[] peakIdx = new int[]{1,3,5};
        float[] peakValue = new float[]{3f,2f,4f};
        float [] expected = new float[]{4.6250f, 3.0000f, 2.1250f, 2.0000f,2.6250f, 4.0000f,6.1250f,9.0000f};
        float[] actual = Envelope.splineInterpolate(peakIdx,peakValue, signalValues.length);
        assertArrayEquals(expected, actual);
    }
}