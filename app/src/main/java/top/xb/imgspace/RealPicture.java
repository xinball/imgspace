package top.xb.imgspace;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
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

import com.bm.library.PhotoView;

import java.io.File;
import java.util.ArrayList;

import top.xb.imgspace.adapter.MyPagerAdapter;
import top.xb.imgspace.utils.DisplayUtil;

public class RealPicture extends AppCompatActivity {

    private ViewPager mPictureviewpager;        //用于滑页
    private String path;
    private ArrayList<String> mImage; //用于滑页，存储所有图片id，如果想存储uri，需要改变ArrayList类型，并且MyPagerAdapter里的
                                                            //生成图片的函数（instantiateItem函数里的）也需要改变
    private MyPagerAdapter myPagerAdapter;         //存储所有图片的适配器

    private View real_bottomtitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_picture);
        //初始化列表，列表里保存的是图片的地址
        initView();
        //获得上一个活动传来的消息
        Intent intent=getIntent();
        mImage=intent.getStringArrayListExtra("files");
        path=intent.getStringExtra("path");

        real_bottomtitle=findViewById(R.id.real_bottomtitle);
        myPagerAdapter=new MyPagerAdapter(mImage,path,real_bottomtitle);
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
        Toast.makeText(RealPicture.this,"你点击了菜单按钮",Toast.LENGTH_SHORT).show();
    }
    //编辑按钮的点击函数
    public void edit(View view){
        String v = myPagerAdapter.getPrimaryItem();
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
                File file=new File(myPagerAdapter.getPrimaryItem());
                if(file.exists()&&!file.isDirectory())
                    if(file.delete()){
                        Toast.makeText(RealPicture.this,"删除图片成功！",Toast.LENGTH_SHORT).show();
                        sendBroadcast(new Intent("Refresh"));
                        finish();
                    }
                    else{
                        Toast.makeText(RealPicture.this,"删除图片失败！",Toast.LENGTH_SHORT).show();
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

}