package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.StackConverter;

import java.util.Arrays;



public class SMP_based_Max implements PlugIn {
    @Override
    public void run(String arg) {
        final int[] chooser = new int[1];
        // dialog with button to choose Single file or Multiple file
        GenericDialog processOptions = new GenericDialog("File Selection");
        processOptions.addMessage("Choose an option:");
        processOptions.addButton("Single File", (event) -> chooser[0] = 1);
        processOptions.addButton("Multiple Files", (event) -> chooser[0] = 2);
        processOptions.showDialog();

        String[] validFilePath = new String[0];
        if (chooser[0] == 1) {
            validFilePath = SmpBasedMaxUtil.handleSingleFile();
            if (validFilePath != null) {
                IJ.showMessage("Selected file: " + validFilePath[0]);
            } else {
                IJ.showMessage("No file selected for Single File option.");
            }
        }
        if (chooser[0] == 2) {
            validFilePath = SmpBasedMaxUtil.handleMultipleFiles();
            if (validFilePath != null) {
                String[] fileNames = new String[validFilePath.length];
                for (int i = 0; i < validFilePath.length; i++) {
                    fileNames[i] = SmpBasedMaxUtil.extractFilename(validFilePath[i]);
                }
                IJ.showMessage("Selected file: " + Arrays.toString(fileNames));
            } else {
                IJ.showMessage("No file selected for Single File option.");
            }
        }

        // Create a dialog with offset, threshold, and range options
        GenericDialog gd = new GenericDialog("Adjust Parameters");
        gd.addNumericField("Stiffness:  ",0, 0);
        gd.addNumericField("Filter size:  ", 0, 0);
        gd.addNumericField("Offset:  ", 0, 0);
        gd.addNumericField("Depth:  ", 0, 0);
        gd.showDialog();
        // Retrieve values from dialog
        double stiffness = gd.getNextNumber();
        double offset = gd.getNextNumber();
        double filter_size = gd.getNextNumber();
        double depth =  gd.getNextNumber();

        // Show the results
        IJ.showMessage("Selected Parameters",
                "sample Name: " + stiffness + "\n" +
                        "Offset: " + offset + "\n" +
                        "Filter size: " + filter_size + "\n" +
                        "Range: " + depth);

        // Perform MIP with filePath in filePathArray
        for (String filepath : validFilePath) {
            // create imagePlus object fromm filePath
            ImagePlus inputImage = new ImagePlus(filepath);
            // convert to 16 bit gray scale
            StackConverter stackConverter = new StackConverter(inputImage);
            stackConverter.convertToGray16();
            // ZProjecting the current image and show it in a new window
            MaxIntensityProjection projector = new MaxIntensityProjection(inputImage);
            ImagePlus projectedImage = projector.doProjection();
            ImagePlus zMap = projector.getZmap();
            projectedImage.show();
            zMap.show();
        }
    }
}
