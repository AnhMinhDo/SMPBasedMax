package smpBasedMax;


public class HandleMultipleFile {

    private String[] filePaths;
    private int stiffness;
    private int filterSize;
    private ZStackDirection zStackDirection;
    private int offset;
    private int depth;


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
        HandleSingleFile hsf = new HandleSingleFile(filePaths[1], zStackDirection,
                stiffness, filterSize,
                offset, depth);
        for (int i = 0; i < filePaths.length; i++) {
          hsf.setFilePath(filePaths[i]);
          hsf.process();
        }
    }
}
