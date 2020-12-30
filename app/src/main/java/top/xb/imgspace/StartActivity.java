package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import top.xb.imgspace.config.APIAddress;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        APIAddress.StartActivity=true;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        setContentView(R.layout.activity_start);
        Button ibt=findViewById(R.id.start_interbtn);
        Thread startThread=new Thread(){
            @Override
            public void run(){
                try{
                    sleep(5000);
                    if(!APIAddress.MainActivity){
                        Intent it=new Intent(getApplicationContext(),MainActivity.class);//启动MainActivity
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
                            if(!APIAddress.MainActivity){
                                Intent it=new Intent(getApplicationContext(),MainActivity.class);//启动MainActivity
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