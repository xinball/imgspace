package top.xb.imgspace.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bm.library.PhotoView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import top.xb.imgspace.PinchImageView;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DownloadUtil;

public class MyPagerAdapter extends PagerAdapter {
    private static final String TAG = "MyPagerAdapter";
    private View dis;
    private boolean local;
    private boolean btnDisplay=true;
    Context context;

    private ArrayList<String> mImage;
    private String path;
    private String v;

    private ArrayList<Photo> photos;
    private Photo p;
    @Override
    public void setPrimaryItem(@NotNull ViewGroup container, int position, @NotNull Object object){
        if(local)
            v=path+mImage.get(position);
        else
            p=photos.get(position);
    }
    public Object getPrimaryItem(){
        if(local)
            return v;
        else
            return p;
    }

    public MyPagerAdapter(Context context,boolean local, ArrayList<String> mImage, String path, ArrayList<Photo> photos, View dis) {
        this.context=context;
        this.dis=dis;
        this.local=local;
        if(local){
            this.mImage = mImage;//接收传入的mIamge
            this.path=path;
        }else {
            this.photos=photos;
        }
    }

    @Override
    public int getCount() {
        if(local)
            return mImage.size();//在Viewpager显示3个页面
        else
            return photos.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view==object;
    }
    //设置每一页显示的内容
    @NotNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PinchImageView imageView = new PinchImageView(container.getContext());
        if(local){
            //imageView.setImageResource(mImage.get(position));//ImageView根据Id设置图片，如果使用URI的话这里需要改变
            //imageView.setImageURI(URI.create(mImage.get(position)));
            Bitmap bmp=BitmapFactory.decodeFile(path+mImage.get(position));
            imageView.setImageBitmap(bmp);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }else{
            String url=photos.get(position).url;
            File cachePath=new File(Environment.getExternalStorageDirectory()+APIAddress.WebcachePath);
            File cacheFile=new File(cachePath+url);
            if(!cachePath.exists()&&!cachePath.mkdirs()) {
                Log.i(TAG,cachePath+"   "+cacheFile);
                Toast.makeText(context,"创建缓存目录失败！",Toast.LENGTH_SHORT).show();
            }else{
                if(!cacheFile.exists())
                    DownloadUtil.SaveAndSetTask(context,APIAddress.WEB_IMG_URL+url,cachePath.getPath(),url,"图片缓存成功！",imageView);
                else{
                    Bitmap bmp=BitmapFactory.decodeFile(cacheFile.getPath());
                    imageView.setImageBitmap(bmp);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        }

        //view组件单击点击函数
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Real","click");
                if(btnDisplay) {
                    btnDisplay=false;
                    dis.setVisibility(View.INVISIBLE);
                }else{
                    btnDisplay=true;
                    dis.setVisibility(View.VISIBLE);
                }
            }
        });
        container.addView(imageView); // 添加到ViewPager容器
        return imageView;// 返回填充的View对象
    }
    // 销毁条目对象
    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((View) object);
    }
}
