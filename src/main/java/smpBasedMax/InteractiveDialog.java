package smpBasedMax;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;
import ij.process.StackConverter;


public class InteractiveDialog {
    private ImagePlus inputImage;
    private final ZStackDirection zStackDirection;
    private int stiffness;
    private int filterSize;
    private int offset;
    private int depth;
//    private ImagePlus projectedImage;
    private ImagePlus zMap;
    private ImagePlus projectedSMPImage;
    private ImagePlus smpZmap;
    private ImagePlus projectedSMPMIPImage;
//    private ImagePlus smpMipZmap;
    private ImagePlus outputImage;
    private GenericDialog interactiveGd;


    public InteractiveDialog(String filePath,
                             ZStackDirection zStackDirection,
                             int stiffness,
                             int filterSize,
                             int offset,
                             int depth){
        // add parameters to private attributes
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;

        String[] validFilePath = SmpBasedMaxUtil.checkSingleFile(filePath);
        if (validFilePath == null) {
            IJ.showMessage("No file selected for Interactive Mode.");
            return;
        }
        // create imagePlus object fromm filePath
        inputImage = new ImagePlus(validFilePath[0]);
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
        projector.doProjection();
        zMap = projector.getZmap();
        // ZProjecting SMP
        SMProjection smProjector = new SMProjection(inputImage, zMap, stiffness, filterSize, zStackDirection, offset);
        projectedSMPImage = smProjector.doSMProjection();
        smpZmap = smProjector.getSMPZmap();
        // SMP-MIP if depth !=0
        SMP_MIP_Projection smpMipProjector = new SMP_MIP_Projection(inputImage, smpZmap, depth, zStackDirection);
        projectedSMPMIPImage = smpMipProjector.doProjection();
//        smpMipZmap = smpMipProjector.getZmap();
        outputImage = depth>0 ? projectedSMPMIPImage : projectedSMPImage;
    }

    public void show(){
        outputImage.show();
        while(true){
        interactiveGd = GUI.newNonBlockingDialog("Interactive Mode");
        interactiveGd.addNumericField("Enter envelope stiffness [pixels]:  ", stiffness, 0);
        interactiveGd.addNumericField("Enter final filter size [pixels]: ", filterSize, 0);
        interactiveGd.addNumericField("Offset: N planes above (+) or below (-) blanket [pixels]:  ", offset, 0);
        interactiveGd.addNumericField("Depth: MIP for N pixels into blanket [pixels]:  ", depth, 0);
        interactiveGd.showDialog();
        if (interactiveGd.wasCanceled()) return;
        this.stiffness = (int)interactiveGd.getNextNumber();
        this.filterSize = (int)interactiveGd.getNextNumber();
        this.offset = (int)interactiveGd.getNextNumber();
        this.depth = (int)interactiveGd.getNextNumber();
        // ZProjecting SMP
        SMProjection smProjector = new SMProjection(inputImage, zMap, stiffness, filterSize, zStackDirection, offset);
        this.projectedSMPImage = smProjector.doSMProjection();
        this.smpZmap = smProjector.getSMPZmap();
        // SMP-MIP if depth !=0
            if(depth != 0) {
                SMP_MIP_Projection smpMipProjector = new SMP_MIP_Projection(inputImage, smpZmap, depth, zStackDirection);
                this.projectedSMPMIPImage = smpMipProjector.doProjection();
            }
        ImageProcessor newOutputImageProcessor = depth>0 ? this.projectedSMPMIPImage.getProcessor() : this.projectedSMPImage.getProcessor();
        outputImage.setProcessor(newOutputImageProcessor);
        outputImage.updateAndDraw();

        }
    }

}
