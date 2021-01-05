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
import com.google.android.material.snackbar.Snackbar;

import top.xb.imgspace.application.ImgSpaceApplication;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserActivity extends AppCompatActivity {
    private UserTask userTask;
    private ProgressBar userProgressView;
    private ScrollView userView;

    private ImageButton change_ig;
    private Button change_every;
    private ImageView iv_photo;
    private Bitmap head;// 头像Bitmap
    private static String path = "/sdcard/myHead/";// sd路径

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
                switch (v.getId()) {
                    case R.id.touxiang:// 更换头像
                        showTypeDialog();
                        break;
                }
            }
        });


    }



    //主函数结束
    private void initView() {


        Bitmap bt = BitmapFactory.decodeFile(path + "head.jpg");// 从SD卡中找头像，转换成Bitmap
        if (bt != null) {
            @SuppressWarnings("deprecation")
            Drawable drawable = new BitmapDrawable(bt);// 转换成drawable
            change_ig.setImageDrawable(drawable);
        } else {
            /**
             * 如果SD里面没有则需要从服务器取头像，取回来的头像再保存在SD中
             *
             */
        }
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
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                //打开文件
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
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
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹
        String fileName = path + "head.jpg";// 图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
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
            SendData="{\"action\":\"send\","+"\"method\":\"userinfo\","+"\"uid\":\"" + uid + "\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(getApplicationContext(), SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            userTask = null;
            DisplayUtil.showProgress(getApplicationContext(),userProgressView,userView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        SharedPreferences.Editor editor = ImgSpaceApplication.userInfo.edit();
                        editor.apply();
                        //发送广播刷新

                        Intent intent = new Intent();
                        intent.setAction("sendOK");
                        sendBroadcast(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
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
            DisplayUtil.showProgress(getApplicationContext(),userProgressView,userView,false);
        }
    }
}