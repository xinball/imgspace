package top.xb.imgspace.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {
    /**
     * 获取网络图片
     *
     * @param imageurl 图片网络地址
     * @return Bitmap 返回位图
     */
    public static Bitmap GetImageInputStream(String imageurl) {
        URL url;
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try {
            url = new URL(imageurl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000); //超时设置
            connection.setDoInput(true);
            connection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 保存位图到本地
     *
     * @param bitmap
     * @param pathDir 本地路径
     * @return void
     */
    public static void SavaImage(Context context, Bitmap bitmap, String pathDir, String imageName, String toastMsg) {
        if (bitmap != null) {
            File file = new File(pathDir);
            FileOutputStream fileOutputStream = null;
            //文件夹不存在，则创建它,只是创建wx文件夹
            if (!file.exists()) {
                file.mkdir();
            }
            try {
                fileOutputStream = new FileOutputStream(pathDir + "/" + imageName);
                if(imageName.endsWith(".png"))
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                if(imageName.endsWith(".jpg")||imageName.endsWith(".jpeg"))
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                //ToastUtil.showText(context,toastMsg,Toast.LENGTH_SHORT);
                Toast.makeText(context,toastMsg,Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static void SaveImageTask(Context context, String url, String path, String imageName, String toastMsg) {
        new ImageTask(context, url, path, imageName, toastMsg).execute();
    }
    public static void SaveAndSetTask(Context context, String url, String path, String imageName, String toastMsg, ImageView view) {
        new ImageTask(context, url, path, imageName, toastMsg ,view).execute();
    }


    /**
     * 异步线程下载图片
     */
    static class ImageTask extends AsyncTask<String, Integer, Bitmap> {
        private Context mContext;
        private String url;
        private String path;
        private String toastMsg;
        private String imageName;
        private ImageView view;
        Bitmap bitmap = null;

        public ImageTask(Context context, String url, String path, String imageName, String toastMsg) {
            this.mContext = context;
            this.url = url;
            this.path = path;
            this.toastMsg = toastMsg;
            this.imageName = imageName;
        }
        public ImageTask(Context context, String url, String path, String imageName, String toastMsg, ImageView view) {
            this.mContext = context;
            this.url = url;
            this.path = path;
            this.toastMsg = toastMsg;
            this.imageName = imageName;
            this.view=view;
        }

        protected Bitmap doInBackground(String... params) {
            bitmap = GetImageInputStream(url);
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                SavaImage(mContext, result, path, imageName, toastMsg);
                if(view!=null){
                    //Bitmap bmp=BitmapFactory.decodeFile(path+imageName);
                    view.setImageBitmap(bitmap);
                    view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }else{
                Toast.makeText(mContext,"加载图片失败！",Toast.LENGTH_SHORT).show();
            }
        }

    }
}
