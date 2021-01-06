package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import top.xb.imgspace.adapter.MyListAdapter;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.bean.User;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class UserspaceActivity extends AppCompatActivity {
    private static final String TAG = "UserspaceActivity";

    private ProgressBar spaceProgressView;
    private View spaceView;
    private UserspaceTask userspaceTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthUtil.verifyPermissions(this);
        setContentView(R.layout.activity_userspace);
        spaceProgressView=findViewById(R.id.userspaceProgress);
        spaceView=findViewById(R.id.userspace_refresh);
        userspaceTask=new UserspaceTask("pelwdq",null);
        userspaceTask.execute();
    }


    @SuppressLint("StaticFieldLeak")
    public class UserspaceTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String key="";

        List<Message> messages=new ArrayList<>();;
        List<List<Photo>> photoslist= new ArrayList<>();

        UserspaceTask(String uid, String key) {
            this.uid=uid;
            if(key!=null){
                this.key=key;
                SendData="{\"action\":\"receive\",\"method\":\"uidkeymsg\",\"uid\":\""+uid+"\",\"key\":\""+key+"\"}";
            }else{
                SendData="{\"action\":\"receive\",\"method\":\"uidmsg\",\"uid\":\""+uid+"\"}";
            }
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(UserspaceActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            userspaceTask = null;
            DisplayUtil.showProgress(UserspaceActivity.this,spaceProgressView,spaceView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        Toast.makeText(UserspaceActivity.this,"收取消息成功！", Toast.LENGTH_SHORT).show();
                        String returnmsg=result.getString("returnmsg");
                        JSONArray msgs= JSON.parseArray(returnmsg);
                        for(Object msg:msgs){
                            JSONObject m=(JSONObject)msg;
                            String uid=m.getString("uid");
                            String mid=m.getString("mid");
                            String message=m.getString("message");
                            String key=m.getString("key");
                            if(key!=null&&key.equals(""))
                                key=null;
                            String sendTimeStr=m.getString("sendTime");
                            String alterTimeStr=m.getString("alterTime");
                            Log.i("UserspaceTask",sendTimeStr+alterTimeStr);
                            Date sendTime=null,alterTime=null;
                            if(sendTimeStr!=null&&!sendTimeStr.equals(""))
                                sendTime=DisplayUtil.dateFormat.parse(sendTimeStr);
                            if(alterTimeStr!=null&&!alterTimeStr.equals(""))
                                alterTime=DisplayUtil.dateFormat.parse(alterTimeStr);
                            messages.add(new Message(uid,mid,message,key,sendTime,alterTime));
                            JSONArray photosStr=JSON.parseArray(m.getString("photos"));
                            List<Photo> photos = new ArrayList<>();
                            for(Object photoStr:photosStr){
                                JSONObject p=(JSONObject)photoStr;
                                String pid=p.getString("pid");
                                String url= APIAddress.WEB_IMG_URL+p.getString("url");
                                Date uploadTime=null;
                                String uploadTimeStr=p.getString("uploadTime");
                                Log.i("UserspaceTask",uploadTimeStr);
                                if(uploadTimeStr!=null&&!uploadTimeStr.equals(""))
                                    uploadTime=DisplayUtil.dateFormat.parse(uploadTimeStr);
                                photos.add(new Photo(pid,uid,mid,url,uploadTime));
                            }
                            photoslist.add(photos);
                        }
                        ListView userspace_message=findViewById(R.id.userspace_message);
                        if(userspace_message==null)
                            Log.i(TAG, "onCreate: lv empty");
                        MyListAdapter adp=new MyListAdapter(messages,photoslist,UserspaceActivity.this);
                        if(adp.isEmpty())
                            Log.i(TAG, "onCreate: adp empty");
                        userspace_message.setAdapter(adp);
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(UserspaceActivity.this,returnmsg,true,"收取消息失败！","确定",null);
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(UserspaceActivity.this,"收取JSON消息失败！", Toast.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(spaceView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            userspaceTask = null;
            DisplayUtil.showProgress(UserspaceActivity.this,spaceProgressView,spaceView,false);
        }
    }
}