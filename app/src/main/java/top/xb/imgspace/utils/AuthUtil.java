package top.xb.imgspace.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class AuthUtil {
    private static final int REQUEST = 1;
    private static final String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE","android.permission.CAMERA","android.permission.INTERNET","android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};
    public static void verifyPermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,"android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS,REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
