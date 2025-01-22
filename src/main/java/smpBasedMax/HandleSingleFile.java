package smpBasedMax;

import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.File;
import java.io.IOException;

public class HandleSingleFile {

    private String filePath;
    private final boolean useSecondFile;
    private final String secondFilePath;
    private final boolean useThirdFile;
    private final String thirdFilePath;
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

    private ImagePlus projectedSMPSecondImage;
    private ImagePlus smpSecondImageZmap;
    private ImagePlus projectedSMPMIPSecondImage;
    private ImagePlus smpMipSecondImageZmap;

    private ImagePlus projectedSMPThirdImage;
    private ImagePlus smpThirdImageZmap;
    private ImagePlus projectedSMPMIPThirdImage;
    private ImagePlus smpMipThirdImageZmap;

    public HandleSingleFile(String filePath,
                            ZStackDirection zStackDirection,
                            int stiffness,
                            int filterSize,
                            int offset,
                            int depth) {
        this.filePath = filePath;
        this.zStackDirection = zStackDirection;
        this.useSecondFile = false;
        this.secondFilePath = "";
        this.useThirdFile = false;
        this.thirdFilePath = "";
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
    }

    public HandleSingleFile(String filePath,
                            ZStackDirection zStackDirection,
                            int stiffness,
                            int filterSize,
                            int offset,
                            int depth,
                            boolean useSecondFile,
                            String secondFilePath,
                            boolean useThirdFile,
                            String thirdFilePath) {
        this.filePath = filePath;
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
        this.useSecondFile = useSecondFile;
        this.secondFilePath = secondFilePath;
        this.useThirdFile = useThirdFile;
        this.thirdFilePath = thirdFilePath;
    }

