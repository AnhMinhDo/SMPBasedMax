package smpBasedMax;

import ij.WindowManager;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class MaxIntensityProjection {

    private ImagePlus imp;
    private int numberOfSlices;

    /**
     * Construction of MaxIntensityProjection with image to be projected.
     */
    public MaxIntensityProjection(ImagePlus imp) {
        this.imp = imp;
        this.numberOfSlices = imp.getNSlices();
    }

    public void setImage(ImagePlus imp) {
        this.imp = imp;
        this.numberOfSlices = imp.getNSlices();
    }

    /** perform actual projection */
    public ImagePlus doProjection() {
        FloatProcessor fp = new FloatProcessor(this.imp.getWidth(), this.imp.getHeight());
        ImageStack imgStack = this.imp.getStack();
        float[] fpixels = (float[]) fp.getPixels();
        int len = fpixels.length;
        for (int i = 0; i < len; i++) {
            fpixels[i] = -Float.MAX_VALUE;
        }
        for (int currentSlice = 1; currentSlice <= this.numberOfSlices; currentSlice++) {
            maxProjection((short[]) imgStack.getPixels(currentSlice), fpixels, len);
        }
        return makeOutputImage(imp, fp);
    }

    public void maxProjection(short[] pixels, float[] fpixels, int len) {
        for (int i = 0; i < len; i++) {
            if ((pixels[i] & 0xffff) > fpixels[i])
                fpixels[i] = pixels[i] & 0xffff;
        }
    }

    private ImagePlus makeOutputImage(ImagePlus imp, FloatProcessor fp) {
        int width = imp.getWidth();
        int height = imp.getHeight();
        float[] pixels = (float[])fp.getPixels();
        ImageProcessor oip;

        // Create output image consistent w/ type of input image.
        int size = pixels.length;
        oip = imp.getProcessor().createProcessor(width,height);
        short[] pixels16 = (short[])oip.getPixels();
        for(int i=0; i<size; i++) {
            pixels16[i] = (short) (pixels[i] + 0.5f);
        }
        // Adjust for display.
        // Calling this on non-ByteProcessors ensures image
        // processor is set up to correctly display image.
        oip.resetMinAndMax();
        return new ImagePlus(makeTitle(imp), oip);
    }

    private String makeTitle(ImagePlus imp) {
        String prefix = "MAX_";
        return WindowManager.makeUniqueName(prefix+imp.getTitle());
        }
}
