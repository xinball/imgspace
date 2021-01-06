package top.xb.imgspace.ui.setting;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import top.xb.imgspace.MainActivity;
import top.xb.imgspace.R;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.DeleteFileUtil;

public class SettingFragment extends Fragment {

    private SettingViewModel settingViewModel;
    private Button clearcache;
    private Button clearlogin;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        MainActivity mainActivity=((MainActivity)getActivity());
        mainActivity.setNavText(getString(R.string.title_setting));
        mainActivity.setting();

        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_setting, container, false);
        //final TextView textView = root.findViewById(R.id.nav_text);
        settingViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        clearcache=root.findViewById(R.id.clearcache);
        clearlogin=root.findViewById(R.id.clearlogin);
        clearcache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String WebCachae= Environment.getExternalStorageDirectory()+ APIAddress.WebcachePath;
                String LocalCachae= Environment.getExternalStorageDirectory()+ APIAddress.LocalcachePath;
                if(new File(WebCachae).exists())
                    DeleteFileUtil.deleteDirectory(WebCachae);
                if(new File(LocalCachae).exists())
                    DeleteFileUtil.deleteDirectory(LocalCachae);
                Toast.makeText(getContext(),"恭喜你，缓存已清理完毕！",Toast.LENGTH_LONG).show();
            }
        });
        clearlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImgSpaceApplication.clear(getContext());
                Toast.makeText(getContext(),"用户已注销！",Toast.LENGTH_LONG).show();
            }
        });


        return root;
    }
}