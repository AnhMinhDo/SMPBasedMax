package smpBasedMax;

import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.File;
import java.io.IOException;

public class HandleSingleFile {

    private String filePath;
    private boolean useSecondFile;
    private String secondFilePath;
    private boolean useThirdFile;
    private String thirdFilePath;
    private int stiffness;
    private int filterSize;
    private ZStackDirection zStackDirection;
    private int offset;
    private int depth;
    private ImagePlus projectedImage;
    private ImagePlus zMap;
    private ImagePlus projectedSMPImage;
    private ImagePlus smpZmap;
    private ImagePlus projectedSMPMIPImage;
    private ImagePlus smpMipZmap;

    public HandleSingleFile(String filePath,
                            boolean useSecondFile,
                            String secondFilePath,
                            boolean useThirdFile,
                            String thirdFilePath) {
        this.filePath = filePath;
        this.useSecondFile = useSecondFile;
        this.secondFilePath = secondFilePath;
        this.useThirdFile = useThirdFile;
        this.thirdFilePath = thirdFilePath;
    }

    public void process(){
        performProcessing();
        saveOutputToFile();
    }

    private void performProcessing(){
        // create imagePlus object from filePath
        ImagePlus inputImage = new ImagePlus(this.filePath);
        inputImage = SmpBasedMaxUtil.preProcessInputImage(inputImage);
        // ZProjecting MIP
        MaxIntensityProjection projector = new MaxIntensityProjection(inputImage);
        this.projectedImage = projector.doProjection();
        this.zMap = projector.getZmap();
        // ZProjecting SMP
        SMProjection smProjector = new SMProjection(inputImage, zMap, stiffness, filterSize, zStackDirection, offset);
        this.projectedSMPImage = smProjector.doSMProjection();
        this.smpZmap = smProjector.getSMPZmap();
        // SMP-MIP if depth !=0
        SMP_MIP_Projection smpMipProjector = new SMP_MIP_Projection(inputImage, smpZmap, depth, zStackDirection);
        this.projectedSMPMIPImage = smpMipProjector.doProjection();
        this.smpMipZmap = smpMipProjector.getZmap();
    }

    private void saveOutputToFile(){
        try {
            // prepare the directory for output
            String resultDir = SmpBasedMaxUtil.createResultDir(filePath,
                    zStackDirection,
                    stiffness,
                    filterSize,
                    offset,
                    depth);
            String fileName = SmpBasedMaxUtil.extractFilename(filePath);
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
