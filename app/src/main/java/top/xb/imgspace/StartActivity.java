package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.config.APIAddress;

public class StartActivity extends AppCompatActivity {

    boolean start = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        APIAddress.StartActivity=true;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        start=ImgSpaceApplication.isFirstStart();
        if(!start){
            startActivity(new Intent(StartActivity.this,MainActivity.class));
            finish();
        }
        SharedPreferences.Editor editor = ImgSpaceApplication.userInfo.edit();
        editor.putInt("start",0);
        editor.apply();
        setContentView(R.layout.activity_start);
        Button ibt=findViewById(R.id.start_interbtn);
        Thread startThread=new Thread(){
            @Override
            public void run(){
                try{
                    sleep(5000);
                    if(start){
                        start=false;
                        Intent it=new Intent(StartActivity.this,MainActivity.class);//启动MainActivity
                        startActivity(it);
                        APIAddress.StartActivity=false;
                        finish();//关闭活动
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        Thread clickThread=new Thread(){
            @Override
            public void run(){
                try{
                    ibt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(start){
                                start=false;
                                Intent it=new Intent(StartActivity.this,MainActivity.class);//启动MainActivity
                                startActivity(it);
                                APIAddress.StartActivity=false;
                                finish();//关闭活动
                            }
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        startThread.start();
        clickThread.start();
    }
}