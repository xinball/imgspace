package top.xb.imgspace.config;

import android.os.Environment;

public class APIAddress
{
	public static final String WEBSITE_PATH = "/imgsite"; //Website path, You can leave it blank in most cases.
	public static final String API_KEY = "12450"; //Application key you set in ```config.php```
	public static final String API_SECRET = "b40484df0ad979d8ba7708d24c301c38"; //Application secret you set in ```config.php```

	public static String LocalcachePath= "/xinball/img/local/";
	public static String WebcachePath= "/xinball/img/web/";
	//活动被打开？
	public static boolean StartActivity=false;
	public static boolean MainActivity=false;
	public static boolean LoginActivity=false;
	public static boolean ReguserActivity=false;
    //Real
    public static final String DOMAIN_NAME = "http://nzq.xinball.top:8080"+ WEBSITE_PATH; // Main domain name
    public static final String BASIC_API_URL = "http://nzq.xinball.top:8080" + WEBSITE_PATH; // Domain name of API
	public static final String SEND_URL = DOMAIN_NAME+"/index"; //

	//中等头像地址
	public static String MIDDLE_AVATAR_URL(String userID, String avatarSize){
		return DOMAIN_NAME + "/files/avatar/"+ avatarSize +"/" + userID +".png";
	}

	//首页帖子列表API地址
	public static String HOME_URL(int targetPage){
		return BASIC_API_URL + "/page/"+ targetPage;
	}

	public static String TOPIC_URL(int topicID, int targetPage){
		return BASIC_API_URL + "/t/" + topicID + "-" + targetPage;
	}

	//验证码
	public static final String VERIFICATION_CODE = BASIC_API_URL + "/seccode.php";

	//登陆
	public static final String LOGIN_URL = BASIC_API_URL + "/login";

	//注册
	public static final String REGISTER_URL = BASIC_API_URL + "/register";

	//获取消息提醒
	public static final String NOTIFICATIONS_URL = BASIC_API_URL + "/notifications";

	//推送接口，维护一个长连接
	public static final String PUSH_SERVICE_URL = BASIC_API_URL + "/json/get_notifications";

	//创建新主题接口
	public static final String NEW_URL = BASIC_API_URL + "/new";

	//回复接口
	public static final String REPLY_URL = BASIC_API_URL + "/reply";
}
