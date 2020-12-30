package top.xb.imgspace.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import top.xb.imgspace.LoginActivity;
import top.xb.imgspace.application.CarbonForumApplication;
import top.xb.imgspace.config.APIAddress;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private Integer connectTimeout = null;
    private Integer socketTimeout = null;
    private String proxyHost = null;
    private Integer proxyPort = null;
    private static String charset = "utf-8";
    private static final String boundary = "*****";

    // Post方法访问服务器，返回json对象
    public static JSONObject postRequest(Context context, String SendData, Map<String, File> files) {
        try {
            String urlStr=APIAddress.SEND_URL;
            Log.d("Post URL:", urlStr);
            URL url = new URL(urlStr);
            //String parameterString = buildParameterString(parameterMap, loginRequired);
            Log.d("Post parameter:", SendData);

            HttpURLConnection con = (HttpURLConnection)  url.openConnection();
            /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            /* 设定传送的method=POST */
            con.setRequestMethod("POST");
            /* setRequestProperty */
            con.setConnectTimeout(10*1000);//超时时间
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", charset);


            String end = "/r/n";
            String hyphens = "--";
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            /* 设定DataOutputStream 发送文件数据*/
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            dos.writeBytes("SendData"+SendData);
            if (files != null) {
                int filei=0;
                for (Map.Entry<String, File> file : files.entrySet()) {
                    String sb = hyphens + BOUNDARY + end +
                            "Content-Disposition: form-data; name=\"file" + filei + "\";filename=\"" + file.getKey() + "\"" + end + end;
                    dos.write(sb.getBytes());
                    InputStream is =  new FileInputStream(file.getValue());
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        dos.write(buffer, 0, len);
                    }
                    is.close();
                    dos.write(end.getBytes());
                }
            }
            byte[] end_data = (hyphens + BOUNDARY + hyphens + end).getBytes();
            dos.write(end_data);
            dos.flush();

            InputStream in = con.getInputStream();
            InputStreamReader isReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(isReader);

            switch (con.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                case 301:
                case 302:
                case 404:
                    break;
                case 403:
                    Log.d("Configuration error", "API_KEY or API_SECRET or system time error.");
                    return null;
                case 401:
                    Log.d("Post Result","Code 401");
                    CarbonForumApplication.userInfo.edit().clear().apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    break;
                case 500:
                    Log.d("Post Result","Code 500");
                    return null;
                default:
                    throw new Exception("HTTP Request is not success, Response code is " + con.getResponseCode());
            }
            try {
                String line = "";
                StringBuilder data = new StringBuilder();
                while ((line = bufReader.readLine()) != null) {
                    data.append(line);
                }

            /*//httpURLConnection.disconnect();//断开连接
            String postResult = resultBuffer.toString();
            Log.d("Post Result",postResult);
            JSONTokener jsonParser = new JSONTokener(postResult);
            return (JSONObject) jsonParser.nextValue();*/
                String getResult = data.toString();
                Log.d("Get URL : ", urlStr);
                Log.d("Get Result",getResult);
                return JSONUtil.jsonString2Object(getResult);
            }  catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (bufReader != null) {
                    bufReader.close();
                }

                if (isReader != null) {
                    isReader.close();
                }

                if (in != null) {
                    in.close();
                }
            }
        } catch (Exception e) {
            Log.d("Post Error", "No Network");
            e.printStackTrace();
            return null;
        }
    }

    //获取之前保存的Cookie
    public static String getCookie(Context context){
        SharedPreferences mySharedPreferences= context.getSharedPreferences("Session",
                Activity.MODE_PRIVATE);
        try{
            return  mySharedPreferences.getString("Cookie", "");
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    //保存Cookie
    public static Boolean saveCookie(Context context, URLConnection connection){
        //获取Cookie
        String headerName=null;
        for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = connection.getHeaderField(i);
                //将Cookie保存起来
                SharedPreferences mySharedPreferences = context.getSharedPreferences("Session",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putString("Cookie", cookie);
                editor.apply();
                return true;
            }
        }
        return false;
    }

    public static String buildParameterString(Map<String, String> parameterMap, Boolean loginRequired){
        /* Translate parameter map to parameter date string */
        StringBuilder parameterBuffer = new StringBuilder();
        String currentTimeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        parameterBuffer
                .append("SKey").append("=")
                .append(APIAddress.API_KEY)
                .append("&")
                .append("STime").append("=")
                .append(currentTimeStamp)
                .append("&")
                .append("SValue").append("=")
                .append(MD5Util.md5(APIAddress.API_KEY + APIAddress.API_SECRET + currentTimeStamp));

        if(loginRequired && CarbonForumApplication.isLoggedIn()){
            parameterBuffer
                    .append("&")
                    .append("AuthUserID").append("=")
                    .append(CarbonForumApplication.userInfo.getString("UserID", ""))
                    .append("&")
                    .append("AuthUserExpirationTime").append("=")
                    .append(CarbonForumApplication.userInfo.getString("UserExpirationTime", ""))
                    .append("&")
                    .append("AuthUserCode").append("=")
                    .append(CarbonForumApplication.userInfo.getString("UserCode", ""));
        }
        if (parameterMap != null) {
            parameterBuffer.append("&");
            Iterator iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) != null) {
                    try {
                        value = URLEncoder.encode(parameterMap.get(key), "UTF-8");
                    }catch(UnsupportedEncodingException e){
                        value = parameterMap.get(key);
                        e.printStackTrace();
                    }
                } else {
                    value = "";
                }
                parameterBuffer.append(key.contains("#") ? key.substring(0, key.indexOf("#")) : key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }

    private URLConnection openConnection(URL localURL) throws IOException {
        URLConnection connection;
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            connection = localURL.openConnection(proxy);
        } else {
            connection = localURL.openConnection();
        }
        return connection;
    }

    private void renderRequest(URLConnection connection) {

        if (connectTimeout != null) {
            connection.setConnectTimeout(connectTimeout);
        }

        if (socketTimeout != null) {
            connection.setReadTimeout(socketTimeout);
        }

    }

    /*
     * Getter & Setter
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        HttpUtil.charset = charset;
    }
}