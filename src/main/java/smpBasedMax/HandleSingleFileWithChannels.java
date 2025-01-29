package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;

import java.io.File;


public class HandleSingleFileWithChannels {
    private File[] otherChannels;
    private File mainChannel;
    private File subDir;
    private ZStackDirection zStackDirection;
    private int stiffness;
    private int filterSize;
    private int offset;
    private int depth;


    public HandleSingleFileWithChannels(File dirPath,
                               ZStackDirection zStackDirection,
                               int stiffness,
                               int filterSize,
                               int offset,
                               int depth) {
        // get main file and subdirectory
        File[] mainFileAndSubDir = dirPath.listFiles();
        //handle empty directory
        if (mainFileAndSubDir == null) {IJ.showMessage("No files found in " + dirPath); return;}
        // Ensure the directory only have one main file and one subdirectory
            if (mainFileAndSubDir.length != 2) {
                IJ.showMessage("The directory is not properly organized as required");
                return;
            }
        // get other mainChannel and subdirectory
        for (File f : mainFileAndSubDir) {
            if (f.isDirectory()) {
                this.subDir = f;
            } else {
                this.mainChannel = f;
            }
        }
        // get other channels in subdirectory
        if (subDir != null) {
            this.otherChannels = subDir.listFiles();
        } else {IJ.showMessage("No additional channels are found in the subdirectory"); return;}
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
    }
    public void process(){
        performProcessing();
    }

    private void performProcessing(){
        HandleSingleFile hsf = new HandleSingleFile(mainChannel.getAbsolutePath(), zStackDirection, stiffness, filterSize, offset, depth);
        hsf.process();
        float[] envMaxzValues = hsf.getEnvMaxzValues();
        File outputDir = new File(subDir.getAbsolutePath() + File.separator + "output");
        for(File otherChannel : this.otherChannels) {
            processOtherChannel(outputDir,
                                otherChannel,
                                envMaxzValues,
                                this.zStackDirection,
                                this.stiffness,
                                this.filterSize,
                                this.offset,
                                this.depth);
        }
    }

    private static void processOtherChannel(File outputDir,
                                            File originalChannel,
                                            float[] envMaxzValues,
                                            ZStackDirection zStackDirection,
                                            int stiffness,
                                            int filterSize,
                                            int offset,
                                            int depth){
        ImagePlus channelImage = new ImagePlus(originalChannel.getAbsolutePath());
        ManifoldBypassProjection mbProjector = new ManifoldBypassProjection(channelImage,envMaxzValues,offset);
        ImagePlus projection = mbProjector.doManifoldBypassProjection();
        ImagePlus zMapProjection = mbProjector.getManifoldBypassZmap();
        if(depth != 0) {
            SMP_MIP_Projection smpMipProjector = new SMP_MIP_Projection(channelImage, zMapProjection, depth, zStackDirection);
            ImagePlus projectedSMPMIPImage = smpMipProjector.doProjection();
            ImagePlus smpMipZmap = smpMipProjector.getZmap();
            saveOutputOtherChannel(outputDir,
                                    originalChannel,
                                    stiffness,
                                    filterSize,
                                    offset,
                                    depth,
                                    projection,
                                    zMapProjection,
                                    projectedSMPMIPImage,
                                    smpMipZmap);
        } else {
            saveOutputOtherChannel(outputDir,
                                    originalChannel,
                                    stiffness,
                                    filterSize,
                                    offset,
                                    depth,
                                    projection,
                                    zMapProjection);
        }

    }

    private static void saveOutputOtherChannel(File outputDir,
                                               File originalChannel,
                                               int stiffness,
                                               int filterSize,
                                               int offset,
                                               int depth,
                                               ImagePlus projectedSMPImage,
                                               ImagePlus smpZmap){
        saveOutputOtherChannel(outputDir,
                            originalChannel,
                            stiffness,
                            filterSize,
                            offset,
                            depth,
                            projectedSMPImage,
                            smpZmap,
                            null,
                            null);
    }

    private static void saveOutputOtherChannel(File outputDir,
                                               File originalChannel,
                                              int stiffness,
                                              int filterSize,
                                              int offset,
                                              int depth,
                                              ImagePlus projectedSMPImage,
                                              ImagePlus smpZmap,
                                              ImagePlus projectedSMPMIPImage,
                                              ImagePlus smpMipZmap){
        String resultDir = outputDir.getAbsolutePath();
        String fileName = originalChannel.getName();
        if (projectedSMPMIPImage == null) {
            // Save SMP projected image and zMap
            SmpBasedMaxUtil.savePostProcessImagePlus(projectedSMPImage, OutputTypeName.SMP,resultDir,fileName, stiffness, filterSize, offset, depth);
            SmpBasedMaxUtil.savePostProcessImagePlus(smpZmap, OutputTypeName.SMP_ZMAP,resultDir,fileName, stiffness, filterSize, offset, depth);
        } else {
            // Save the depth-adjusted output
            SmpBasedMaxUtil.savePostProcessImagePlus(projectedSMPMIPImage, OutputTypeName.SMPbasedMIP,resultDir,fileName, stiffness, filterSize, offset, depth);
            SmpBasedMaxUtil.savePostProcessImagePlus(smpMipZmap, OutputTypeName.SMPbasedMIP_ZMAP,resultDir,fileName, stiffness, filterSize, offset, depth);

        }
    }


}
