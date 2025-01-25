package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.StackConverter;
import ij.io.FileSaver;
import ij.Prefs;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;


public class SMP_based_Max implements PlugIn {
    @Override
    public void run(String arg) {
        // default parameters for the dialog
        String currentFile = Prefs.get("SMP_based_Max.settings.currentFile", "");
        String currentDir = Prefs.get("SMP_based_Max.settings.currentDir", "");
        String defaultOutputFolder = Prefs.get("SMP_based_Max.settings.defaultOutputFolder", "");
        boolean defaultUseOutputFolder = Prefs.get("SMP_based_Max.settings.defaultUseOutputFolder", false);
        double defaultStiffness = Prefs.get("SMP_based_Max.settings.defaultStiffness",60);
        double defaultFilterSize =  Prefs.get("SMP_based_Max.settings.defaultFilterSize", 30);
        double defaultOffset = Prefs.get("SMP_based_Max.settings.defaultOffset", 7);
        double defaultDepth = Prefs.get("SMP_based_Max.settings.defaultDepth", 0);
        // extract all the values in ProcessingMode ENUM class
        String[] modes = Stream.of(ProcessingMode.values()).map(Enum::name).toArray(String[]::new);
        ZStackDirection currentDirection = ZStackDirection.IN;
        String currentMode = modes[0];
        while(true) { // keep the dialog open after each run
            // dialog with button to choose Single file or Multiple file
            GenericDialog processOptions = GUI.newNonBlockingDialog("SMP based Max");
            processOptions.addRadioButtonGroup("Process Mode: ",modes,1,ProcessingMode.values().length, currentMode);
            processOptions.addEnumChoice("Direction of z-stack", ZStackDirection.values(), currentDirection);
            processOptions.addNumericField("Enter envelope stiffness [pixels]:  ", defaultStiffness, 0);
            processOptions.addNumericField("Enter final filter size [pixels]: ", defaultFilterSize, 0);
            processOptions.addNumericField("Offset: N planes above (+) or below (-) blanket [pixels]:  ", defaultOffset, 0);
            processOptions.addNumericField("Depth: MIP for N pixels into blanket [pixels]:  ", defaultDepth, 0);
            processOptions.addDirectoryField("Directory for MULTIPLE FILES", currentDir,30);
            processOptions.addFileField("File path for SINGLE FILE", currentFile, 30);
            processOptions.addCheckbox("Use Output Folder", defaultUseOutputFolder);
            processOptions.addDirectoryField("Output Folder",defaultOutputFolder,30);
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
            boolean useOutputFolder = processOptions.getNextBoolean();
            String outputPath = processOptions.getNextString();
            // save parameters to Prefs when plugin is closed
            Prefs.set("SMP_based_Max.settings.currentDir", dirPath);
            Prefs.set("SMP_based_Max.settings.currentFile",filePath);
            Prefs.set("SMP_based_Max.settings.defaultOutputFolder",outputPath);
            Prefs.set("SMP_based_Max.settings.defaultUseOutputFolder", useOutputFolder);
            Prefs.set("SMP_based_Max.settings.defaultStiffness", stiffness);
            Prefs.set("SMP_based_Max.settings.defaultFilterSize", filterSize);
            Prefs.set("SMP_based_Max.settings.defaultOffset", offset);
            Prefs.set("SMP_based_Max.settings.defaultDepth", depth);

            // update the values in while loop
            currentMode = chosenMode.name();
            currentDir = dirPath;
            currentFile = filePath;
            defaultOutputFolder = outputPath;
            defaultUseOutputFolder = useOutputFolder;
            currentDirection = zStackDirection;
            defaultStiffness = stiffness;
            defaultFilterSize = filterSize;
            defaultOffset = offset;
            defaultDepth = depth;

            String[] validFilePath = new String[0];

            // Interactive Mode
            if (chosenMode == ProcessingMode.INTERACTIVE) {
                validFilePath = SmpBasedMaxUtil.handleSingleFile(filePath);
                if (validFilePath == null) {
                    IJ.showMessage("Selected file is not valid");
                    return;
                }
                new InteractiveDialog(filePath,
                                    zStackDirection,
                                    stiffness,
                                    filterSize,
                                    offset,
                                    depth
                                    ).show();
            }

            // If users choose Single File
            if (chosenMode == ProcessingMode.SINGLE_FILE) {
                validFilePath = SmpBasedMaxUtil.handleSingleFile(filePath);
                if (validFilePath == null) {
                    IJ.showMessage("Selected file is not valid");
                    return;
                }
            }

            // if users choose Multiple Files
            if (chosenMode == ProcessingMode.MULTIPLE_FILES) {
                validFilePath = SmpBasedMaxUtil.handleMultipleFiles(dirPath);
                if (validFilePath == null) {
                    IJ.showMessage("No file selected for multiple-files Option");
                    return;
                }
            }
            // Process image(s) in non-interactive mode
            if (chosenMode != ProcessingMode.INTERACTIVE) {
                // Perform MIP with filePath in filePathArray
                for (String filepath : validFilePath) {
                // create imagePlus object fromm filePath
                ImagePlus inputImage = new ImagePlus(filepath);
                // check if stack is time series, perform flatten to create new stack without time dimension
                inputImage = inputImage.getNFrames() > 1 ? ConvertUtil.convertTimeSeriesToStack(inputImage) : inputImage;
                // convert RGB to grayScale
                if (inputImage.getNChannels() > 1 ||
                        (inputImage.getType() != ImagePlus.GRAY8 &&
                                inputImage.getType() != ImagePlus.GRAY16 &&
                                inputImage.getType() != ImagePlus.GRAY32)) {
                    inputImage = SmpBasedMaxUtil.RGBStackToGrayscaleStack(inputImage); // perform conversion
                }
                // convert to 16 bit gray scale
                if (inputImage.getType() != ImagePlus.GRAY16) {
                    StackConverter stackConverter = new StackConverter(inputImage);
                    stackConverter.convertToGray16();
                }
                // ZProjecting MIP - Maximum Intensity Projection
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
                            fileName + "_SMP" + "_s" + stiffness + "_f" + filterSize +
                            "_o" + offset + ".tif");
                    smpZmapTiff.saveAsTiff(resultDir + File.separator +
                            fileName + "_SMP_zmap" + "_s" + stiffness +
                            "_f" + filterSize + "_o" + offset + ".tif");
                    if (depth == 0 && useOutputFolder) {
                        projectedSMPImageTiff.saveAsTiff(outputPath +
                                fileName + "_SMP" + "_s" + stiffness + "_f" + filterSize +
                                "_o" + offset + ".tif");
                    }
                    // Save SMP depth-adjusted image and zMap
                    if (depth != 0) {
                        FileSaver projectedSMPMIPImageTiff = new FileSaver(projectedSMPMIPImage);
                        FileSaver smpMipZmapTiff = new FileSaver(smpMipZmap);
                        projectedSMPMIPImageTiff.saveAsTiff(resultDir + File.separator +
                                fileName + "_SMPbasedMIP" + "_s" + stiffness +
                                "_f" + filterSize + "_o" + offset +
                                "_d" + depth + ".tif");
                        smpMipZmapTiff.saveAsTiff(resultDir + File.separator +
                                fileName + "_SMPbasedMIP_zmap" + "_s" + stiffness +
                                "_f" + filterSize + "_o" + offset +
                                "_d" + depth + ".tif");
                        if (useOutputFolder) {
                            projectedSMPMIPImageTiff.saveAsTiff(outputPath +
                                    fileName + "_SMP" + "_s" + stiffness + "_f" +
                                    filterSize + "_o" + offset +
                                    "_d" + depth + ".tif");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                }
            }
        }
    }
}
