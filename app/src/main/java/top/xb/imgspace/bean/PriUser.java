package top.xb.imgspace.bean;

import java.text.ParseException;
import java.util.Date;

import top.xb.imgspace.utils.DisplayUtil;

public class PriUser extends User{
    public PriUser() {
    }

    public String pwd;//密码
    public Date regTime;//注册时间
    public String regIp;//注册ip
    public PriUser(String uid,String name,String pwd, String email, String tel, String nickname, String realname, String avatar, String slogan, int sex, Date birthday, String livead, Date regTime, String regIp) {
        super(uid, name,email, tel, nickname, realname, avatar, slogan, sex, birthday, livead);
        this.pwd=pwd;
        this.regTime = regTime;
        this.regIp = regIp;
    }
    public PriUser(String uid,String name,String pwd, String email, String tel, String nickname, String realname, String avatar, String slogan, String sex, String birthday, String livead, String regTime, String regIp) throws ParseException {
        super(uid, name,email, tel, nickname, realname, avatar, slogan, sex, birthday, livead);
        this.pwd=pwd;
        if(!regTime.equals(""))
            this.regTime = DisplayUtil.dateFormat.parse(regTime);
        else
            this.regTime=null;
        this.regIp = regIp;
    }
}
