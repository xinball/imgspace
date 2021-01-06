package top.xb.imgspace.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Photo implements Parcelable {
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

    protected Photo(Parcel in) {
        pid = in.readString();
        uid = in.readString();
        mid = in.readString();
        url = in.readString();
        uploadTime=new Date(in.readLong());
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(pid);
        parcel.writeString(uid);
        parcel.writeString(mid);
        parcel.writeString(url);
        parcel.writeLong(uploadTime.getTime());
    }
}
