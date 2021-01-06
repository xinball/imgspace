package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.xb.imgspace.adapter.SpaceListAdapter;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.bean.PriUser;
import top.xb.imgspace.bean.User;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class SpaceActivity extends AppCompatActivity {
    private static final String TAG = "SpaceActivity";

    private ProgressBar spaceProgressView;
    private View spaceView;
    private SpaceTask spaceTask;
    IntentFilter intentFilter;

    private ProgressBar userProgressView=null;
    private View userView=null;
    PriUser priuser;
    String uid;
    private TextView space_name;
    private EditText spacekey;
    private ImageView space_headPhoto;
    private UserTask userTask;

    private final BroadcastReceiver SpaceReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("SpaceRefresh")){
                String key=intent.getStringExtra("key");
                if(key!=null&&key.equals(""))
                    key=null;
                if(priuser==null){
                    uid=intent.getStringExtra("uid");
                    if(uid!=null&&uid.equals(""))
                        uid=null;
                }else{
                    uid=priuser.uid;
                }
                //Toast.makeText(mainActivity,"send ok!", Toast.LENGTH_SHORT).show();
                spaceTask = new SpaceTask(key);
                DisplayUtil.showProgress(context,spaceProgressView,spaceView,true);
                spaceTask.execute();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthUtil.verifyPermissions(this);
        setContentView(R.layout.activity_space);
        intentFilter=new IntentFilter();
        intentFilter.addAction("SpaceRefresh");
        registerReceiver(SpaceReceiver,intentFilter);
        spaceProgressView=findViewById(R.id.spaceProgress);
        spaceView=findViewById(R.id.space_refresh);
        userProgressView=spaceProgressView;
        userView=spaceView;
        spacekey=findViewById(R.id.spacekey);

        spaceTask=new SpaceTask(null);
        DisplayUtil.showProgress(this,spaceProgressView,spaceView,true);
        spaceTask.execute();


        space_headPhoto=findViewById(R.id.space_headPhoto);
        space_name=findViewById(R.id.space_name);
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");
        if(uid!=null){
            DisplayUtil.showProgress(SpaceActivity.this,spaceProgressView,spaceView,true);
            spaceTask=new SpaceTask(null);
            spaceTask.execute();
        }else{
            Toast.makeText(this,"打开空间失败！",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(SpaceReceiver);
        super.onDestroy();
    }

    public void spaceRefresh(View view) {
        Intent intent=new Intent("SpaceRefresh");
        String key=spacekey.getText().toString().trim();
        if(!key.isEmpty())
            intent.putExtra("key",key);
        sendBroadcast(intent);
    }

    @SuppressLint("StaticFieldLeak")
    public class SpaceTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String key="";

        List<Message> messages=new ArrayList<>();
        List<List<Photo>> photoslist= new ArrayList<>();
        List<String> names=new ArrayList<>();
        List<String> avatars=new ArrayList<>();

        SpaceTask(String key) {
            if(key!=null){
                this.key=key;
                SendData="{\"action\":\"receive\",\"method\":\"allkeymsg\",\"key\":\""+key+"\"}";
            }else{
                SendData="{\"action\":\"receive\",\"method\":\"allmsg\"}";
            }
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(SpaceActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            spaceTask = null;
            DisplayUtil.showProgress(SpaceActivity.this,spaceProgressView,spaceView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        Toast.makeText(SpaceActivity.this,"收取消息成功！", Toast.LENGTH_SHORT).show();
                        String returnmsg=result.getString("returnmsg");
                        JSONArray msgs= JSON.parseArray(returnmsg);
                        for(Object msg:msgs){
                            JSONObject m=(JSONObject)msg;
                            String uid=m.getString("uid");
                            String name=m.getString("name");
                            String avatar=m.getString("avatar");
                            String mid=m.getString("mid");
                            String message=m.getString("message");
                            String key=m.getString("key");
                            if(key!=null&&key.equals(""))
                                key=null;
                            String sendTimeStr=m.getString("sendTime");
                            String alterTimeStr=m.getString("alterTime");
                            Log.i("SpaceTask",sendTimeStr+alterTimeStr);
                            Date sendTime=null,alterTime=null;
                            if(sendTimeStr!=null&&!sendTimeStr.equals(""))
                                sendTime=DisplayUtil.dateFormat.parse(sendTimeStr);
                            if(alterTimeStr!=null&&!alterTimeStr.equals(""))
                                alterTime=DisplayUtil.dateFormat.parse(alterTimeStr);
                            messages.add(new Message(uid,mid,message,key,sendTime,alterTime));
                            names.add(name);
                            avatars.add(avatar);
                            JSONArray photosStr=JSON.parseArray(m.getString("photos"));
                            List<Photo> photos = new ArrayList<>();
                            for(Object photoStr:photosStr){
                                JSONObject p=(JSONObject)photoStr;
                                String pid=p.getString("pid");
                                String url= p.getString("url");
                                Date uploadTime=null;
                                String uploadTimeStr=p.getString("uploadTime");
                                Log.i("SpaceTask",uploadTimeStr);
                                if(uploadTimeStr!=null&&!uploadTimeStr.equals(""))
                                    uploadTime=DisplayUtil.dateFormat.parse(uploadTimeStr);
                                photos.add(new Photo(pid,uid,mid,url,uploadTime));
                            }
                            photoslist.add(photos);
                        }
                        ListView space_message=findViewById(R.id.space_message);
                        if(space_message==null)
                            Log.i(TAG, "onCreate: lv empty");
                        SpaceListAdapter adp=new SpaceListAdapter(messages,photoslist,names,avatars,SpaceActivity.this);
                        if(adp.isEmpty()||space_message==null)
                            Log.i(TAG, "onCreate: adp empty");
                        else
                            space_message.setAdapter(adp);


                        userTask=new UserTask(uid);
                        DisplayUtil.showProgress(SpaceActivity.this,userProgressView,userView,false);
                        userTask.execute();
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(SpaceActivity.this,returnmsg,true,"收取消息失败！","确定",null);
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SpaceActivity.this,"收取JSON消息失败！", Toast.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(spaceView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            spaceTask = null;
            DisplayUtil.showProgress(SpaceActivity.this,spaceProgressView,spaceView,false);
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
            return HttpUtil.postRequest(SpaceActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            userTask = null;
            DisplayUtil.showProgress(SpaceActivity.this,userProgressView,userView,false);
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
                            Glide.with(SpaceActivity.this).load(avatarcache.getPath()).override(40,40).into(space_headPhoto);
                        else
                            Glide.with(SpaceActivity.this).load(avatarPath).override(40,40).into(space_headPhoto);
                        space_name.setText(priuser.name);
                    } else {
                        Toast.makeText(SpaceActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
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
            DisplayUtil.showProgress(SpaceActivity.this,userProgressView,userView,false);
        }
    }
}