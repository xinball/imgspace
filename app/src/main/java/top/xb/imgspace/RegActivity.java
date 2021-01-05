package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;

import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.User;
import top.xb.imgspace.utils.CheckUtil;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class RegActivity extends AppCompatActivity {

    private ProgressBar regProgressView;
    private View regView;
    private RegTask regTask;
    private IntentFilter intentFilter=null;


    private EditText reg_username;    //账号
    private EditText reg_password;    //密码
    private EditText reg_password2;   //确认密码
    private EditText reg_mail;        //邮箱
    private EditText reg_tel;         //手机号
    private Button reg_btn_sure;     //确认注册
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        reg_username = (EditText) findViewById(R.id.reg_username);
        reg_password = (EditText) findViewById(R.id.reg_password);
        reg_password2 = (EditText) findViewById(R.id.reg_password2);
        reg_mail = (EditText) findViewById(R.id.reg_mail);
        reg_tel = (EditText) findViewById(R.id.reg_tel);
        reg_btn_sure = (Button) findViewById(R.id.reg_btn_sure);
        regView = reg_btn_sure;
        regProgressView=findViewById(R.id.regProgress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {      //头部颜色
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        reg_btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //具体点击操作的逻辑
                String username = reg_username.getText().toString().trim();
                String password = reg_password.getText().toString().trim();
                String password2 = reg_password2.getText().toString().trim();
                String mail = reg_mail.getText().toString().trim();
                String tel = reg_tel.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(password2) || TextUtils.isEmpty(mail)) {
                    Toast.makeText(RegActivity.this, "请填写完整", Toast.LENGTH_SHORT).show();
                } else {
                    if(CheckUtil.isNumeric(username, 1, -1)){
                        Toast.makeText(RegActivity.this, "用户名不能全为数字", Toast.LENGTH_SHORT).show();
                    }else {
                        if(CheckUtil.isText(username,1,6)){
                            Toast.makeText(RegActivity.this, "用户名需要大于6位！", Toast.LENGTH_SHORT).show();
                        }else if(!CheckUtil.isText(username,1,-1)) {
                            Toast.makeText(RegActivity.this, "用户名格式错误！", Toast.LENGTH_SHORT).show();
                        }else{
                            if(!CheckUtil.isNumeric(tel,6,15)){
                                Toast.makeText(RegActivity.this, "手机号格式错误", Toast.LENGTH_SHORT).show();
                            }else{
                                if(password.length() <= 6){
                                    Toast.makeText(RegActivity.this, "密码需大于6位", Toast.LENGTH_SHORT).show();
                                }else {
                                    if (TextUtils.equals(password, password2)) {
                                        //调用注册函数
                                        regTask=new RegTask(username,password,mail,tel);
                                        DisplayUtil.showProgress(RegActivity.this,regProgressView,regView,false);
                                        regTask.execute();
                                    } else {
                                        Toast.makeText(RegActivity.this, "两次输入的密码不一样", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        }

                    }
                }
            }

        });
    }


    @SuppressLint("StaticFieldLeak")
    public class RegTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String name="";
        private String pwd="";
        private String email="";
        private String tel="";

        RegTask(String name, String pwd ,String email ,String tel) {
            this.name=name;
            this.pwd=pwd;
            this.email=email;
            this.tel=tel;
            SendData="{\"action\":\"send\",\"method\":\"reguser\",\"name\":\""+name+"\",\"pwd\":\""+pwd+"\",\"email\":\""+email+"\",\"tel\":\""+tel+"\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(RegActivity.this, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            regTask = null;
            DisplayUtil.showProgress(RegActivity.this,regProgressView,regView,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        Toast.makeText(RegActivity.this,"注册成功,请返回登录", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = ImgSpaceApplication.userInfo.edit();
                        editor.putString("name",name);
                        editor.putString("tel",tel);
                        //editor.putString("uid",);
                        editor.apply();
                        //发送广播
                        Intent intent = new Intent();
                        intent.setAction("regOK");
                        sendBroadcast(intent);
                        startActivity(new Intent(RegActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(RegActivity.this,returnmsg,true,"注册失败","确定",null);
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(regView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            regTask = null;
            DisplayUtil.showProgress(RegActivity.this,regProgressView,regView,false);
        }
    }
}