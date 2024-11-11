package smpBasedMax;

import java.io.File;
import java.util.ArrayList;
import ij.io.OpenDialog;
import ij.io.DirectoryChooser;


public class SmpBasedMaxUtil {

    static public String[] handleSingleFile(){
        OpenDialog od = new OpenDialog("Choose .tiff File");
        String filePath=od.getPath();
        if (isTiffExtension(filePath)) {
            return new String[]{filePath};
        } else {
            return null;
        }
    }

    static public String[] handleMultipleFiles (){
        DirectoryChooser dc = new DirectoryChooser("Choose Directory");
        String dirPath = dc.getDirectory();
        String[] filePaths = listFilesInDirectory(dirPath);
        if (filePaths == null) return null;
        // check for .tiff file extension
        ArrayList<String> tiffFilePaths = new ArrayList<>();
        for (String filePath : filePaths) {
            if (isTiffExtension(filePath)) {
                tiffFilePaths.add(filePath);
            }
        }
        if (tiffFilePaths.isEmpty()) return null;
        return tiffFilePaths.toArray(new String[0]);
    }

    static public String[] listFilesInDirectory(String dirPath){
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            // Convert each file to its absolute path
            if (files == null) return null;
            String[] absolutePaths = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                absolutePaths[i] = files[i].getAbsolutePath();  // Get absolute path for each file
            }
            return absolutePaths;
        } else {
            return null;
        }
    }

    static public boolean isTiffExtension(String filePath){
        if (filePath == null) {
            return false;
        } else {
            String filePathLowerCase = filePath.toLowerCase();
            return filePathLowerCase.endsWith(".tiff") || filePathLowerCase.endsWith(".tif");
        }
    }

    public static String extractFilename(String path) {
        int lastSlashIndex = path.lastIndexOf(File.separator);  // Use File.separator for cross-platform support
        if (lastSlashIndex != -1) {
            return path.substring(lastSlashIndex + 1);  // Extract the part after the last slash
        }
        return path;  // If there is no slash, return the path itself (it might be just a file name)
    }

}
