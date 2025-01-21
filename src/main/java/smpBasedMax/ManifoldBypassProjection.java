package smpBasedMax;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

public class ManifoldBypassProjection {
    private final ImagePlus originalImage;
    private final FloatProcessor envMax;
    private int offSet;
    private MaxIntensityProjection projector;

    public ManifoldBypassProjection(ImagePlus originalImage,
                                    FloatProcessor envMax,
                                    int offSet){
        this.originalImage = originalImage;
        this.envMax = envMax;
        this.offSet = offSet;
    }

    public ImagePlus doManifoldBypassProjection() {
        float[] envMaxzValues = (float[])this.envMax.getPixels();
        ImageStack imageStackAfterApplySmoothSheet = SMProjection.smpProjection(this.originalImage,envMaxzValues, this.offSet);
        ImagePlus imageAfterApplySmoothSheet = new ImagePlus(this.originalImage.getTitle(), imageStackAfterApplySmoothSheet);
        this.projector = new MaxIntensityProjection(imageAfterApplySmoothSheet);
        return this.projector.doProjection();
    }

    public ImagePlus getManifoldBypassZmap(){
        return projector.getZmap();
    }
}
