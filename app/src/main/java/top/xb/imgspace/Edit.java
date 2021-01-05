package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;
import site.iway.androidhelpers.AssetsHelper;
import site.iway.androidhelpers.ImageCropper;
import site.iway.androidhelpers.UnitHelper;
import top.xb.imgspace.utils.DisplayUtil;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.itzs.imagecutter.ImageCutView;
import com.zjun.widget.RuleView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class Edit extends AppCompatActivity{
    private static final String TAG = "Edit";
    public static Bitmap bitmap;
    private int HaveSave;
    private ImageView imageView;
    private Bitmap lastBitmap;
    //private RuleView ruleView;
    private ImageCropper mImageCropper;
    private String file;
    private String path;
    private String name;
    SaveTask saveTask;
    View saveProgress;
    View saveView;
    public static final int CODE_CUT=2;
    public static final int CODE_ROTATE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        saveView=findViewById(R.id.edit_title);
        saveProgress=findViewById(R.id.editProgress);
        Intent intent=getIntent();
        file=intent.getStringExtra("file");
        path=file.substring(0,file.lastIndexOf("/"));
        name=file.substring(file.lastIndexOf("/"));
        imageView=(ImageView)findViewById(R.id.edit_image);
        bitmap = BitmapFactory.decodeFile(file);
        lastBitmap=bitmap;
        imageView.setImageBitmap(bitmap);
        HaveSave=0;
    }

    public void rotate(View view){
        Intent intent=new Intent(Edit.this,Rotate.class);
        intent.putExtra("Rotate",file);
        startActivityForResult(intent , CODE_ROTATE);

    }
    public void cut(View view){
        Intent intent=new Intent(Edit.this,Cut.class);
        intent.putExtra("file",file);
        startActivityForResult(intent , CODE_CUT);
    }
    public void back(){
        if(HaveSave==1||lastBitmap.equals(bitmap)) {        //若save函数已经保存过了，或者编辑前后的图片没有改变，将直接退出
            finish();
        }else {
            AlertDialog.Builder builder = DisplayUtil.dialogExcute(Edit.this,"是否放弃当前更改?",false,"");
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Log.i(TAG,"保存");
                    save(null);
                }
            });
            builder.setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Log.i(TAG,"放弃");
                    finish();
                }
            });
            builder.create().show();
        }
    }
    public void back(View view){
        back();
    }
    public void save(View view){
        if(lastBitmap.equals(bitmap)) ;
        else{
            saveTask=new SaveTask(Edit.this,path,name,bitmap);
            DisplayUtil.showProgress(Edit.this,saveProgress,saveView,true);
            saveTask.execute();
        }
        HaveSave=1;
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        back();
    }

    //Intent向上一个活动返回时的监听函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode) {
            case CODE_ROTATE: //返回的结果是来自于Rotate
            case CODE_CUT: //返回的结果是来自于Cut
                if (resultCode == RESULT_OK) {
                    imageView.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }
    }



    @SuppressLint("StaticFieldLeak")
    public class SaveTask extends AsyncTask<Void, Void, String> {
        Context context;
        String path;
        String name;
        Bitmap bitmap;
        SaveTask(Context context,String path,String name,Bitmap bitmap) {
            this.context=context;
            this.path=path;
            this.name=name;
            this.bitmap=bitmap;
        }
        @Override
        protected String doInBackground(Void... params) {
           return DisplayUtil.saveEditBitmap(context,path,name,bitmap,new Date());
        }

        @Override
        protected void onPostExecute(String result) {
            if(result!=null){
                Toast.makeText(Edit.this,"图片保存成功！新图片路径为："+result,Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("Refresh"));
            }
            DisplayUtil.showProgress(Edit.this,saveProgress,saveView,false);
        }

        @Override
        protected void onCancelled() {
            saveTask = null;
            DisplayUtil.showProgress(Edit.this,saveProgress,saveView,false);
        }
    }
}


