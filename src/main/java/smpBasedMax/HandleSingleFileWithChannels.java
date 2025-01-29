package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class HandleSingleFileWithChannels {
    File[] mainFileAndSubDir;
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
        this.mainFileAndSubDir = dirPath.listFiles();
        //handle empty directory
        if (mainFileAndSubDir == null) {IJ.showMessage("No files found in " + dirPath); return;}
        // get mainChannel and subdirectory
        for (File f : mainFileAndSubDir) {
            if (f.isDirectory() && f.getName().equals("Channels")) {
                this.subDir = f;
            } else if (!f.isDirectory() && SmpBasedMaxUtil.isTiffExtension(f.getName())) {
                this.mainChannel = f;
            }
        }
        // Return error when there is no Channels dir
        if(this.subDir == null) {
            IJ.showMessage("No Directory with the name Channels in " + dirPath + System.lineSeparator() +
                "Please add all additional channel images in a subdirectory with the name \"Channels\"," + System.lineSeparator()+
                    "and this subdirectory should be placed inside " + dirPath);
            return;
        }
        // get other channels in subdirectory; condition: they are not a directory and tif files
        this.otherChannels = subDir.listFiles(pathname -> pathname.isFile() && SmpBasedMaxUtil.isTiffExtension(pathname.getName()));
        if (otherChannels == null) {IJ.showMessage("No tif file found in " + subDir.getAbsolutePath()); return;}
        // assign other attributes
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
    }
    public void process(){
        if(this.subDir != null &&
        this.mainFileAndSubDir != null &&
        this.otherChannels != null) {
            performProcessing();
        }
    }

    private void performProcessing(){
        HandleSingleFile hsf = new HandleSingleFile(mainChannel.getAbsolutePath(), zStackDirection, stiffness, filterSize, offset, depth);
        hsf.process();
        float[] envMaxzValues = hsf.getEnvMaxzValues();
        Path outDirPath = Paths.get(subDir.getAbsolutePath() + File.separator + "output");
        if(Files.notExists(outDirPath)) {
            try {
                Files.createDirectories(outDirPath);
            } catch (IOException e) {
                IJ.showMessage("Could not create output directory" + System.lineSeparator() +
                        e.getMessage());
                return;
            }
        }
        File outDir = outDirPath.toFile();
        for(File otherChannel : this.otherChannels) {
            processOtherChannel(outDir,
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
        SmpBasedMaxUtil.preProcessInputImage(channelImage);
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
