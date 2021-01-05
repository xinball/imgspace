package top.xb.imgspace.utils;

public class CheckUtil {
    public static boolean isNumeric(String str,int min,int max){
        String reg="";
        if(max==-1)
            reg="\\d{"+min+",}";
        else
            reg="\\d{"+min+","+max+"}";
        if(str.matches(reg))
            return true;
        return false;
    }
    public static boolean isText(String str,int min,int max){
        String reg="";
        if(max==-1)
            reg="\\w{"+min+",}";
        else
            reg="\\w{"+min+","+max+"}";
        if(str.matches(reg))
            return true;
        return false;
    }
}
