package smpBasedMax;


import java.util.Comparator;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
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

    // helper function to select peak by distance
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
            while (k >= 0 && peakIdx[j] - peakIdx[k] < distance) {
                keep[k] = false;
                k--;
            }

            // Flag later peaks for removal until minimal distance is exceeded
            k = j + 1;
            while (k < peakIdxLength && peakIdx[k] - peakIdx[j] < distance) {
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
