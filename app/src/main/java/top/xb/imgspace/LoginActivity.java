package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.*;
import top.xb.imgspace.utils.CheckUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;
import top.xb.imgspace.utils.JSONUtil;

public class LoginActivity extends AppCompatActivity {
    private final String LoginUid="loginuid";
    private final String LoginName="loginname";
    private final String LoginTel="logintel";
    private ProgressBar loginProgressView;
    private View loginView;
    private LoginTask loginTask;
    private IntentFilter intentFilter;
    private boolean rememberPwd=true;
    private final String TAG="LoginActivity";

    private EditText et_username;       //登录用户账号
    private EditText et_password;       //登录用户密码
    private EditText et_password2;      //注册页面的确认密码
    private EditText et_mail;           //邮箱
    private Button btn_login;           //登录
    private TextView btn_reg;           //登录
    private CheckBox checkbox;          //记住密码


    /*private final BroadcastReceiver NotLoginReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(),"not login!", Toast.LENGTH_SHORT).show();
        }
    };*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*intentFilter=new IntentFilter();
        intentFilter.addAction("notlogin");
        registerReceiver(NotLoginReceiver,intentFilter);*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {     //头部颜色
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        et_username =(EditText) findViewById(R.id.et_username);
        et_password =(EditText) findViewById(R.id.et_password);
        checkbox = (CheckBox) findViewById(R.id.checkBox);
        btn_login =(Button) findViewById(R.id.button_login);
        btn_reg=findViewById(R.id.button_toreg);
        //bk_btn =(ImageButton)findViewById(R.id.log_bk_btn) ;
        loginProgressView=findViewById(R.id.loginProgress);
        loginView=btn_login;

        String uid=ImgSpaceApplication.getUid();
        String pwd=ImgSpaceApplication.getPwd();
        checkbox.setChecked(ImgSpaceApplication.getRememberPwd());
        if(uid!=null&&pwd!=null){
                loginTask=new LoginTask(uid,pwd,LoginUid);
                DisplayUtil.showProgress(LoginActivity.this,loginProgressView,loginView,true);
                loginTask.execute();
        }else{
            String name=ImgSpaceApplication.getName();
            if(name!=null) {
                et_username.setText(name);
            }else{
                String tel=ImgSpaceApplication.getTel();
                if(tel!=null) {
                    et_username.setText(tel);
                }
            }
        }
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //具体点击操作的逻辑
                String username =et_username.getText().toString().trim();
                String password =et_password.getText().toString().trim();
                if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"密码或账号不能为空",Toast.LENGTH_SHORT).show();
                }
                else {
                    if(CheckUtil.isNumeric(username,7,-1)){        //手机号登录
                        //调用数据库手机号登录
                        rememberPwd=checkbox.isChecked();
                        loginTask=new LoginTask(username,password,LoginTel);
                        DisplayUtil.showProgress(LoginActivity.this,loginProgressView,loginView,true);
                        loginTask.execute();
                    }
                    else if(CheckUtil.isText(username,7,-1)){       //用户名登录
                        //调用数据库用户名登录
                        rememberPwd=checkbox.isChecked();
                        loginTask=new LoginTask(username,password,LoginName);
                        DisplayUtil.showProgress(LoginActivity.this,loginProgressView,loginView,true);
                        loginTask.execute();
                    }else{
                        Toast.makeText(LoginActivity.this,"暂不支持UID登录！",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);      //跳到首页，改成注册界面
                startActivity(intent);
            }
        });
    }




    @SuppressLint("StaticFieldLeak")
    public class LoginTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String name="";
        private String tel="";
        private String pwd="";

        LoginTask(String loginname, String pwd ,String loginMethod) {
            this.pwd=pwd;
            String method="\"method\":\""+loginMethod+"\",";
            String login="";
            switch (loginMethod) {
                case LoginUid:
                    uid=loginname;
                    login = "\"uid\":\"" + uid + "\",";
                    break;
                case LoginName:
                    name=loginname;
                    login = "\"name\":\"" + name + "\",";
                    break;
                case LoginTel:
                    tel=loginname;
                    login = "\"tel\":\"" + tel + "\",";
                    break;
            }
            SendData="{\"action\":\"receive\","+method+login+"\"pwd\":\""+pwd+"\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(LoginActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            loginTask = null;
            DisplayUtil.showProgress(LoginActivity.this,loginProgressView,loginView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        SharedPreferences.Editor editor = ImgSpaceApplication.userInfo.edit();
                        PriUser priuser= result.getObject("returnmsg",PriUser.class);
                        //editor.putString("uid",);
                        editor.putString("uid",priuser.uid);
                        editor.putBoolean("rememberPwd",rememberPwd);
                        if(rememberPwd){
                            Log.d(TAG,priuser.pwd);
                            editor.putString("pwd",priuser.pwd);
                        }else{
                            Log.d(TAG,"remove pwd");
                            editor.remove("pwd");
                        }
                        editor.apply();
                        //发送广播
                        Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);      //跳到首页，改成用户首页
                        startActivity(intent);
                        finish();
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(LoginActivity.this,returnmsg,true,"登录失败","确定",null);
                        //Toast.makeText(LoginActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(loginView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            loginTask = null;
            DisplayUtil.showProgress(LoginActivity.this,loginProgressView,loginView,false);
        }
    }
}