    public void process(){
        performProcessing();
        saveOutputToFile();
        if(useSecondFile){saveOutputSecondFile();}
        if(useThirdFile){saveOutputThirdFile();}
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
        this.envMaxzValues = smProjector.getEnvMax();
        this.smpZmap = smProjector.getSMPZmap();
        // SMP-MIP if depth !=0
        SMP_MIP_Projection smpMipProjector = new SMP_MIP_Projection(inputImage, smpZmap, depth, zStackDirection);
        this.projectedSMPMIPImage = smpMipProjector.doProjection();
        this.smpMipZmap = smpMipProjector.getZmap();
        // Handle projection for second file using first file SMP
        if (useSecondFile){
            ImagePlus secondImage = new ImagePlus(this.secondFilePath);
            secondImage = SmpBasedMaxUtil.preProcessInputImage(secondImage);
            // SMP Second file
            ManifoldBypassProjection mbpSecondFileProjector = new ManifoldBypassProjection(secondImage,this.envMaxzValues,offset);
            this.projectedSMPSecondImage = mbpSecondFileProjector.doManifoldBypassProjection();
            this.smpSecondImageZmap = mbpSecondFileProjector.getManifoldBypassZmap();
            // SMP-MIP Second file
            SMP_MIP_Projection smpMipSecondProjector = new SMP_MIP_Projection(secondImage, smpSecondImageZmap, depth, zStackDirection);
            this.projectedSMPMIPSecondImage = smpMipSecondProjector.doProjection();
            this.smpMipSecondImageZmap = smpMipSecondProjector.getZmap();
        }
        // Handle projection for third file using first file SMP
        if (useThirdFile){
            ImagePlus thirdImage = new ImagePlus(this.thirdFilePath);
            thirdImage = SmpBasedMaxUtil.preProcessInputImage(thirdImage);
            // SMP third file
            ManifoldBypassProjection mbpThirdFileProjector = new ManifoldBypassProjection(thirdImage,this.envMaxzValues,offset);
            this.projectedSMPThirdImage = mbpThirdFileProjector.doManifoldBypassProjection();
            this.smpThirdImageZmap = mbpThirdFileProjector.getManifoldBypassZmap();
            // SMP-MIP third file
            SMP_MIP_Projection smpMipThirdProjector = new SMP_MIP_Projection(thirdImage, smpThirdImageZmap, depth, zStackDirection);
            this.projectedSMPMIPThirdImage = smpMipThirdProjector.doProjection();
            this.smpMipThirdImageZmap = smpMipThirdProjector.getZmap();
        }
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

    private  void saveOutputSecondFile(){
        try {
            // prepare the directory for output
            String resultSecondDir = SmpBasedMaxUtil.createResultDir(secondFilePath,
                                                                    zStackDirection,
                                                                    stiffness,
                                                                    filterSize,
                                                                    offset,
                                                                    depth);
            String secondFileName = SmpBasedMaxUtil.extractFilename(secondFilePath);
            // Save SMP projected second image and zMap
            FileSaver projectedSMPSecondImageTiff = new FileSaver(projectedSMPSecondImage);
            FileSaver smpSecondImageZmapTiff = new FileSaver(smpSecondImageZmap);
            projectedSMPSecondImageTiff.saveAsTiff(resultSecondDir + File.separator +
                    secondFileName + "_SMP" + "_stiffness" + stiffness + "_filterSize" + filterSize +
                    "_offSet" + offset + ".tif");
            smpSecondImageZmapTiff.saveAsTiff(resultSecondDir + File.separator +
                    secondFileName + "_SMP_zmap" + "_stiffness" + stiffness +
                    "_filterSize" + filterSize + "_offSet" + offset + ".tif");
            // Save SMP depth-adjusted image and zMap
            if (depth != 0) {
                FileSaver projectedSMPMIPSecondImageTiff = new FileSaver(projectedSMPMIPSecondImage);
                FileSaver smpMipSecondImageZmapTiff = new FileSaver(smpMipSecondImageZmap);
                projectedSMPMIPSecondImageTiff.saveAsTiff(resultSecondDir + File.separator +
                        secondFileName + "_SMPbasedMIP" + "_stiffness" + stiffness +
                        "_filterSize" + filterSize + "_offSet" + offset +
                        "_depth" + depth + ".tif");
                smpMipSecondImageZmapTiff.saveAsTiff(resultSecondDir + File.separator +
                        secondFileName + "_SMPbasedMIP_zmap" + "_stiffness" + stiffness +
                        "_filterSize" + filterSize + "_offSet" + offset +
                        "_depth" + depth + ".tif");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private  void saveOutputThirdFile(){
        try {
            // prepare the directory for output
            String resultThirdDir = SmpBasedMaxUtil.createResultDir(thirdFilePath,
                    zStackDirection,
                    stiffness,
                    filterSize,
                    offset,
                    depth);
            String thirdFileName = SmpBasedMaxUtil.extractFilename(thirdFilePath);
            // Save SMP projected third image and zMap
            FileSaver projectedSMPThirdImageTiff = new FileSaver(projectedSMPThirdImage);
            FileSaver smpThirdImageZmapTiff = new FileSaver(smpThirdImageZmap);
            projectedSMPThirdImageTiff.saveAsTiff(resultThirdDir + File.separator +
                    thirdFileName + "_SMP" + "_stiffness" + stiffness + "_filterSize" + filterSize +
                    "_offSet" + offset + ".tif");
            smpThirdImageZmapTiff.saveAsTiff(resultThirdDir + File.separator +
                    thirdFileName + "_SMP_zmap" + "_stiffness" + stiffness +
                    "_filterSize" + filterSize + "_offSet" + offset + ".tif");
            // Save SMP depth-adjusted image and zMap
            if (depth != 0) {
                FileSaver projectedSMPMIPThirdImageTiff = new FileSaver(projectedSMPMIPThirdImage);
                FileSaver smpMipThirdImageZmapTiff = new FileSaver(smpMipThirdImageZmap);
                projectedSMPMIPThirdImageTiff.saveAsTiff(resultThirdDir + File.separator +
                        thirdFileName + "_SMPbasedMIP" + "_stiffness" + stiffness +
                        "_filterSize" + filterSize + "_offSet" + offset +
                        "_depth" + depth + ".tif");
                smpMipThirdImageZmapTiff.saveAsTiff(resultThirdDir + File.separator +
                        thirdFileName + "_SMPbasedMIP_zmap" + "_stiffness" + stiffness +
                        "_filterSize" + filterSize + "_offSet" + offset +
                        "_depth" + depth + ".tif");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
