package top.xb.imgspace.ui.space;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.xb.imgspace.LoginActivity;
import top.xb.imgspace.MainActivity;
import top.xb.imgspace.R;
import top.xb.imgspace.SendActivity;
import top.xb.imgspace.SpaceActivity;
import top.xb.imgspace.UserActivity;
import top.xb.imgspace.UserspaceActivity;
import top.xb.imgspace.adapter.SpaceListAdapter;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.bean.PriUser;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;


public class SpaceFragment extends Fragment {

    private static final String TAG = "SpaceFrag";
    private SpaceViewModel spaceViewModel;
    private MainActivity mainActivity;

    private IntentFilter intentFilter;
    private LoginTask loginTask;
    PriUser priuser;

    private View spaceloginProgress;
    private ImageView nav_avatar;
    private TextView nav_name;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=((MainActivity)getActivity());
    }
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        spaceViewModel = new ViewModelProvider(this).get(SpaceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_space, container, false);
        //final TextView textView = root.findViewById(R.id.nav_text);
        spaceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        mainActivity.setNavText(getString(R.string.title_space));
        mainActivity.space();
        spaceloginProgress=root.findViewById(R.id.spaceloginProgress);
        nav_avatar=mainActivity.findViewById(R.id.nav_avatar);
        nav_name=mainActivity.findViewById(R.id.nav_name);

        //SharedPreferences loginUser = mainActivity.getSharedPreferences("loginUser", Activity.MODE_PRIVATE);
        if(!ImgSpaceApplication.isLoggedIn()){
            mainActivity.startActivity(new Intent(mainActivity,LoginActivity.class));
            //mainActivity.sendBroadcast(new Intent("notlogin"));
            mainActivity.finish();
        }else {
            String uid=ImgSpaceApplication.getUid();
            String pwd=ImgSpaceApplication.getPwd();
            loginTask=new LoginTask(uid,pwd);
            DisplayUtil.showProgress(mainActivity,spaceloginProgress,null,true);
            loginTask.execute();
        }

        Button touserspace=root.findViewById(R.id.touserspace);
        Button tospace=root.findViewById(R.id.tospace);
        Button sendnew=root.findViewById(R.id.sendnew);
        touserspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mainActivity, UserspaceActivity.class);
                intent.putExtra("uid",priuser.uid);
                startActivity(intent);
            }
        });
        tospace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mainActivity, SpaceActivity.class);
                intent.putExtra("uid",priuser.uid);
                startActivity(intent);
            }
        });
        sendnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mainActivity, SendActivity.class));
            }
        });

        return root;
    }

    @SuppressLint("StaticFieldLeak")
    public class LoginTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String pwd="";

        LoginTask(String uid, String pwd) {
            this.uid=uid;
            this.pwd=pwd;
            SendData="{\"action\":\"receive\",\"method\":\"loginuid\",\"uid\":\"" + uid + "\","+"\"pwd\":\""+pwd+"\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(mainActivity, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            loginTask = null;
            DisplayUtil.showProgress(mainActivity,spaceloginProgress,null,false);
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
                            Glide.with(mainActivity).load(avatarcache.getPath()).override(40,40).into(nav_avatar);
                        else
                            Glide.with(mainActivity).load(avatarPath).override(40,40).into(nav_avatar);
                        nav_name.setText(priuser.name);
                        nav_avatar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(mainActivity, UserActivity.class);
                                intent.putExtra("uid",priuser.uid);
                                mainActivity.startActivity(intent);
                            }
                        });
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(mainActivity,returnmsg,true,"登录失败","确定",null);
                        mainActivity.startActivity(new Intent(mainActivity,LoginActivity.class));
                        mainActivity.finish();
                        //mainActivity.sendBroadcast(new Intent("notlogin"));
                        //Toast.makeText(LoginActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                //Snackbar.make(loginView, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }

        @Override
        protected void onCancelled() {
            loginTask = null;
            DisplayUtil.showProgress(mainActivity,spaceloginProgress,null,false);
        }
    }
}