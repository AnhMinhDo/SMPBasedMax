package smpBasedMax;

import ij.IJ;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.Prefs;

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
            currentMode = chosenMode.name();
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
                String[] validFilePath = SmpBasedMaxUtil.checkSingleFile(filePath);
                if (validFilePath == null) {
                    IJ.showMessage("Selected File is not valid");
                    return;
                }
                new InteractiveDialog(validFilePath[0],
                                    zStackDirection,
                                    stiffness,
                                    filterSize,
                                    offset,
                                    depth
                                    ).show();
            }

            // If users choose Single File
            if (chosenMode == ProcessingMode.SINGLE_FILE) {
                String[] validFilePath = SmpBasedMaxUtil.checkSingleFile(filePath);
                if (validFilePath == null) {
                    IJ.showMessage("Selected File is not valid");
                    return;
                }
                HandleSingleFile hsf = new HandleSingleFile(validFilePath[0],
                        zStackDirection,
                        stiffness,
                        filterSize,
                        offset,
                        depth,
                        useSecondFile,
                        secondFilePath,
                        useThirdFile,
                        thirdFilePath);
                hsf.process();
            }

            // if users choose Multiple Files
            if (chosenMode == ProcessingMode.MULTIPLE_FILES) {
                String[] validFilePath = SmpBasedMaxUtil.checkMultipleFile(dirPath);
                if (validFilePath == null) {
                    IJ.showMessage("No file selected for multiple-files Option.");
                    return;
                }
                HandleMultipleFile hmf = new HandleMultipleFile(validFilePath,
                        zStackDirection,
                        stiffness,
                        filterSize,
                        offset,
                        depth);
                hmf.process();
            }
        }
    }
}
