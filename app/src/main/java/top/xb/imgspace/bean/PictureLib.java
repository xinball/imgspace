package top.xb.imgspace.bean;

public class PictureLib {


    private String name;
    private  int imageId;

    public PictureLib(String name, int imageId) {
        this.name = name;
        this.imageId=imageId;
    }
    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }


}
