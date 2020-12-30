package top.xb.imgspace;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.*;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView outtv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        APIAddress.MainActivity=true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
         //       detectDiskWrites().detectNetwork().penaltyLog().build());
        EditText mulin=findViewById(R.id.multextin);
        outtv=findViewById(R.id.textViewout);
        Button getbtn=findViewById(R.id.main_get_btn);
        mulin.setText("{\n" +
                "\t\"action\":\"send\",\n" +
                "\t\"method\":\"uploadavatar\",\n" +
                "\t\"uid\":\"pelwdq\",\n" +
                "\t\"pwd\":\"123456\"\n" +
                "}");
        getbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequestWithHttpURLConnection(getApplicationContext(),mulin.getText().toString(),null);
            }
        });
    }
    private void sendRequestWithHttpURLConnection(Context context, String SendData, List<String> files){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> filesStr = new ArrayList<>();
                filesStr.add("/storage/emulated/0/Pictures/Screenshots/1.PNG");
                JSONObject jsonObject = HttpUtil.postRequest(context,SendData,filesStr);
                String returnvalue,msg;
                try {
                    returnvalue=jsonObject.getString("return");
                    msg=jsonObject.getString("returnmsg");
                    showResponse(returnvalue,msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void showResponse(final String returnvalue,final String msg){
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                outtv.setText(returnvalue+msg);
            }
        });
    }
}