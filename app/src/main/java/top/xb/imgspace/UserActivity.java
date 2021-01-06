package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.PriUser;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private UserTask userTask;
    private ProgressBar userProgressView;
    private ScrollView userView;
    PriUser priuser;

    private ImageButton change_ig;
    private Button change_every;
    private ImageView iv_photo;
    private Bitmap head;// 头像Bitmap

    TextView tv_accout;
    TextView tv_emailname;
    TextView tv_telname;
    TextView tv_fakeusername;
    TextView tv_sexname;
    TextView tv_truename;
    TextView tv_addressname;
    TextView tv_yearname;
    TextView tv_monthname;
    TextView tv_dayname;
    Button btn_tospace;

    private TextView tv_user_show_name;
    private Context mContext;


    private static final int REQUEST_EXTERNAL_STORAG = 1;
    private static String[] PERMISSIONS_STORAG = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {       //头部颜色
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        AuthUtil.verifyPermissions(this);   //获得存储权限

        /*Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        int checkCallPhonePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},222);
            return;
        }*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);                  //跳转界面
        change_ig=(ImageButton)findViewById(R.id.touxiang);
        change_ig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 更换头像
                 showTypeDialog();
            }
        });
        userProgressView=findViewById(R.id.userProgress);
        userView=findViewById(R.id.userview);
        tv_user_show_name=findViewById(R.id.tv_user_show_name);


        tv_accout=findViewById(R.id.tv_accout);
        tv_emailname=findViewById(R.id.tv_emailname);
        tv_telname=findViewById(R.id.tv_telname);
        tv_fakeusername=findViewById(R.id.tv_fakeusername);
        tv_sexname=findViewById(R.id.tv_sexname);
        tv_truename=findViewById(R.id.tv_truename);
        tv_addressname=findViewById(R.id.tv_addressname);
        tv_yearname=findViewById(R.id.tv_yearname);
        tv_monthname=findViewById(R.id.tv_monthname);
        tv_dayname=findViewById(R.id.tv_dayname);
        btn_tospace=findViewById(R.id.btn_tospace);
        Intent intent=getIntent();
        String uid=intent.getStringExtra("uid");
        if(uid!=null&&uid.equals(""))
            uid=null;
        //Toast.makeText(mainActivity,"send ok!", Toast.LENGTH_SHORT).show();
        userTask = new UserTask(uid);
        DisplayUtil.showProgress(this,userProgressView,userView,true);
        userTask.execute();

        btn_tospace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserActivity.this, UserspaceActivity.class);
                intent.putExtra("uid",priuser.uid);
                startActivity(intent);
            }
        });
    }


    private void showTypeDialog() {
        //显示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.dialog_select_photo, null);
        TextView tv_select_gallery = (TextView) view.findViewById(R.id.tv_select_gallery);
        //TextView tv_select_camera = (TextView) view.findViewById(R.id.tv_select_camera);
        tv_select_gallery.setOnClickListener(new View.OnClickListener() {// 在相册中选取
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                //打开文件
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 1);
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());// 裁剪图片
                }

                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));// 裁剪图片
                }

                break;
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        /**
                         * 上传服务器代码
                         */
                        setPicToView(head);// 保存在SD卡中
                        change_ig.setImageBitmap(head);// 用ImageButton显示出来
                    }
                }
                break;
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 调用系统的裁剪功能
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        String path=sdStatus+APIAddress.WebcachePath;
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        priuser.avatar=".png";
        String fileName = path + priuser.uid+priuser.avatar;// 图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            return HttpUtil.postRequest(UserActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            userTask = null;
            DisplayUtil.showProgress(UserActivity.this,userProgressView,userView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        priuser= result.getObject("returnmsg",PriUser.class);
                        //头像
                        String avatarFile=priuser.uid+priuser.avatar;
                        Log.i(TAG,"avatarFile"+avatarFile);
                        String avatarPath= APIAddress.WEB_IMG_URL+avatarFile;
                        Log.i(TAG,"avatarPath"+avatarPath);
                        File avatarcache=new File(Environment.getExternalStorageDirectory()+APIAddress.WebcachePath+avatarFile);
                        Log.i(TAG,"avatarcache"+avatarcache);
                        if(avatarcache.exists())
                            Glide.with(UserActivity.this).load(avatarcache.getPath()).override(40,40).into(change_ig);
                        else
                            Glide.with(UserActivity.this).load(avatarPath).override(40,40).into(change_ig);
                        tv_user_show_name.setText(priuser.name);

                        tv_accout.setText(priuser.slogan);
                        tv_emailname.setText(priuser.email);
                        tv_telname.setText(priuser.tel);
                        tv_fakeusername.setText(priuser.nickname);
                        tv_sexname.setText(priuser.sex);
                        tv_truename.setText(priuser.realname);
                        tv_addressname.setText(priuser.livead);
                        //tv_yearname.setText(priuser.birthday);
                        //tv_monthname.setText(priuser.birthday);
                        //tv_dayname.setText(priuser.birthday);
                    } else {
                        Toast.makeText(UserActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
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
            DisplayUtil.showProgress(UserActivity.this,userProgressView,userView,false);
        }
    }
}