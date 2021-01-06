package top.xb.imgspace.ui.space;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import top.xb.imgspace.LoginActivity;
import top.xb.imgspace.MainActivity;
import top.xb.imgspace.R;
import top.xb.imgspace.UserspaceActivity;
import top.xb.imgspace.application.ImgSpaceApplication;


public class SpaceFragment extends Fragment {

    private static final String TAG = "SpaceFrag";
    private SpaceViewModel spaceViewModel;
    private MainActivity mainActivity;
    private IntentFilter intentFilter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=((MainActivity)getActivity());
    }
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        mainActivity.setNavText(getString(R.string.title_space));
        mainActivity.space();

        //SharedPreferences loginUser = mainActivity.getSharedPreferences("loginUser", Activity.MODE_PRIVATE);
        if(!ImgSpaceApplication.isLoggedIn()){
            mainActivity.startActivity(new Intent(mainActivity,LoginActivity.class));
            //mainActivity.sendBroadcast(new Intent("notlogin"));
            mainActivity.finish();
        }
        ((MainActivity)getActivity()).setNavText(getString(R.string.title_space));

        spaceViewModel = new ViewModelProvider(this).get(SpaceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_space, container, false);
        //final TextView textView = root.findViewById(R.id.nav_text);
        spaceViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }

}