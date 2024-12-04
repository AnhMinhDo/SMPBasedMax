package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import ij.process.StackConverter;
import ij.io.FileSaver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class SMP_based_Max implements PlugIn {
    @Override
    public void run(String arg) {
        while(true) {
            final int[] chooser = new int[]{1};
            String currentDir = IJ.getDirectory("file");
            // dialog with button to choose Single file or Multiple file
            GenericDialog processOptions = new NonBlockingGenericDialog("SMP based Max");
            processOptions.addMessage("Choose an option:");
            processOptions.addButton("Single File", (event) -> chooser[0] = 1);
            processOptions.addButton("Multiple Files", (event) -> chooser[0] = 2);
            processOptions.addStringField("Direction of z-stack (IN or OUT): ", "IN", 10);
            processOptions.addNumericField("Enter envelope stiffness [pixels]:  ", 30, 0);
            processOptions.addNumericField("Enter final filter size [pixels]: ", 30, 0);
            processOptions.addNumericField("Offset: N planes above (+) or below (-) blanket [pixels]:  ", 2, 0);
            processOptions.addNumericField("Depth: MIP for N pixels into blanket [pixels]:  ", 0, 0);
            processOptions.addDirectoryField("image source", currentDir,30);
            processOptions.showDialog();
            if (processOptions.wasCanceled()) return;

            // Retrieve parameter values from dialog
            String zStackDirectionString = processOptions.getNextString();
            ZStackDirection zStackDirection;
            if (zStackDirectionString.equalsIgnoreCase("OUT")) {
                zStackDirection = ZStackDirection.OUT;
            } else {
                zStackDirection = ZStackDirection.IN;
            }
            int stiffness = (int) processOptions.getNextNumber();
            int filterSize = (int) processOptions.getNextNumber();
            int offset = (int) processOptions.getNextNumber();
            int depth = (int) processOptions.getNextNumber();

            // If users choose Single File
            String[] validFilePath = new String[0];
            if (chooser[0] == 1) {
                validFilePath = SmpBasedMaxUtil.handleSingleFile();
                if (validFilePath == null) {
                    IJ.showMessage("No file selected for Single File option.");
                    return;
                }
            }
            // if users choose Multiple Files
            if (chooser[0] == 2) {
                validFilePath = SmpBasedMaxUtil.handleMultipleFiles();
                if (validFilePath != null) {
                    String[] fileNames = new String[validFilePath.length];
                    for (int i = 0; i < validFilePath.length; i++) {
                        fileNames[i] = SmpBasedMaxUtil.extractFilename(validFilePath[i]);
                    }
//                    IJ.showMessage("Selected Parameters and File names",
//                            "Selected file: " + Arrays.toString(fileNames) + "\n" +
//                                    "Direction of z-stack: " + zStackDirection + "\n" +
//                                    "Envelope Stiffness: " + stiffness + "\n" +
//                                    "Final Filter Size: " + filterSize + "\n" +
//                                    "Offset: " + offset + "\n" +
//                                    "Depth: " + depth);
                } else {
                    IJ.showMessage("No file selected for multiple-files Option.");
                    return;
                }
            }

            // Perform MIP with filePath in filePathArray
            for (String filepath : validFilePath) {
                // create imagePlus object fromm filePath
                ImagePlus inputImage = new ImagePlus(filepath);
                // convert to 16 bit gray scale
                StackConverter stackConverter = new StackConverter(inputImage);
                stackConverter.convertToGray16();
                // ZProjecting MIP
                MaxIntensityProjection projector = new MaxIntensityProjection(inputImage);
                ImagePlus projectedImage = projector.doProjection();
                ImagePlus zMap = projector.getZmap();
                // ZProjecting SMP
                SMProjection smProjector = new SMProjection(inputImage, zMap, stiffness, filterSize, zStackDirection, offset);
                ImagePlus projectedSMPImage = smProjector.doSMProjection();
                ImagePlus smpZmap = smProjector.getSMPZmap();
                // SMP-MIP if depth !=0
                SMP_MIP_Projection smpMipProjector = new SMP_MIP_Projection(inputImage, smpZmap, depth, zStackDirection);
                ImagePlus projectedSMPMIPImage = smpMipProjector.doProjection();
                ImagePlus smpMipZmap = smpMipProjector.getZmap();
                // Save files to output directory
                try {
                    // prepare the directory for output
                    String resultDir = SmpBasedMaxUtil.createResultDir(filepath,
                            zStackDirection,
                            stiffness,
                            filterSize,
                            offset,
                            depth);
                    String fileName = SmpBasedMaxUtil.extractFilename(filepath);
                    // Save MIP projected Image and zMap
                    FileSaver projectedImageTiff = new FileSaver(projectedImage);
                    FileSaver zMapTiff = new FileSaver(zMap);
                    projectedImageTiff.saveAsTiff(resultDir + File.separator +
                            fileName + "_MIP" + ".tif");
                    zMapTiff.saveAsTiff(resultDir + File.separator +
                            fileName + "_MIP_zmap" + ".tif");
                    // Save SMP projected image and zMap
                    FileSaver projectedSMPImageTiff = new FileSaver(projectedSMPImage);
                    FileSaver smpZmapTiff = new FileSaver(smpZmap);
                    projectedSMPImageTiff.saveAsTiff(resultDir + File.separator +
                            fileName + "_SMP" + "_stiffness" + stiffness + "_filterSize" + filterSize +
                            "_offSet" + offset + ".tif");
                    smpZmapTiff.saveAsTiff(resultDir + File.separator +
                            fileName + "_SMP_zmap" + "_stiffness" + stiffness +
                            "_filterSize" + filterSize + "_offSet" + offset + ".tif");
                    if (depth != 0) {
                        // Save SMP-MIP projected image and zMap
                        FileSaver projectedSMPMIPImageTiff = new FileSaver(projectedSMPMIPImage);
                        FileSaver smpMipZmapTiff = new FileSaver(smpMipZmap);
                        projectedSMPMIPImageTiff.saveAsTiff(resultDir + File.separator +
                                fileName + "_SMPbasedMIP" + "_stiffness" + stiffness +
                                "_filterSize" + filterSize + "_offSet" + offset +
                                "_depth" + depth + ".tif");
                        smpMipZmapTiff.saveAsTiff(resultDir + File.separator +
                                fileName + "_SMPbasedMIP_zmap" + "_stiffness" + stiffness +
                                "_filterSize" + filterSize + "_offSet" + offset +
                                "_depth" + depth + ".tif");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
