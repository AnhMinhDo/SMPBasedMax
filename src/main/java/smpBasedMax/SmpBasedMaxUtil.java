package smpBasedMax;


import java.util.Comparator;
import java.util.Arrays;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import ij.io.OpenDialog;
import ij.io.DirectoryChooser;



public class SmpBasedMaxUtil {

    static public String[] handleSingleFile(){
        OpenDialog od = new OpenDialog("Choose .tiff File");
        String filePath=od.getPath();
        if (isTiffExtension(filePath)) {
            return new String[]{filePath};
        } else {
            return null;
        }
    }

    static public String[] handleMultipleFiles (){
        DirectoryChooser dc = new DirectoryChooser("Choose Directory");
        String dirPath = dc.getDirectory();
        String[] filePaths = listFilesInDirectory(dirPath);
        if (filePaths == null) return null;
        // check for .tiff file extension
        ArrayList<String> tiffFilePaths = new ArrayList<>();
        for (String filePath : filePaths) {
            if (isTiffExtension(filePath)) {
                tiffFilePaths.add(filePath);
            }
        }
        if (tiffFilePaths.isEmpty()) return null;
        return tiffFilePaths.toArray(new String[0]);
    }

    static public String[] listFilesInDirectory(String dirPath){
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            // Convert each file to its absolute path
            if (files == null) return null;
            String[] absolutePaths = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                absolutePaths[i] = files[i].getAbsolutePath();  // Get absolute path for each file
            }
            return absolutePaths;
        } else {
            return null;
        }
    }

    static public boolean isTiffExtension(String filePath){
        if (filePath == null) {
            return false;
        } else {
            String filePathLowerCase = filePath.toLowerCase();
            return filePathLowerCase.endsWith(".tiff") || filePathLowerCase.endsWith(".tif");
        }
    }

    public static String extractFilename(String path) {
        File file = new File(path);
        String fileNameWithExtension = file.getName();
        int dotIndex = fileNameWithExtension.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileNameWithExtension.substring(0,dotIndex);  // Extract the part before the dot
        }
        return fileNameWithExtension;  // If there is no dot, return the name itself
    }

    // Create the result directory at same dir of the image stack
    public static String createResultDir (String filePath) throws IOException {
        File file = new File(filePath);
        String fileName = extractFilename(filePath);
        String fileParentDir = file.getParent();
        Path resultDir = Paths.get(fileParentDir + File.separator + "OUT_" + fileName);
        Files.createDirectory(resultDir);
        return resultDir.toString();
    }

    /**
     * Find the indices of localMaxima in 1D array of pixels
     * @param x The float array of pixels
     * @param distance the distance in integer that peak indices are separated by at least
     * @return the indices of peaks that satisfied the distance condition
     */
    public static int[] findPeak(float[] x, int distance){
        Result localMax = findLocalMaxima(x);
        int[] peakIdx = localMax.midpoints;
        int[] leftEdges = localMax.leftEdges;
        int[] rightEdges = localMax.rightEdges;
        float[] peakValues = ConvertUtil.extractElementsByIndices(x, peakIdx);
        boolean[] satisfiedArray = selectPeakByDistance(peakIdx,peakValues,distance);
        int counterTrue = 0;
        for (int i = 0; i < satisfiedArray.length; i++) {
            if (satisfiedArray[i]) {
                counterTrue++;
            }
        }
        int[] peakIdxSatisfiedDistance = new int[counterTrue];
        int pointer = 0;
        for (int i = 0; i < satisfiedArray.length; i++) {
            if (satisfiedArray[i]) {
                peakIdxSatisfiedDistance[pointer] = peakIdx[i];
                pointer++;
            }
        }
        return peakIdxSatisfiedDistance;
    }

    /**
     * Finds local maxima in a 1D array.
     * A maxima are defined as one or more consecutive values that are surrounded by smaller values.
     *
     * @param x The array to search for local maxima.
     * @return A Result object containing indices of midpoints, left edges, and right edges of local maxima.
     */
    public static Result findLocalMaxima(float[] x) {
        int size = x.length;
        int maxSize = size / 2; // There can't be more maxima than half the size of the array

        // Preallocate arrays to store potential maxima information
        ArrayList<Integer> midpoints = new ArrayList<>(maxSize);
        ArrayList<Integer> leftEdges = new ArrayList<>(maxSize);
        ArrayList<Integer> rightEdges = new ArrayList<>(maxSize);

        int i = 1;          // Start at second element
        int iMax = size - 1; // Last element can't be a maxima

        while (i < iMax) {
            // Check if the previous element is smaller
            if (x[i - 1] < x[i]) {
                int iAhead = i + 1;

                // Find the next element that is not equal to x[i]
                while (iAhead < iMax && x[iAhead] == x[i]) {
                    iAhead++;
                }

                // Check if the next unequal element is smaller
                if (x[iAhead] < x[i]) {
                    // Store the indices
                    leftEdges.add(i);
                    rightEdges.add(iAhead - 1);
                    midpoints.add((i + (iAhead - 1)) / 2);

                    // Skip samples that can't be maxima
                    i = iAhead;
                }
            }
            i++;
        }

        // Convert lists to arrays
        int[] midpointsArray = midpoints.stream().mapToInt(Integer::intValue).toArray();
        int[] leftEdgesArray = leftEdges.stream().mapToInt(Integer::intValue).toArray();
        int[] rightEdgesArray = rightEdges.stream().mapToInt(Integer::intValue).toArray();

        return new Result(midpointsArray, leftEdgesArray, rightEdgesArray);
    }

    /**
     * A simple data structure to hold the result of the function findLocalMaxima
     */
    public static class Result {
        public final int[] midpoints;
        public final int[] leftEdges;
        public final int[] rightEdges;

        public Result(int[] midpoints, int[] leftEdges, int[] rightEdges) {
            this.midpoints = midpoints;
            this.leftEdges = leftEdges;
            this.rightEdges = rightEdges;
        }
    }

    /**
     * Helper function to select peaks based on a minimum distance requirement.
     * <p>
     * This method processes an array of peak indices (`peakIdx`) and their corresponding
     * values (`peakValue`) to determine which peaks should be kept or discarded. The
     * decision is based on a specified minimum distance between peaks. Peaks with a higher
     * value are given priority when there are conflicts, keeping them over lower-value peaks
     * if they fall within the specified distance.
     * </p>
     *
     * @param peakIdx    An array of integers representing the indices of detected peaks in a 1D signal.
     * @param peakValue  A float array representing the values or priorities of each corresponding peak.
     *                   Peaks with higher values have a higher priority when considering conflicts.
     * @param distance   An integer specifying the minimum distance required between peaks.
     *                   Peaks that are closer than this distance will be evaluated based on priority.
     * @return A boolean array of the same length as `peakIdx` where each entry indicates whether
     *         the corresponding peak should be kept (`true`) or discarded (`false`).
     */
    public static boolean[] selectPeakByDistance (int[] peakIdx, float[] peakValue, int distance){
        int peakIdxLength = peakIdx.length;
        boolean[] keep = new boolean[peakIdxLength];
        Arrays.fill(keep, true);

        int[] priorityToPosition = sortReturnPeakIndices(peakValue);
        // Highest priority first -> iterate in reverse order (decreasing)
        for (int i = peakIdxLength - 1; i >= 0; i--) {
            // Translate `i` to `j`
            int j = priorityToPosition[i];
            if (!keep[j]) {
                continue;  // Skip evaluation for peak already marked as "don't keep"
            }

            // Flag earlier peaks for removal until minimal distance is exceeded
            int k = j - 1;
            while (k >= 0 && peakIdx[j] - peakIdx[k] <= distance) {
                keep[k] = false;
                k--;
            }

            // Flag later peaks for removal until minimal distance is exceeded
            k = j + 1;
            while (k < peakIdxLength && peakIdx[k] - peakIdx[j] <= distance) {
                keep[k] = false;
                k++;
            }
        }
        return keep;
    }

    /**
     * Sorts an array of peak values and returns the original indices in the new sorted order.
     *
     * @param peakValues The array of peak values to be sorted
     * @return int[] An array of indices that reflects the sorted order of the peak values
     */
    public static int[] sortReturnPeakIndices (float[] peakValues){
        Integer[] indices = new Integer[peakValues.length];
        for (int i = 0; i < peakValues.length; i++) {
            indices[i] = i;
        }
        // Sort indices based on the values in the float array
        Arrays.sort(indices, Comparator.comparingDouble(i -> peakValues[i]));

        return ConvertUtil.convertToPrimitiveInt(indices);
    }





}
