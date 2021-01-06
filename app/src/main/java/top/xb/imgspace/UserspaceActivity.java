package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import top.xb.imgspace.adapter.MyListAdapter;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.bean.PriUser;
import top.xb.imgspace.bean.User;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;
import top.xb.imgspace.utils.ToastUtil;

public class UserspaceActivity extends AppCompatActivity {
    private static final String TAG = "UserspaceActivity";

    private ProgressBar userspaceProgressView;
    private View userspaceView;
    private UserspaceTask userspaceTask;
    IntentFilter intentFilter;

    private UserTask userTask;
    private ProgressBar userProgressView=null;
    private View userView=null;
    PriUser priuser;

    private TextView userspace_name;
    private TextView userspacekey;
    private ImageView userspace_headPhoto;

    private final BroadcastReceiver UserspaceReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("UserspaceRefresh")){
                String uid,key;
                if(priuser==null){
                    uid=intent.getStringExtra("uid");
                    if(uid!=null&&uid.equals(""))
                        uid=null;
                }else{
                    uid=priuser.uid;
                }
                key=intent.getStringExtra("key");
                if(key!=null&&key.equals(""))
                    key=null;
                //Toast.makeText(mainActivity,"send ok!", Toast.LENGTH_SHORT).show();
                userspaceTask = new UserspaceActivity.UserspaceTask(uid,key);
                DisplayUtil.showProgress(UserspaceActivity.this,userspaceProgressView,userspaceView,true);
                userspaceTask.execute();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthUtil.verifyPermissions(this);
        setContentView(R.layout.activity_userspace);
        intentFilter=new IntentFilter();
        intentFilter.addAction("UserspaceRefresh");
        registerReceiver(UserspaceReceiver,intentFilter);
        userspaceProgressView=findViewById(R.id.userspaceProgress);
        userProgressView=userspaceProgressView;
        userspaceView=findViewById(R.id.userspaceView);
        userView=userspaceView;
        userspace_headPhoto=findViewById(R.id.userspace_headPhoto);
        userspace_name=findViewById(R.id.userspace_name);
        userspacekey=findViewById(R.id.userspacekey);
        Intent intent=getIntent();
        String uid=intent.getStringExtra("uid");
        if(uid!=null){
            DisplayUtil.showProgress(UserspaceActivity.this,userspaceProgressView,userspaceView,true);
            userspaceTask=new UserspaceTask(uid,null);
            userspaceTask.execute();
        }else{
            Toast.makeText(this,"打开空间失败！",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(UserspaceReceiver);
        super.onDestroy();
    }

    public void userspaceRefresh(View view) {
        Intent intent=new Intent("UserspaceRefresh");
        String key=userspacekey.getText().toString().trim();
        if(!key.isEmpty())
            intent.putExtra("key",key);
        sendBroadcast(intent);
    }

    @SuppressLint("StaticFieldLeak")
    public class UserspaceTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String key="";

        List<Message> messages=new ArrayList<>();;
        List<List<Photo>> photoslist= new ArrayList<>();

        UserspaceTask(String uid, String key) {
            if(uid!=null)
                this.uid=uid;
            else
                this.uid="";
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
            DisplayUtil.showProgress(UserspaceActivity.this,userspaceProgressView,userspaceView,false);
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
                                String url= p.getString("url");
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

                        userTask=new UserTask(uid);
                        DisplayUtil.showProgress(UserspaceActivity.this,userProgressView,userView,false);
                        userTask.execute();
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(UserspaceActivity.this,returnmsg,true,"收取消息失败！","确定",null);
                        finish();
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(UserspaceActivity.this,"收取JSON消息失败！", Toast.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(userspaceView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            userspaceTask = null;
            DisplayUtil.showProgress(UserspaceActivity.this,userspaceProgressView,userspaceView,false);
        }
    }



    @SuppressLint("StaticFieldLeak")
    public class UserTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private IntentFilter intentFilter=null;

        UserTask(String uid) {
            this.uid=uid;
            SendData="{\"action\":\"receive\","+"\"method\":\"userinfo\","+"\"uid\":\"" + uid + "\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(UserspaceActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            userTask = null;
            DisplayUtil.showProgress(UserspaceActivity.this, userProgressView,userView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        priuser= result.getObject("returnmsg", PriUser.class);
                        //头像
                        String avatarFile=priuser.uid+priuser.avatar;
                        Log.i(TAG,"avatarFile"+avatarFile);
                        String avatarPath= APIAddress.WEB_IMG_URL+avatarFile;
                        Log.i(TAG,"avatarPath"+avatarPath);
                        File avatarcache=new File(Environment.getExternalStorageDirectory()+APIAddress.WebcachePath+avatarFile);
                        Log.i(TAG,"avatarcache"+avatarcache);
                        if(avatarcache.exists())
                            Glide.with(UserspaceActivity.this).load(avatarcache.getPath()).override(40,40).into(userspace_headPhoto);
                        else
                            Glide.with(UserspaceActivity.this).load(avatarPath).override(40,40).into(userspace_headPhoto);
                        userspace_name.setText(priuser.name);
                    } else {
                        Toast.makeText(UserspaceActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(userView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            userTask = null;
            DisplayUtil.showProgress(UserspaceActivity.this,userProgressView,userView,false);
        }
    }
}