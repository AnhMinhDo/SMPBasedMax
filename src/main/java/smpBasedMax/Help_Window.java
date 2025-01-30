package smpBasedMax;

import ij.gui.HTMLDialog;
import ij.plugin.PlugIn;

public class Help_Window implements PlugIn {
    @Override
    public void run(String arg) {
        String HELP_INSTRUCTION = "<h2>Process Mode:</h2>\n" +
                "<p><strong>Single File</strong>: Process only 1 file or 1 file with additional channels<br><strong>Multiple Files</strong>: Process multiple files or multiple files with respective additional channels for the same parameter set<br><strong>Interactive</strong>: used for parameter tuning, processed Image is shown in new window and updated when parameters are adjust the result are not saved; this is to ensure fast process and update.</p>\n" +
                "<h3>Direction of Z-Stack:</h3>\n" +
                "<p>The Z-stack slices are ordered based on the Z-position in the 3D space.<br><strong>IN</strong>: Top-to-Bottom: When the slices are taken from the top of the sample to the bottom, the Z-stack increases in value as you go deeper.<br><strong>OUT</strong>: When the slices are taken from the bottom of the sample to the top.</p>\n" +
                "<h3>Stiffness:</h3>\n" +
                "<p>This is the constraint for the minimum distance between consecutive peaks.<br>In the Smooth manifold process, signal peak is selected based on the distance with the nearest neighbor peaks.<br>High intensity peaks have higher priority.<br>The higher the stiffness, the fewer peaks are selected; result in less noisy projected image but also fewer details.</p>\n" +
                "<h3>Filter Size:</h3>\n" +
                "<p>Filter size for Gaussian smoothing filters to reduce noise after the raw smoothing process.</p>\n" +
                "<h3>Offset:</h3>\n" +
                "<p>Number to slices counting from the current smp slice to be performed MIP.<br>The higher the offset, the more detail from neighboring slice is capture.<br>This parameter is needed to be tuned for optimal result.</p>\n" +
                "<h3>Depth:</h3>\n" +
                "<p>Number of steps to be moved after applied offset; used to move the projected image up the surface or deep in the sample.</p>" ;
        new HTMLDialog("Help & Instructions",
                HELP_INSTRUCTION,
                false);

        }
}




