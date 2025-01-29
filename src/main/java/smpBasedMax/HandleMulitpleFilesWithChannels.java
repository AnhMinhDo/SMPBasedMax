package smpBasedMax;

import ij.IJ;

import java.io.File;

public class HandleMulitpleFilesWithChannels {

    public HandleMulitpleFilesWithChannels(File dirPath,
                                           ZStackDirection zStackDirection,
                                           int stiffness,
                                           int filterSize,
                                           int offset,
                                           int depth) {
        File[] ArrayFileWithChannels = dirPath.listFiles(File::isDirectory);
        if (ArrayFileWithChannels == null) {
            IJ.showMessage("No subdirectory in " + dirPath); return;}
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
