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
            return dir.list();
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
}
