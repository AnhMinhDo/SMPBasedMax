package smpBasedMax;

import ij.IJ;

import java.io.File;

public class HandleMulitpleFilesWithChannels {
    File[] ArrayFileWithChannels;
    ZStackDirection zStackDirection;
    int stiffness;
    int filterSize;
    int offset;
    int depth;
    public HandleMulitpleFilesWithChannels(File dirPath,
                                           ZStackDirection zStackDirection,
                                           int stiffness,
                                           int filterSize,
                                           int offset,
                                           int depth) {
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
        this.ArrayFileWithChannels = dirPath.listFiles(File::isDirectory);
        if (this.ArrayFileWithChannels == null) {
            IJ.showMessage("No subdirectory in " + dirPath);
        }
    }

    public void process() {
        performProcessing();
    }

    private void performProcessing(){
        for(File fileWithChannel : this.ArrayFileWithChannels) {
            HandleSingleFileWithChannels hsfwc = new HandleSingleFileWithChannels(fileWithChannel,
                    this.zStackDirection,
                    this.stiffness,
                    this.filterSize,
                    this.offset,
                    this.depth);
            hsfwc.process();
        }
    }
}
