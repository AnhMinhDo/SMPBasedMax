package smpBasedMax;

import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.File;
import java.io.IOException;

public class HandleSingleFile {

    private String filePath;
    private final int stiffness;
    private final int filterSize;
    private final ZStackDirection zStackDirection;
    private final int offset;
    private final int depth;
    private float[] envMaxzValues;

    private ImagePlus projectedImage;
    private ImagePlus zMap;
    private ImagePlus projectedSMPImage;
    private ImagePlus smpZmap;
    private ImagePlus projectedSMPMIPImage;
    private ImagePlus smpMipZmap;

    private boolean hasProcessed = false;

    public HandleSingleFile(String filePath,
                            ZStackDirection zStackDirection,
                            int stiffness,
                            int filterSize,
                            int offset,
                            int depth) {
        this.filePath = filePath;
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
    }

    public void process(){
        performProcessing();
        saveOutputToFile();

    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public float[] getEnvMaxzValues() { return hasProcessed ? this.envMaxzValues : null;}

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
        this.envMaxzValues = smProjector.getEnvMax();
        hasProcessed = true;
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
            SmpBasedMaxUtil.savePostProcessImagePlus(this.projectedSMPImage, OutputTypeName.SMP,resultDir,fileName, stiffness, filterSize, offset, depth);
            SmpBasedMaxUtil.savePostProcessImagePlus(this.smpZmap, OutputTypeName.SMP_ZMAP,resultDir,fileName, stiffness, filterSize, offset, depth);
            // Save SMP depth-adjusted image and zMap
            if (depth != 0) {
            SmpBasedMaxUtil.savePostProcessImagePlus(this.projectedSMPMIPImage, OutputTypeName.SMPbasedMIP,resultDir,fileName, stiffness, filterSize, offset, depth);
            SmpBasedMaxUtil.savePostProcessImagePlus(this.smpMipZmap, OutputTypeName.SMPbasedMIP_ZMAP,resultDir,fileName, stiffness, filterSize, offset, depth);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
