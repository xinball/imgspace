package top.xb.imgspace.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import top.xb.imgspace.LoginActivity;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.config.APIAddress;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HttpUtil {
    private Integer connectTimeout = null;
    private Integer socketTimeout = null;
    private String proxyHost = null;
    private Integer proxyPort = null;
    private static String charset = "UTF-8";
    private static final String boundary = "*****";

    // Post方法访问服务器，返回json对象
    public static JSONObject postRequest(Context context, String SendData, List<String> files) {
        try {
            String urlStr=APIAddress.SEND_URL;
            Log.d("Post URL", urlStr);
            URL url = new URL(urlStr);
            //String parameterString = buildParameterString(parameterMap, loginRequired);
            Log.d("Post parameter", SendData);

            HttpsURLConnection con = (HttpsURLConnection)  url.openConnection();
            /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            /* 设定传送的method=POST */
            con.setRequestMethod("POST");
            /* setRequestProperty */
            con.setConnectTimeout(10*1000);//超时时间
            con.setReadTimeout(10*1000);
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", charset);
            JSONObject jsob=new JSONObject();
            boolean uploadCheck=false,paraCheck=true;//是否上传图片，是否传递参数
            String end = "\r\n";
            String hyphens = "--";
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            JSONObject SendDataJSON=JSON.parseObject(SendData);
            if(SendDataJSON!=null){
                String page=SendDataJSON.getString("action");
                String action=SendDataJSON.getString("method");
                if(!page.equals("")&&!action.equals("")){
                    if(page.equals("send")&&(action.equals("uploadmsg")||action.equals("uploadphoto")||action.equals("uploadavatar"))){
                        uploadCheck=true;
                    }
                }else {
                    return JSONUtil.returnmsg(0,"JSON格式有误！");
                }
            }else {
                return JSONUtil.returnmsg(0,"JSON格式有误！");
            }
            /* 设定DataOutputStream 发送文件数据*/
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            String msg=null;
            if (files != null&&uploadCheck) {
                int filei=0;
                for (String filepath : files) {
                    if(filepath==null)
                        continue;
                    File file = new File(filepath);
                    long filesize=file.length();
                    if(filesize>10*1024*1024){
                        msg="文件大小太大，上传失败！";
                        paraCheck=false;
                        break;
                    }
                    String filename = file.getName().toLowerCase();
                    //没有传入文件类型，同时根据文件获取不到类型，默认采用application/octet-stream
                    String contentType = null;
                    //contentType非空采用filename匹配默认的图片类型
                    if (filename.endsWith(".png")) {
                        contentType = "image/png";
                    }else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".jpe")) {
                        contentType = "image/jpeg";
                    }else if (filename.endsWith(".gif")) {
                        contentType = "image/gif";
                    }else if (filename.endsWith(".ico")) {
                        contentType = "image/image/x-icon";
                    }
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    String sb = end + hyphens + BOUNDARY + end + "Content-Disposition: form-data; name=\"file" + filei + "\";filename=\"" + filename + "\"" + end + "Content-Type:" + contentType + end + end;
                    dos.write(sb.getBytes());
                    if(file.exists()){
                        DataInputStream is =  new DataInputStream(new FileInputStream(file));
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {
                            dos.write(buffer, 0, len);
                        }
                        is.close();
                    }else{
                        msg="需要上传的文件不存在！";
                        paraCheck=false;
                        break;
                    }
                    dos.write(end.getBytes());
                    filei++;
                }
            }
            if(paraCheck)
                dos.write((end + hyphens + BOUNDARY + end + "Content-Disposition: form-data; name=\"SendData\"" + end + end + SendData+ end).getBytes());
            dos.write((end + hyphens + BOUNDARY + hyphens + end).getBytes());
            dos.flush();
            dos.close();

            if(!paraCheck) {
                return JSONUtil.returnmsg(0,msg);
            }else{
                InputStream in = con.getInputStream();
                InputStreamReader isReader = null;
                BufferedReader bufReader = null;

                switch (con.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                    case 301:
                    case 302:
                    case 404:
                        break;
                    case 403:
                        Log.d("Configuration error", "API_KEY or API_SECRET or system time error.");
                        return JSONUtil.returnmsg(0, "Configuration error:API_KEY or API_SECRET or system time error.");
                    case 401:
                        Log.d("Post Result", "Code 401");
                        ImgSpaceApplication.userInfo.edit().clear().apply();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        break;
                    case 500:
                        Log.d("Post Result", "Code 500");
                        return JSONUtil.returnmsg(0, "Post Result:Code 500");
                    default:
                        throw new Exception("HTTP Request is not success, Response code is " + con.getResponseCode());
                }
                try {
                    isReader = new InputStreamReader(in);
                    bufReader = new BufferedReader(isReader);
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
                    Pattern pattern = Pattern.compile(".*<body>(.*)</body>.*");
                    Matcher matcher = pattern.matcher(getResult);
                    while (matcher.find()) {
                        getResult = matcher.group(1);
                    }
                    Log.d("Get URL : ", urlStr);
                    Log.d("Get Result", getResult);
                    return JSON.parseObject(getResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    return JSONUtil.returnmsg(0, "服务器返回数据格式有误！");
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
            }
        } catch (Exception e) {
            Log.d("Post Error", "No Network");
            e.printStackTrace();
            return JSONUtil.returnmsg(0,"Post Error:No Network");
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

        if(loginRequired && ImgSpaceApplication.isLoggedIn()){
            parameterBuffer
                    .append("&")
                    .append("AuthUserID").append("=")
                    .append(ImgSpaceApplication.userInfo.getString("UserID", ""))
                    .append("&")
                    .append("AuthUserExpirationTime").append("=")
                    .append(ImgSpaceApplication.userInfo.getString("UserExpirationTime", ""))
                    .append("&")
                    .append("AuthUserCode").append("=")
                    .append(ImgSpaceApplication.userInfo.getString("UserCode", ""));
        }
        if (parameterMap != null) {
            parameterBuffer.append("&");
            Iterator<String> iterator = parameterMap.keySet().iterator();
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