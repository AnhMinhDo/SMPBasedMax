package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.io.FileInfo;
import ij.process.StackConverter;

public class SMP_based_Max implements PlugIn {
    @Override
    public void run(String arg) {
        // Get the currently active image
        ImagePlus currentImage = WindowManager.getCurrentImage();
        if (currentImage == null) {
            IJ.showMessage("No image open", "Please open an image to use this plugin.");
            return;
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
        // get file path
        FileInfo fileInfo = currentImage.getOriginalFileInfo();
        String filePath = null;
        if (fileInfo != null && fileInfo.directory != null && fileInfo.fileName != null) {
            filePath = fileInfo.directory + fileInfo.fileName;
            IJ.showMessage("Image Path", "File path of current image:\n" + filePath);
        } else {
            IJ.showMessage("File path not available", "The current image may not have a file path (e.g., it was generated or modified in ImageJ).");
        }
        // create imagePlus object fromm filePath
        ImagePlus inputImage = new ImagePlus(filePath);
        // convert to 16 bit gray scale
        StackConverter stackConverter = new StackConverter(inputImage);
        stackConverter.convertToGray16();
        // ZProjecting the current image and show it in a new window
        MaxIntensityProjection projector = new MaxIntensityProjection(inputImage);
        ImagePlus projectedImage = projector.doProjection();
        projectedImage.show();

    }
}
