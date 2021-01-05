package top.xb.imgspace.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import top.xb.imgspace.LoginActivity;
import top.xb.imgspace.R;

public class DisplayUtil {
    private static final String TAG = "DisplayUtil";
    private static int dialogResult;
    private static int hide=View.INVISIBLE;
    private static int dispaly=View.VISIBLE;

    public static SimpleDateFormat dayFormat=new SimpleDateFormat("MM/dd", Locale.getDefault());
    public static SimpleDateFormat yearFormat=new SimpleDateFormat("yyyy", Locale.getDefault());
    public static SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
    public static SimpleDateFormat AllFormat=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.getDefault());
    public static SimpleDateFormat dateFormatForFile=new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
    public static SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showProgress(Context context, View progressView, View formView, final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
            if(formView!=null){
                formView.setVisibility(show ? hide : dispaly);
                formView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        formView.setVisibility(show ? hide : dispaly);
                    }
                });
            }
            if(progressView!=null){
                progressView.setVisibility(show ? dispaly : hide);
                progressView.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressView.setVisibility(show ? dispaly : hide);
                    }
                });
            }
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            if(progressView!=null)
                progressView.setVisibility(show ? dispaly : hide);
            if(formView!=null)
                formView.setVisibility(show ? hide : dispaly);
        }
    }

    /*
     * Dialog对话框提示用户删除操作
     * position为删除图片位置
     */


    //快捷删除提示框
    public static void dialogProcess(Context context,String message,boolean Cancelable,String title, String posiBtn, String negaBtn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(Cancelable);
        builder.setIcon(context.getResources().getDrawable(R.drawable.progressbar));
        if(posiBtn!=null)
        builder.setPositiveButton(posiBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
            }
        });
        if(negaBtn!=null)
        builder.setNegativeButton(negaBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
            }
        });
        builder.create().show();
    }
    //快捷删除提示框
    public static AlertDialog.Builder dialogExcute(Context context, String message, boolean Cancelable, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(Cancelable);
        builder.setIcon(context.getResources().getDrawable(R.drawable.progressbar));
        return builder;
    }

    //编辑中保存bitmap图像
    public static String saveEditBitmap(Context context, String filePath,String fileName ,Bitmap mBitmap, Date date) {
        File filepath = new File(filePath);
        if(!filepath.exists()&&!filepath.isDirectory()) {//判断上传文件的保存目录是否存在
            Log.i("saveBitmap","目录 " + filepath + " 不存在，需要创建");//创建目录
            if(!filepath.mkdirs())
                return null;
        }
        String name=filepath.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+"_"+DisplayUtil.dateFormatForFile.format(date)+".png";
        File file=new File(name);
        try {
            if(!file.exists()||(file.exists()&&file.isDirectory())){
                if(!file.createNewFile())
                    return null;
            }
        } catch (IOException e) {
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        return name;
    }
    //保存bitmap图像
    public static String saveBitmap(Context context, String filePath,String fileName ,Bitmap mBitmap) {
        File filepath = new File(filePath);
        if(!filepath.exists()&&!filepath.isDirectory()) {//判断上传文件的保存目录是否存在
            Log.i("saveBitmap","目录 " + filepath + " 不存在，需要创建");//创建目录
            if(!filepath.mkdirs())
                return null;
        }
        String subfix=fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        String name=filepath.getPath()+"/"+fileName;
        File file=new File(name);
        try {
            if(!file.exists()||(file.exists()&&file.isDirectory())){
                if(!file.createNewFile())
                    return null;
            }
        } catch (IOException e) {
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        if(subfix.equals(".png"))
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        else if(subfix.equals(".jpg")||subfix.equals(".jpeg"))
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        return name;
    }
    //保存bitmap图像
    public static String saveBitmap(Context context, String srcPath,String dstPath,String fileName,int Quality) {
        File filepath = new File(dstPath);
        if(!filepath.exists()&&!filepath.isDirectory()) {//判断上传文件的保存目录是否存在
            Log.i("saveBitmap","目录 " + filepath + " 不存在，需要创建");//创建目录
            if(!filepath.mkdirs())
                return null;
        }BitmapFactory.Options op=new BitmapFactory.Options();
        //op.inJustDecodeBounds=true;
        op.inSampleSize=4;
        op.inJustDecodeBounds=false;
        Bitmap bmp=BitmapFactory.decodeFile(srcPath+"/"+fileName,op);
        if (bmp == null)
            return null;
        Log.d(TAG, "bmp:" + srcPath+"/"+fileName);
        int w = Math.min(bmp.getHeight(), bmp.getWidth());
        int topx = (bmp.getWidth() - w) / 2;
        int topy = (bmp.getHeight() - w) / 2;
        Bitmap bmp0 = Bitmap.createBitmap(bmp, topx, topy, w, w);
        Bitmap bmp1 = Bitmap.createScaledBitmap(bmp0, 32, 32, true);
        bmp.recycle();
        bmp0.recycle();
        String subfix=fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        String name=filepath.getPath()+"/"+fileName;
        File file=new File(name);
        try {
            if(!file.exists()||(file.exists()&&file.isDirectory())){
                if(!file.createNewFile())
                    return null;
            }
        } catch (IOException e) {
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(file.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        if(subfix.equals(".png"))
            bmp1.compress(Bitmap.CompressFormat.PNG, Quality, fos);
        else if(subfix.equals(".jpg")||subfix.equals(".jpeg"))
            bmp1.compress(Bitmap.CompressFormat.JPEG, Quality, fos);
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"保存图片失败！",Toast.LENGTH_SHORT).show();
            return null;
        }
        return null;
    }
    /*public static void hideKeyboard(Context context) {
        InputMethodManager imm =  (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);
        }
    }*/
}
