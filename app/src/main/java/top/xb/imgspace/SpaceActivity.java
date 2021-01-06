package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.xb.imgspace.adapter.SpaceListAdapter;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.bean.User;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class SpaceActivity extends AppCompatActivity {
    private static final String TAG = "UserspaceActivity";

    private ProgressBar spaceProgressView;
    private View spaceView;
    private SpaceTask spaceTask;
    IntentFilter intentFilter;

    private final BroadcastReceiver SpaceReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("SpaceRefresh")){
                String key=intent.getStringExtra("key");
                if(key!=null&&key.equals(""))
                    key=null;
                //Toast.makeText(mainActivity,"send ok!", Toast.LENGTH_SHORT).show();
                spaceTask = new SpaceTask(key);
                DisplayUtil.showProgress(context,null,null,true);
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
        spaceView=findViewById(R.id.userspace_refresh);
        spaceTask=new SpaceTask(null);
        spaceTask.execute();
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
                            Log.i("UserspaceTask",sendTimeStr+alterTimeStr);
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
                                Log.i("UserspaceTask",uploadTimeStr);
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
                        if(adp.isEmpty())
                            Log.i(TAG, "onCreate: adp empty");
                        space_message.setAdapter(adp);
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
}