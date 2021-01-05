package top.xb.imgspace.bean;

import java.util.Date;

public class Photo {
    public String pid;//图片编号：2位
    public String uid;//用户编号：6位
    public String mid;//消息编号：时间12位+随机1位
    public String url;//文件本地路径：uid+mid+pid+后缀格式
    public Date uploadTime;//上传时间

    public Photo() {
    }

    public Photo(String pid, String uid, String mid, String url, Date uploadTime) {
        this.pid = pid;
        this.uid = uid;
        this.mid = mid;
        this.url = url;
        this.uploadTime = uploadTime;
    }
}
