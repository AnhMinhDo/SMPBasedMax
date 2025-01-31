package smpBasedMax;

import ij.gui.HTMLDialog;
import ij.plugin.PlugIn;

public class Help_Window implements PlugIn {
    @Override
    public void run(String arg) {
        String HELP_INSTRUCTION =
                "<h2>Features:</h2>\n" +
                "<h3>Process Mode:</h3>\n" +
                "<p><strong>Single File</strong>: Process only 1 file or 1 file with additional channels\n <strong>Multiple Files</strong>: Process multiple files or multiple files with respective additional channels for the same parameter set\n <strong>Interactive</strong>: used for parameter tuning, processed Image is shown in new window and updated when parameters are adjust the result are not saved; this is to ensure fast process and update.</p>\n" +
                "<h3>Direction of Z-Stack:</h3>\n" +
                "<p>The Z-stack slices are ordered based on the Z-position in the 3D space. <strong>IN</strong>: Top-to-Bottom: When the slices are taken from the top of the sample to the bottom, the Z-stack increases in value as you go deeper. <strong>OUT</strong>: When the slices are taken from the bottom of the sample to the top.</p>\n" +
                "<h3>Stiffness:</h3>\n" +
                "<p>This is the constraint for the minimum distance between consecutive peaks. In the Smooth manifold process, signal peak is selected based on the distance with the nearest neighbor peaks. High intensity peaks have higher priority. The higher the stiffness, the fewer peaks are selected; result in less noisy projected image but also fewer details.</p>\n" +
                "    <h3>Filter Size:</h3>\n" +
                "<p>Filter size for Gaussian smoothing filters to reduce noise after the raw smoothing process.</p>\n" +
                "    <h3>Offset:</h3>\n" +
                "<p>Number to slices counting from the current smp slice to be performed MIP. The higher the offset, the more detail from neighboring slice is capture. This parameter is needed to be tuned for optimal result.</p>\n" +
                "<h3>Depth:</h3>\n" +
                "<p>Number of steps to be moved after applied offset; used to move the projected image up the surface or deep in the sample</p>\n" +
                "<h3>Directory for MULTIPLE FILES:</h3>\n" +
                "<p>Choose the directory for MULTIPLE_FILE MODE</p>\n" +
                "<h3>File path for SINGLE FILE:</h3>\n" +
                "<p>Choose the file path for SINGLE_FILE MODE</p>\n" +
                "<h3>Project Additional channels:</h3>\n" +
                "<p>Tick this if the smp z-map is used to applied for other channels of the same sample or image with the exact same dimension of the main channel. <strong>IMPORTANT</strong>: Additional channel images must be in a &quot;Channels&quot; folder (the folder with the exact name &quot;Channels&quot;), located alongside the main channel image.</p>\n" +
                "<h3>Additional channel directory:</h3>\n" +
                "<p>SINGLE_FILE and Additional channels: specify the sample folder</p>\n" +
                "<p>MULTIPLE_FILES and Additional channels: specify the parent folder(the folder contains multiple sample Folders)</p>" ;
        new HTMLDialog("Help & Instructions",
                HELP_INSTRUCTION,
                false);

        }
}




