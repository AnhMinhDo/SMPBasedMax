package smpBasedMax;


import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ChannelSplitter;

import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;



public class SmpBasedMaxUtil {

    static public String[] handleSingleFile(String filePath){
        if (isTiffExtension(filePath)) {
            return new String[]{filePath};
        } else {
            return null;
        }
    }

    static public String[] handleMultipleFiles (String dirPath){
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
    public static String createResultDir (String filePath,
                                          ZStackDirection zStackDirection,
                                          int stiffness,
                                          int filterSize,
                                          int offset,
                                          int depth) throws IOException {
        String direction;
        if (zStackDirection == ZStackDirection.IN){
            direction  = "IN_";
        } else {
            direction = "OUT_";
        }
        File file = new File(filePath);
        String fileName = extractFilename(filePath);
        String fileParentDir = file.getParent();
        Path resultDir = Paths.get(fileParentDir + File.separator + direction + fileName+"_stiffness"+stiffness+"_filterSize"+filterSize+"_offSet"+offset+"_depth"+depth);
        Files.createDirectory(resultDir);
        return resultDir.toString();
    }

    /**
     * function to Convert RGB Image Stack to Grayscale
     * Logic: The human perception of color brightness is different between each one, with green is the brightest.
     * Therefore, each channel value needed to be multiple with the suitable coefficient.
     * these coefficient is used to weight how much each channel contributed to the total brightness(grayscale)
     * grayscale_value = 0.2125 Red + 0.7154 Green + 0.0721 Blue
     * source: <a href="https://poynton.ca/PDFs/ColorFAQ.pdf">...</a> (page 6)
     * @param imp ImagePlus object of the RGB Stack
     * @return ImagePlus object of the grayscale Stack
     */
    public static ImagePlus RGBStackToGrayscaleStack(ImagePlus imp){
        ImageStack[] channels = ChannelSplitter.splitRGB(imp.getStack(),true);
        ImageStack redStack = channels[0];
        ImageStack greenStack = channels[1];
        ImageStack blueStack = channels[2];
        ImageStack resultStack = ImageStack.create(redStack.getWidth(), redStack.getHeight(),
                                                redStack.getSize(), 16);
        for (int currentSlice = 0; currentSlice < redStack.getSize(); currentSlice++) {
            short[] redPixelArray = (short[]) redStack.getPixels(currentSlice);
            short[] greenPixelArray = (short[]) greenStack.getPixels(currentSlice);
            short[] bluePixelArray = (short[]) blueStack.getPixels(currentSlice);
            float[] resultPixelArray = (float[]) resultStack.getProcessor(currentSlice).getPixels();
            for (int i = 0; i < resultPixelArray.length; i++) {
                resultPixelArray[i] = calculateBrightnessFromRGB(redPixelArray[i],
                                                                greenPixelArray[i],
                                                                bluePixelArray[i]);
            }
        }
        return new ImagePlus(imp.getTitle(), resultStack);

    }

    public static float calculateBrightnessFromRGB(short redValue, short greenValue, short blueValue) {
        return (float)(0.2125*redValue + 0.7154*greenValue + 0.0721*blueValue);
    }
}
























