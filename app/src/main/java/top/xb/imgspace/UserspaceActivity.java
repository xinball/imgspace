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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.xb.imgspace.adapter.MyListAdapter;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.bean.User;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class UserspaceActivity extends AppCompatActivity {

    private List<List<Photo>> Images;
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
/*
    private List<Data> getList(){
        List<Data> list=new ArrayList<Data>();
        Data d=new Data();
        d.name="Da Lao";
        d.content="垃圾安卓，垃圾Android，垃圾Android Stidio!!!!\n滚出老子的电脑";
        BitmapFactory.Options op=new BitmapFactory.Options();
        op.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(ScanPath,op);
        op.inSampleSize=4;
        op.inJustDecodeBounds=false;
        Bitmap bmp=BitmapFactory.decodeFile(ScanPath,op);
        d.img=bmp;
        for(int i=0;i<3;i++){
            list.add(d);
        }
        return list;
    }

    public void test(){
        @SuppressLint("SdCardPath")
        String scanpath="/sdcard/1/002.jpg";
        Log.i("test：", "test: "+scanpath);
        ArrayList<List<String>> mImagesPath = new ArrayList<List<String>>();
        List<String> paths=new ArrayList<String>();
        for(int n=0;n<5;n++){
            paths.add(scanpath);
        }
        mImagesPath.add(paths);
        paths=new ArrayList<String>();
        for(int n=0;n<3;n++){
            paths.add(ScanPath);
        }
        mImagesPath.add(paths);
        paths=new ArrayList<String>();
        for(int n=0;n<8;n++){
            paths.add(ScanPath);
        }
        mImagesPath.add(paths);
    }

*/


    @SuppressLint("StaticFieldLeak")
    public class UserspaceTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String method="";
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
                            Date sendTime=DisplayUtil.dateFormat.parse(m.getString("sendTime"));
                            Date alterTime=DisplayUtil.dateFormat.parse(m.getString("alterTime"));
                            messages.add(new Message(uid,mid,message,key,sendTime,alterTime));
                            JSONArray photosStr=JSON.parseArray(m.getString("photos"));
                            List<Photo> photos = new ArrayList<>();
                            for(Object photoStr:photosStr){
                                JSONObject p=(JSONObject)photoStr;
                                String pid=p.getString("pid");
                                String url=p.getString("url");
                                Date uploadTime=DisplayUtil.dateFormat.parse(p.getString("uploadTime"));
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