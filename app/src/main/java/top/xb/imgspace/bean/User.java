package top.xb.imgspace.bean;

import android.content.IntentFilter;

import java.text.ParseException;
import java.util.Date;

import top.xb.imgspace.utils.DisplayUtil;

public class User {
    public String uid;//用户编号：6位
    public String name;//用户名
    public String email;//电子邮箱
    public String tel;//手机号码

    public String nickname;//用户昵称
    public String realname;//真名
    public String avatar;//头像
    public String slogan;//个签
    public int sex;//性别
    public Date birthday;//生日
    public String livead;//住址

    public User() {
    }

    public User(String uid, String name, String email, String tel, String nickname, String realname, String avatar, String slogan, int sex, Date birthday, String livead) {
        this.uid = uid;
        this.name=name;
        this.email = email;
        this.tel = tel;
        this.nickname = nickname;
        this.realname = realname;
        this.avatar = avatar;
        this.slogan = slogan;
        this.sex = sex;
        this.birthday = birthday;
        this.livead = livead;
    }
    public User(String uid,String name, String email, String tel, String nickname, String realname, String avatar, String slogan, String sex, String birthday, String livead) throws ParseException {
        this.uid = uid;
        this.name=name;
        this.email = email;
        this.tel = tel;
        this.nickname = nickname;
        this.realname = realname;
        this.avatar = avatar;
        this.slogan = slogan;
        this.sex = Integer.parseInt(sex);
        if(!birthday.equals("")){
            this.birthday = DisplayUtil.dateFormat.parse(birthday);
        }else{
            this.birthday=null;
        }
        this.livead = livead;
    }
}
