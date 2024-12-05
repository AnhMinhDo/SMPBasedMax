package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import ij.process.StackConverter;
import ij.io.FileSaver;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;


public class SMP_based_Max implements PlugIn {
    @Override
    public void run(String arg) {
        while(true) {
            // default parameters for the dialog
            String currentDir = IJ.getDirectory("current");
            String currentFile = IJ.getDirectory("image");
            // extract all the values in ProcessingMode ENUM class
            String[] modes = Stream.of(ProcessingMode.values()).map(Enum::name).toArray(String[]::new);
            // dialog with button to choose Single file or Multiple file
            NonBlockingGenericDialog processOptions = new NonBlockingGenericDialog("SMP based Max");
            processOptions.addRadioButtonGroup("Process Mode: ",modes,1,ProcessingMode.values().length, modes[0]);
            processOptions.addEnumChoice("Direction of z-stack", ZStackDirection.values(),ZStackDirection.IN);
            processOptions.addNumericField("Enter envelope stiffness [pixels]:  ", 30, 0);
            processOptions.addNumericField("Enter final filter size [pixels]: ", 30, 0);
            processOptions.addNumericField("Offset: N planes above (+) or below (-) blanket [pixels]:  ", 2, 0);
            processOptions.addNumericField("Depth: MIP for N pixels into blanket [pixels]:  ", 0, 0);
            processOptions.addDirectoryField("Directory for MULTIPLE FILES", currentDir,30);
            processOptions.addFileField("File path for SINGLE FILE", currentFile, 30);
            processOptions.showDialog();
            if (processOptions.wasCanceled()) return;

            // Retrieve parameter values from dialog
            ProcessingMode chosenMode = ProcessingMode.valueOf(processOptions.getNextRadioButton());
            ZStackDirection zStackDirection = processOptions.getNextEnumChoice(ZStackDirection.class);
            int stiffness = (int) processOptions.getNextNumber();
            int filterSize = (int) processOptions.getNextNumber();
            int offset = (int) processOptions.getNextNumber();
            int depth = (int) processOptions.getNextNumber();
            String dirPath = processOptions.getNextString();
            String filePath = processOptions.getNextString();

            // If users choose Single File
            String[] validFilePath = new String[0];
            if (chosenMode == ProcessingMode.SINGLE_FILE) {
                validFilePath = SmpBasedMaxUtil.handleSingleFile(filePath);
                if (validFilePath == null) {
                    IJ.showMessage("No file selected for Single File option.");
                    return;
                }
            }
            // if users choose Multiple Files
            if (chosenMode == ProcessingMode.MULTIPLE_FILES) {
                validFilePath = SmpBasedMaxUtil.handleMultipleFiles(dirPath);
                if (validFilePath != null) {
                    String[] fileNames = new String[validFilePath.length];
                    for (int i = 0; i < validFilePath.length; i++) {
                        fileNames[i] = SmpBasedMaxUtil.extractFilename(validFilePath[i]);
                    }
                } else {
                    IJ.showMessage("No file selected for multiple-files Option.");
                    return;
                }
            }

            // Perform MIP with filePath in filePathArray
            for (String filepath : validFilePath) {
                // create imagePlus object fromm filePath
                ImagePlus inputImage = new ImagePlus(filepath);
                // check if stack is grayscale
                if(inputImage.getNChannels() > 1 ||
                        (inputImage.getType() != ImagePlus.GRAY8 &&
                                inputImage.getType() != ImagePlus.GRAY16 &&
                                inputImage.getType() != ImagePlus.GRAY32)){
                    // TODO: function to convert multi channels stack to grayscale stack
                }
                // convert to 16 bit gray scale
                if (inputImage.getType() != ImagePlus.GRAY16) {
                    StackConverter stackConverter = new StackConverter(inputImage);
                    stackConverter.convertToGray16();
                }
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
