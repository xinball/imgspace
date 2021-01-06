package top.xb.imgspace.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import top.xb.imgspace.R;
import top.xb.imgspace.utils.HTTPSTrustManager;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ImgSpaceApplication extends Application {

    public static SharedPreferences userInfo;
    public static SharedPreferences msgInfo;
    public static SharedPreferences cacheSharedPreferences;

    public static Boolean isLoggedIn(){
        return userInfo.getString("uid", null)!=null&&userInfo.getString("pwd", null)!=null;
    }
    public static Boolean isFirstStart(){
        return userInfo.getInt("start", 1)==1;
    }
    public static String getUid(){
        return userInfo.getString("uid", null);
    }
    public static String getPwd(){
        return userInfo.getString("pwd", null);
    }
    public static String getName(){
        return userInfo.getString("name", null);
    }
    public static String getTel(){
        return userInfo.getString("tel", null);
    }
    public static boolean getRememberPwd(){
        return userInfo.getBoolean("rememberPwd", true);
    }

    public static void clear(Context context){
        SharedPreferences.Editor editor = ImgSpaceApplication.userInfo.edit();
        editor.remove("uid");
        editor.remove("pwd");
        editor.remove("name");
        editor.remove("tel");
        editor.remove("rememberPwd");
        editor.apply();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        userInfo = getApplicationContext().getSharedPreferences("UserInfo", Activity.MODE_PRIVATE);
        msgInfo = getApplicationContext().getSharedPreferences("MsgInfo", Activity.MODE_PRIVATE);
        //获取缓存
        cacheSharedPreferences = getSharedPreferences("MainCache", Activity.MODE_PRIVATE);
        /*
        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }
            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
        */

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_blue_500).sizeDp(56);
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });
    }



    public static void allowAllSSL(){
        try {
            TrustManager[] trustManagers =new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {

                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType){
                }
            }};
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null,trustManagers,new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
}