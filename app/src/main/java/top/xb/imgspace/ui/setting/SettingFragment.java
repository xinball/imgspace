package top.xb.imgspace.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import top.xb.imgspace.MainActivity;
import top.xb.imgspace.R;

public class SettingFragment extends Fragment {

    private SettingViewModel settingViewModel;
    private ListView settingList;

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
        settingList=root.findViewById(R.id.settingList);
        //settingList.addHeaderView();


        return root;
    }
}