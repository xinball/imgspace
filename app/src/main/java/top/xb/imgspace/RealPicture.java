package top.xb.imgspace;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncStats;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bm.library.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

import top.xb.imgspace.adapter.MyPagerAdapter;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class RealPicture extends AppCompatActivity {

    private ViewPager mPictureviewpager;        //用于滑页
    private MyPagerAdapter myPagerAdapter;         //存储所有图片的适配器
    private View deleteProgress;
    private boolean local=true;

    private String path=null;
    private ArrayList<String> mImage=null; //用于滑页，存储所有图片id，如果想存储uri，需要改变ArrayList类型，并且MyPagerAdapter里的
    //生成图片的函数（instantiateItem函数里的）也需要改变

    private ArrayList<Photo> photos=null;
    private boolean self=true;
    private DeletephotoTask deletephotoTask;

    private View real_bottomtitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_picture);
        //初始化列表，列表里保存的是图片的地址
        initView();
        //获得上一个活动传来的消息
        Intent intent=getIntent();
        local=intent.getBooleanExtra("local",true);
        if(local){
            path=intent.getStringExtra("path");
            mImage=intent.getStringArrayListExtra("files");
            findViewById(R.id.realEdit_btn).setVisibility(View.VISIBLE);
        }else{
            photos=intent.getParcelableArrayListExtra("photos");
            self=intent.getBooleanExtra("self",false);
            if(self)
                findViewById(R.id.realDelete_btn).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.realDelete_btn).setVisibility(View.GONE);

            findViewById(R.id.realEdit_btn).setVisibility(View.GONE);
        }
        deleteProgress=findViewById(R.id.deleteProgress);
        real_bottomtitle=findViewById(R.id.real_bottomtitle);
        myPagerAdapter=new MyPagerAdapter(RealPicture.this,local,mImage,path,photos,real_bottomtitle);
        mPictureviewpager.setAdapter(myPagerAdapter);
        //找到上一个活动点击时的位置，可以从该位置查看图片
        int index=intent.getIntExtra("position",0);
        mPictureviewpager.setCurrentItem(index,false);
    }

    private void initView(){
        mPictureviewpager=(ViewPager)findViewById(R.id.viewpager);
        /**mImage.add(R.drawable.img1);
        mImage.add(R.drawable.img2);
        mImage.add(R.drawable.img3);
        mImage.add(R.drawable.img4);
        mImage.add(R.drawable.img5);
        mImage.add(R.drawable.img6);
         */
    }

    //分享按钮的点击函数
    public void share(View view){
        Toast.makeText(RealPicture.this,"你点击了分享按钮",Toast.LENGTH_SHORT).show();
    }

    //菜单按钮的点击函数
    public void menu(View view){
        if(local){
            String path=(String) myPagerAdapter.getPrimaryItem();
            DisplayUtil.dialogProcess(this,"图片位置为："+ path,true,"图片信息","好！",null);
        }else{
            Photo photo=(Photo) myPagerAdapter.getPrimaryItem();
            DisplayUtil.dialogProcess(this,"网络路径为："+ APIAddress.WEB_IMG_URL+photo.url+
                    "\n本地保存位置为："+ Environment.getExternalStorageDirectory()+APIAddress.WebcachePath+photo.uid+"\n上传时间为："+DisplayUtil.dateFormat.format(photo.uploadTime),
                    true,"图片信息","好！",null);
        }
        //Toast.makeText(RealPicture.this,"你点击了菜单按钮",Toast.LENGTH_SHORT).show();
    }
    //编辑按钮的点击函数
    public void edit(View view){
        String v = (String) myPagerAdapter.getPrimaryItem();
        //Log.d("RealPicture","aaaa"+R.drawable.img2);
        Intent intent1=new Intent(RealPicture.this,Edit.class);
        intent1.putExtra("file",v);
        startActivity(intent1);
    }
    //删除按钮的点击函数
    public void delete(View view){
        AlertDialog.Builder dialog= DisplayUtil.dialogExcute(RealPicture.this,"是否删除此图片?",false,"");
        dialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(local){
                    File file=new File((String) myPagerAdapter.getPrimaryItem());
                    if(file.exists()&&!file.isDirectory())
                        if(file.delete()){
                            Toast.makeText(RealPicture.this,"删除图片成功！",Toast.LENGTH_SHORT).show();
                            sendBroadcast(new Intent("HomeRefresh"));
                            finish();
                        }
                        else{
                            Toast.makeText(RealPicture.this,"删除图片失败！",Toast.LENGTH_SHORT).show();
                        }
                }else{
                    String pwd=ImgSpaceApplication.getPwd();
                    if(pwd==null){
                        Toast.makeText(RealPicture.this,"请先登录再进行操作！",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RealPicture.this,LoginActivity.class));
                        finish();
                    }else{
                        Photo photo=(Photo) myPagerAdapter.getPrimaryItem();
                        deletephotoTask=new DeletephotoTask(photo.uid,pwd,photo.mid,photo.pid);
                        deletephotoTask.execute();
                    }
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();

    }


    @SuppressLint("StaticFieldLeak")
    public class DeletephotoTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String pwd="";
        private String mid="";
        private String pid="";

        DeletephotoTask(String uid, String pwd ,String mid ,String pid) {
            this.uid=uid;
            this.pwd=pwd;
            this.mid=mid;
            this.pid=pid;
            SendData="{\"action\":\"send\",\"method\":\"deletephoto\",\"uid\":\""+uid+"\",\"pwd\":\""+pwd+"\",\"mid\":\""+mid+"\",\"pid\":\""+pid+"\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(RealPicture.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            deletephotoTask = null;
            DisplayUtil.showProgress(RealPicture.this,deleteProgress,null,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        Toast.makeText(RealPicture.this,"图片删除成功！", Toast.LENGTH_SHORT).show();
                        //发送广播
                        Intent intent = new Intent();
                        intent.setAction("SpaceRefresh");
                        sendBroadcast(intent);
                        finish();
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(RealPicture.this,returnmsg,true,"删除图片失败","确定",null);
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(real_bottomtitle, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            deletephotoTask = null;
            DisplayUtil.showProgress(RealPicture.this,deleteProgress,null,false);
        }
    }
}