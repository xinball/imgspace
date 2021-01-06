package top.xb.imgspace;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;
import top.xb.imgspace.utils.JSONUtil;

public class SendActivity extends AppCompatActivity {
    private UploadMsgTask uploadMsgTask=null;
    private ProgressBar sendProgressView;
    private View sendView;
    private Button send_btn;
    private IntentFilter intentFilter;
    private EditText mContent;



    private GridView gridView1;              //网格显示缩略图
    private Button buttonPublish;            //发布按钮
    private Button buttonCancel;             //取消发布
    private final int IMAGE_OPEN = 1;        //打开图片标记
    private String pathImage;                //选择图片路径
    private Bitmap bmp;                      //导入临时图片
    private ArrayList<HashMap<String, Object>> imageItem;
    private SimpleAdapter simpleAdapter;     //适配器
    private EditText myEditText;              //文本框


    private final BroadcastReceiver  SendOKReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(SendActivity.this,"send ok!", Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {  //头部颜色
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        AuthUtil.verifyPermissions(this);                 //获得存储权限
        myEditText = (EditText) findViewById(R.id.editText1);    //获得文本框

        sendView = findViewById(R.id.Layout_btn);
        sendProgressView = findViewById(R.id.sendProgress);

        intentFilter=new IntentFilter();
        intentFilter.addAction("sendOK");
        registerReceiver(SendOKReceiver,intentFilter);


        /*
         * 防止键盘挡住输入框
         * 不希望遮挡设置activity属性 android:windowSoftInputMode="adjustPan"
         * 希望动态调整高度 android:windowSoftInputMode="adjustResize"
         */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        //获取控件对象
        gridView1 = (GridView) findViewById(R.id.gridView1);
        buttonPublish = (Button)findViewById(R.id.send_btn1);
        buttonCancel = (Button)findViewById(R.id.send_btn2);

        /*
         * 载入默认图片添加图片加号
         * 通过适配器实现
         * SimpleAdapter参数imageItem为数据源 R.layout.griditem_addpic为布局
         */
        //获取资源图片加号
        bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_add);
        imageItem = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("itemImage", bmp);
        imageItem.add(map);
        simpleAdapter = new SimpleAdapter(this,imageItem, R.layout.griditem_addpic,
                new String[] { "itemImage"},
                new int[] { R.id.imageView1});



        /*
         * HashMap载入bmp图片在GridView中不显示,但是如果载入资源ID能显示 如
         * map.put("itemImage", R.drawable.img);
         * 解决方法:
         *              1.自定义继承BaseAdapter实现
         *              2.ViewBinder()接口实现
         *  参考 http://blog.csdn.net/admin_/article/details/7257901
         */
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                // TODO Auto-generated method stub
                /*if(view instanceof ImageView && data instanceof String){
                    Glide.with(getApplicationContext()).load(data).into((ImageView) view);
                    return true;
                }*/
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView i = (ImageView)view;
                    i.setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
            }
        });
        gridView1.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();


        /*
         * 监听GridView点击事件
         * 报错:该函数必须抽象方法 故需要手动导入import android.view.View;
         */
        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {

                if( imageItem.size() == 10) { //第一张为默认图片
                    Toast.makeText(SendActivity.this, "图片数9张已满", Toast.LENGTH_SHORT).show();
                }
                else if(position == 0) { //点击图片位置为+ 0对应0张图片
                    Toast.makeText(SendActivity.this, "添加图片", Toast.LENGTH_SHORT).show();
                    //选择图片
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, IMAGE_OPEN);
                    //通过onResume()刷新数据
                }
                else {
                    dialog(position);
                    //Toast.makeText(MainActivity.this, "点击第"+(position + 1)+" 号图片",
                    //      Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //具体点击操作的逻辑
                String myEdit = myEditText.getText().toString().trim();
                if(TextUtils.isEmpty(myEdit)){
                    Toast.makeText(SendActivity.this, "文字为空", Toast.LENGTH_SHORT).show();
                }else {
                    /*
                    调用发送函数

                    */

                    List<String> filesStr = new ArrayList<>();

                    for(int i=1;i<imageItem.size();i++){
                        filesStr.add((String) imageItem.get(i).get("file"));
                    }
                    if(ImgSpaceApplication.isLoggedIn()){
                        String uid=ImgSpaceApplication.getUid();
                        String pwd=ImgSpaceApplication.getPwd();
                        uploadMsgTask=new UploadMsgTask(uid, pwd, myEdit, filesStr);
                        DisplayUtil.showProgress(SendActivity.this,sendProgressView,sendView,true);
                        uploadMsgTask.execute();
                    }else{
                        Toast.makeText(SendActivity.this, "未登录！", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SendActivity.this,LoginActivity.class));
                        finish();
                    }
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //具体点击操作的逻辑
                SendActivity.this.finish();
            }
        });

    }
    //主函数结束


    //获取图片路径 响应startActivityForResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //打开图片
        if(resultCode==RESULT_OK && requestCode==IMAGE_OPEN) {
            Uri uri = data.getData();
            if (!TextUtils.isEmpty(uri.getAuthority())) {
                //查询选择图片
                @SuppressLint("Recycle")
                Cursor cursor = getContentResolver().query(
                        uri,
                        new String[] { MediaStore.Images.Media.DATA },
                        null,
                        null,
                        null);
                //返回 没找到选择图片
                if (null == cursor) {
                    return;
                }
                //光标移动至开头 获取图片路径
                cursor.moveToFirst();
                pathImage = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        }  //end if 打开图片
    }

    //刷新图片
    @Override
    protected void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(pathImage)){
            Bitmap addbmp= BitmapFactory.decodeFile(pathImage);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", addbmp);
            map.put("file", pathImage);
            imageItem.add(map);
            simpleAdapter = new SimpleAdapter(this,
                    imageItem, R.layout.griditem_addpic,
                    new String[] { "itemImage"}, new int[] { R.id.imageView1});
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data,
                                            String textRepresentation) {
                    // TODO Auto-generated method stub
                    if(view instanceof ImageView && data instanceof Bitmap){
                        ImageView i = (ImageView)view;
                        i.setImageBitmap((Bitmap) data);
                        return true;
                    }
                    return false;
                }
            });
            gridView1.setAdapter(simpleAdapter);
            simpleAdapter.notifyDataSetChanged();
            //刷新后释放防止手机休眠后自动添加
            pathImage = null;
        }
    }

    /*
     * Dialog对话框提示用户删除操作
     * position为删除图片位置
     */
    //快捷删除提示框
    protected void dialog(final int position) {
        AlertDialog.Builder builder = DisplayUtil.dialogExcute(SendActivity.this,"确认移除已添加图片吗？",true,"提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                imageItem.remove(position);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @SuppressLint("StaticFieldLeak")
    public class UploadMsgTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String message="";
        private List<String> files=null;

        UploadMsgTask(String uid, String pwd, String message, String[] files) {
            this.uid=uid;
            this.message=message;
            SendData="{\"action\":\"send\",\"method\":\"uploadmsg\",\"uid\":\""+uid+"\",\"pwd\":\""+pwd+"\",\"message\":\""+message+"\"}";
            this.files= Arrays.asList(files);
        }
        UploadMsgTask(String uid, String pwd, String message, List<String> files) {
            this.uid=uid;
            this.message=message;
            SendData="{\"action\":\"send\",\"method\":\"uploadmsg\",\"uid\":\""+uid+"\",\"pwd\":\""+pwd+"\",\"message\":\""+message+"\"}";
            this.files=files;
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(SendActivity.this, SendData,files);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            uploadMsgTask = null;
            DisplayUtil.showProgress(SendActivity.this,sendProgressView,sendView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        String returnmsg=result.getString("returnmsg");
                        AlertDialog.Builder builder=DisplayUtil.dialogExcute(SendActivity.this,returnmsg,false,"发送成功");
                        builder.setPositiveButton("好", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                Intent intent=new Intent(SendActivity.this,SpaceActivity.class);
                                intent.putExtra("uid",uid);
                                startActivity(intent);
                                finish();
                            }
                        });
                        builder.show();
                    } else {
                        //Toast.makeText(getApplicationContext(), result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                        Snackbar.make(sendView, result.getString("returnmsg"), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(sendView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            uploadMsgTask = null;
            DisplayUtil.showProgress(SendActivity.this,sendProgressView,sendView,false);
        }
    }
}