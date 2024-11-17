package smpBasedMax;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmpBasedMaxUtilTest {

    @Test
    void sortReturnPeakIndices() {
        SmpBasedMaxUtil smpBasedMaxUtil = new SmpBasedMaxUtil();

        float[] peakValues = {1.2f, 3.4f, 2.5f};

        int[] result = smpBasedMaxUtil.sortReturnPeakIndices(peakValues);

        // Assert that the indices are sorted based on peakValues
        assertArrayEquals(new int[]{0, 2, 1}, result);
    }
}