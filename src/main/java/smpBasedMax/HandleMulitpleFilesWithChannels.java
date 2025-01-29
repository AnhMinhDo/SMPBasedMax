package smpBasedMax;

import ij.IJ;

import java.io.File;

public class HandleMulitpleFilesWithChannels {

    private final File inputDir;

    public HandleMulitpleFilesWithChannels(File dirPath,
                                           ZStackDirection zStackDirection,
                                           int stiffness,
                                           int filterSize,
                                           int offset,
                                           int depth) {
        this.inputDir = dirPath;
        File[] ArrayFileWithChannels = inputDir.listFiles(File::isDirectory);
        if (ArrayFileWithChannels == null) {
            IJ.showMessage("No subdirectory in " + this.inputDir); return;}
        for(File fileWithChannel : ArrayFileWithChannels) {
            HandleSingleFileWithChannels hsfwc = new HandleSingleFileWithChannels(fileWithChannel,
                                                                                    zStackDirection,
                                                                                    stiffness,
                                                                                    filterSize,
                                                                                    offset,
                                                                                    depth);
            hsfwc.process();
        }

    }
}
