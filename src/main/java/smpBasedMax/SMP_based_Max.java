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
        boolean defaultUseSecondFile = Prefs.get("SMP_based_Max.settings.defaultUseSecondFile", false);
        String defaultSecondFile = Prefs.get("SMP_based_Max.settings.defaultSecondFile", "");
        boolean defaultUseThirdFile = Prefs.get("SMP_based_Max.settings.defaultUseThirdFile", false);
        String defaultThirdFile = Prefs.get("SMP_based_Max.settings.defaultThirdFile", "");
        double defaultStiffness = Prefs.get("SMP_based_Max.settings.defaultStiffness",60);
        double defaultFilterSize =  Prefs.get("SMP_based_Max.settings.defaultFilterSize", 30);
        double defaultOffset = Prefs.get("SMP_based_Max.settings.defaultOffset", 7);
        double defaultDepth = Prefs.get("SMP_based_Max.settings.defaultDepth", 0);
        // extract all the values in ProcessingMode ENUM class
        String[] modes = Stream.of(ProcessingMode.values()).map(Enum::name).toArray(String[]::new);
        ZStackDirection currentDirection = ZStackDirection.IN;
        while(true) { // keep the dialog open after each run
            // dialog with button to choose Single file or Multiple file
            GenericDialog processOptions = GUI.newNonBlockingDialog("SMP based Max");
            processOptions.addRadioButtonGroup("Process Mode: ",modes,1,ProcessingMode.values().length, modes[0]);
            processOptions.addEnumChoice("Direction of z-stack", ZStackDirection.values(), currentDirection);
            processOptions.addNumericField("Enter envelope stiffness [pixels]:  ", defaultStiffness, 0);
            processOptions.addNumericField("Enter final filter size [pixels]: ", defaultFilterSize, 0);
            processOptions.addNumericField("Offset: N planes above (+) or below (-) blanket [pixels]:  ", defaultOffset, 0);
            processOptions.addNumericField("Depth: MIP for N pixels into blanket [pixels]:  ", defaultDepth, 0);
            processOptions.addDirectoryField("Directory for MULTIPLE FILES", currentDir,30);
            processOptions.addFileField("File path for SINGLE FILE", currentFile, 30);
            processOptions.addCheckbox("Project Second File", defaultUseSecondFile);
            processOptions.addFileField("SECOND FILE", defaultSecondFile, 30);
            processOptions.addCheckbox("Project Third File", defaultUseThirdFile);
            processOptions.addFileField("THIRD FILE", defaultThirdFile, 30);
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
            boolean useSecondFile = processOptions.getNextBoolean();
            String secondFilePath = processOptions.getNextString();
            boolean useThirdFile = processOptions.getNextBoolean();
            String thirdFilePath = processOptions.getNextString();
            // save parameters to Prefs when plugin is closed
            Prefs.set("SMP_based_Max.settings.currentDir", dirPath);
            Prefs.set("SMP_based_Max.settings.currentFile",filePath);
            Prefs.set("SMP_based_Max.settings.defaultUseSecondFile", useSecondFile);
            Prefs.set("SMP_based_Max.settings.defaultSecondFile", secondFilePath);
            Prefs.set("SMP_based_Max.settings.defaultUseThirdFile", useThirdFile);
            Prefs.set("SMP_based_Max.settings.defaultThirdFile", thirdFilePath);
            Prefs.set("SMP_based_Max.settings.defaultStiffness", stiffness);
            Prefs.set("SMP_based_Max.settings.defaultFilterSize", filterSize);
            Prefs.set("SMP_based_Max.settings.defaultOffset", offset);
            Prefs.set("SMP_based_Max.settings.defaultDepth", depth);
            // update the values in while loop
            currentDir = dirPath;
            currentFile = filePath;
            defaultUseSecondFile = useSecondFile;
            defaultSecondFile = secondFilePath;
            defaultUseThirdFile = useThirdFile;
            defaultThirdFile = thirdFilePath;
            currentDirection = zStackDirection;
            defaultStiffness = stiffness;
            defaultFilterSize = filterSize;
            defaultOffset = offset;
            defaultDepth = depth;

            // Interactive Mode
            if (chosenMode == ProcessingMode.INTERACTIVE) {
                new InteractiveDialog(filePath,
                                    zStackDirection,
                                    stiffness,
                                    filterSize,
                                    offset,
                                    depth
                                    ).show();
            }

            // If users choose Single File
            String[] validFilePath = new String[0];
            if (chosenMode == ProcessingMode.SINGLE_FILE | chosenMode == ProcessingMode.INTERACTIVE) {
                validFilePath = SmpBasedMaxUtil.checkSingleFile(filePath);
                if (validFilePath == null) {
                    IJ.showMessage("No file selected for Single File option.");
                    return;
                }
            }

            // if users choose Multiple Files
            if (chosenMode == ProcessingMode.MULTIPLE_FILES) {
                validFilePath = SmpBasedMaxUtil.checkMultipleFile(dirPath);
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
                    // Save SMP depth-adjusted image and zMap
                    if (depth != 0) {
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
}
