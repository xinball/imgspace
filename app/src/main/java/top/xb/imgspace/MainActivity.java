package top.xb.imgspace;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.EnvironmentCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.net.URI;

import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.ui.home.HomeFragment;


public class MainActivity extends AppCompatActivity {

    public enum MODE{PIC,LIST,SPACE,SETTING};
    private TextView nav_title;
    private TextView nav_name;
    private ImageView nav_avatar;
    private ImageView homefile_btn;
    private ImageView picmode_btn;
    private ImageView picSmall_btn;
    private ImageView picBig_btn;

    private ImageView listmode_btn;
    public static MODE mode=MODE.PIC;
    /*private IntentFilter intentFilter;
    private final BroadcastReceiver ModeReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if()
            Toast.makeText(getApplicationContext(),"receive mode ok!", Toast.LENGTH_SHORT).show();
        }
    };*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nav_title=findViewById(R.id.nav_title);
        nav_name=findViewById(R.id.nav_name);;
        nav_avatar=findViewById(R.id.nav_avatar);
        APIAddress.MainActivity=true;
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_space, R.id.navigation_setting).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Intent intent=getIntent();
        String root=intent.getStringExtra("rootpath");
        if(root!=null){
            HomeFragment.rootPath=new File(root);
        }
        int modeord=intent.getIntExtra("mode", -1);
        if (modeord == MODE.PIC.ordinal())
            mode=MODE.PIC;
        else if (modeord == MODE.LIST.ordinal())
            mode=MODE.LIST;
        else if (modeord == MODE.SPACE.ordinal())
            mode=MODE.SPACE;
        else if (modeord == MODE.SETTING.ordinal())
            mode=MODE.SETTING;

        if(mode==MODE.PIC){
            pic();
            sendBroadcast(new Intent("PicRefresh"));
        }else if(mode==MODE.LIST){
            list();
            sendBroadcast(new Intent("ListRefresh"));
        }else if(mode==MODE.SPACE){
            space();
        }else if(mode==MODE.SETTING){
            setting();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 6666) {//判断是否选择和Code判断
            Uri uri;
            String root;
            if(data!=null){
                uri = data.getData();//拿到路
                String s =getImageAbsolutePath(this,uri);
                int i;
                for(i=s.length()-1;i>=0;i--){
                    if(s.charAt(i)=='/'){
                        break;
                    }
                }
                root=s.substring(0,i);
            }
            else{
                root=Environment.getExternalStorageDirectory().getPath();//拿到路
            }
            Intent intent=new Intent(MainActivity.this,MainActivity.class);
            intent.putExtra("rootpath",root);
            intent.putExtra("mode",mode.ordinal());
            startActivity(intent);
            finish();
        }
    }
    public void setNavText(String s){
        nav_title=findViewById(R.id.nav_title);
        nav_title.setText(s);
        Log.d("nav",s);
    }
    public void space(){
        nav_name=findViewById(R.id.nav_name);
        nav_avatar=findViewById(R.id.nav_avatar);
        homefile_btn=findViewById(R.id.homefile_btn);
        picmode_btn=findViewById(R.id.picmode_btn);
        listmode_btn=findViewById(R.id.listmode_btn);
        picSmall_btn=findViewById(R.id.picSmall_btn);
        picBig_btn=findViewById(R.id.picBig_btn);

        nav_name.setVisibility(View.VISIBLE);
        nav_avatar.setVisibility(View.VISIBLE);
        homefile_btn.setVisibility(View.INVISIBLE);
        picmode_btn.setVisibility(View.INVISIBLE);
        listmode_btn.setVisibility(View.INVISIBLE);
        picSmall_btn.setVisibility(View.INVISIBLE);
        picBig_btn.setVisibility(View.INVISIBLE);
    }
    public void pic(){
        nav_name=findViewById(R.id.nav_name);;
        nav_avatar=findViewById(R.id.nav_avatar);
        homefile_btn=findViewById(R.id.homefile_btn);
        picmode_btn=findViewById(R.id.picmode_btn);
        listmode_btn=findViewById(R.id.listmode_btn);
        picSmall_btn=findViewById(R.id.picSmall_btn);
        picBig_btn=findViewById(R.id.picBig_btn);


        nav_name.setVisibility(View.INVISIBLE);
        nav_avatar.setVisibility(View.INVISIBLE);
        homefile_btn.setVisibility(View.VISIBLE);
        picmode_btn.setVisibility(View.VISIBLE);
        listmode_btn.setVisibility(View.VISIBLE);
        picSmall_btn.setVisibility(View.VISIBLE);
        picBig_btn.setVisibility(View.VISIBLE);
    }
    public void list(){
        nav_name=findViewById(R.id.nav_name);;
        nav_avatar=findViewById(R.id.nav_avatar);
        homefile_btn=findViewById(R.id.homefile_btn);
        picmode_btn=findViewById(R.id.picmode_btn);
        listmode_btn=findViewById(R.id.listmode_btn);
        picSmall_btn=findViewById(R.id.picSmall_btn);
        picBig_btn=findViewById(R.id.picBig_btn);

        nav_name.setVisibility(View.INVISIBLE);
        nav_avatar.setVisibility(View.INVISIBLE);
        homefile_btn.setVisibility(View.VISIBLE);
        picmode_btn.setVisibility(View.VISIBLE);
        listmode_btn.setVisibility(View.VISIBLE);
        picSmall_btn.setVisibility(View.VISIBLE);
        picBig_btn.setVisibility(View.VISIBLE);
    }
    public void setting(){
        nav_name=findViewById(R.id.nav_name);;
        nav_avatar=findViewById(R.id.nav_avatar);
        homefile_btn=findViewById(R.id.homefile_btn);
        picmode_btn=findViewById(R.id.picmode_btn);
        listmode_btn=findViewById(R.id.listmode_btn);
        picSmall_btn=findViewById(R.id.picSmall_btn);
        picBig_btn=findViewById(R.id.picBig_btn);

        nav_name.setVisibility(View.INVISIBLE);
        nav_avatar.setVisibility(View.INVISIBLE);
        homefile_btn.setVisibility(View.INVISIBLE);
        picmode_btn.setVisibility(View.INVISIBLE);
        listmode_btn.setVisibility(View.INVISIBLE);
        picSmall_btn.setVisibility(View.INVISIBLE);
        picBig_btn.setVisibility(View.INVISIBLE);
    }
    public void PicBig(View view){
        if(mode==MODE.PIC){
            if(HomeFragment.picCol>2){
                HomeFragment.picCol--;
                /*ImageView iv=findViewById(R.id.homeimg);
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams) iv.getLayoutParams();
                lp.height=300/HomeFragment.picCol;
                iv.setLayoutParams(lp);
                iv.setMaxHeight(300/HomeFragment.picCol);
                iv.setMinimumHeight(300/HomeFragment.picCol);*/
                sendBroadcast(new Intent("PicRefresh"));
            }else{
                Toast.makeText(this,"不能再放大啦>_<!", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(HomeFragment.listEdge<135){
                HomeFragment.listEdge+=15;
                sendBroadcast(new Intent("ListRefresh"));
            }else{
                Toast.makeText(this,"不能再放大啦>_<!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void PicSmall(View view){
        if(mode==MODE.PIC) {
            if (HomeFragment.picCol < 6) {
                HomeFragment.picCol++;
                sendBroadcast(new Intent("PicRefresh"));
            } else {
                Toast.makeText(this, "不能再缩小啦>_<!", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(HomeFragment.listEdge>60){
                HomeFragment.listEdge-=15;
                sendBroadcast(new Intent("ListRefresh"));
            }else{
                Toast.makeText(this,"不能再缩小啦>_<!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void ChooseFile(View view){
        Intent i;
        i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, 6666);
    }
    public void PicMode(View view){
        pic();
        mode=MODE.PIC;
        sendBroadcast(new Intent("PicRefresh"));
    }
    public void ListMode(View view){
        list();
        mode=MODE.LIST;
        sendBroadcast(new Intent("ListRefresh"));
    }
    public void get(View view){
        Log.i("get","哈哈哈");
        startActivity(new Intent(this, SpaceActivity.class));
    }
    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     * @param
     * @param imageUri
     * @author yaoxing
     * @date 2014-10-12
     */
    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}