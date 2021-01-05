package top.xb.imgspace.bean;

import java.util.Date;

public class Message {
    public Message() {
    }
    public String uid;//用户编号：6位
    public String mid;//消息编号：时间12位+随机1位
    public String content;//消息内容
    public String key;//消息内容
    public Date sendTime;//发送时间
    public Date alterTime;//修改时间

    public Message(String uid, String mid, String content, String key, Date sendTime, Date alterTime) {
        this.uid = uid;
        this.mid = mid;
        this.content = content;
        this.key = key;
        this.sendTime = sendTime;
        this.alterTime = alterTime;
    }

}
