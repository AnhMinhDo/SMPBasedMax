package smpBasedMax;


public class HandleMultipleFile {

    private final String[] filePaths;
    private final int stiffness;
    private final int filterSize;
    private final ZStackDirection zStackDirection;
    private final int offset;
    private final int depth;


    public HandleMultipleFile(String[] filePaths,
                              ZStackDirection zStackDirection,
                              int stiffness,
                              int filterSize,
                              int offset,
                              int depth) {
        this.filePaths = filePaths;
        this.zStackDirection = zStackDirection;
        this.stiffness = stiffness;
        this.filterSize = filterSize;
        this.offset = offset;
        this.depth = depth;
    }

    public void process(){
        HandleSingleFile hsf = new HandleSingleFile(filePaths[0], zStackDirection,
                stiffness, filterSize,
                offset, depth);
        for (String filePath : filePaths) {
            hsf.setFilePath(filePath);
            hsf.process();
        }
    }
}
