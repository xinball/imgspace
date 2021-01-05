package top.xb.imgspace.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bm.library.PhotoView;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;

import top.xb.imgspace.PinchImageView;

public class MyPagerAdapter extends PagerAdapter {
    private ArrayList<String> mImage;
    private String v;
    private String path;
    private View dis;
    private boolean btnDisplay=true;
    @Override
    public void setPrimaryItem(@NotNull ViewGroup container, int position, @NotNull Object object){
        v=path+mImage.get(position);
    }
    public String getPrimaryItem(){
        return v;
    }

    public MyPagerAdapter(ArrayList<String> mImage,String path,View dis) {
        this.mImage = mImage;//接收传入的mIamge
        this.path=path;
        this.dis=dis;
    }

    @Override
    public int getCount() {
        return mImage.size();//在Viewpager显示3个页面
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
        //imageView.setImageResource(mImage.get(position));//ImageView根据Id设置图片，如果使用URI的话这里需要改变
        //imageView.setImageURI(URI.create(mImage.get(position)));
        Bitmap bmp=BitmapFactory.decodeFile(path+mImage.get(position));
        imageView.setImageBitmap(bmp);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

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
