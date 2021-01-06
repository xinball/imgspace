package top.xb.imgspace.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.xb.imgspace.R;
import top.xb.imgspace.RealPicture;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.config.APIAddress;

public class MyGridAdapter extends BaseAdapter {
    private List<Photo> photos=null;
    private Context context;
    private int position;
    private boolean self;
    private LayoutInflater inflater;
    private static final String TAG = "MyGridAdapter";
    public MyGridAdapter(List<Photo> photos,Context context,int position) {
        this.photos=photos;
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.position=position;
        if(photos==null||photos.size()==0)
            self=false;
        else{
            if(photos.get(0).uid.equals(ImgSpaceApplication.getUid()))
                self=true;
            else
                self=false;
        }
    }
    @Override
    public int getCount() {
        if(photos!=null||photos.size()==0){
            Log.i(TAG, "getCount: "+photos.size());
            return photos.size();
        }else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        if(photos!=null||photos.size()==0)
            return photos.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            holder= new ViewHolder();
            convertView=inflater.inflate(R.layout.grid,null,false);
            holder.img=(ImageView)convertView.findViewById(R.id.grid_img);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder) convertView.getTag();
        }
        /*BitmapFactory.Options op=new BitmapFactory.Options();
        op.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(photos.get(position).url,op);
        op.inSampleSize=4;
        op.inJustDecodeBounds=false;
        Bitmap bmp=BitmapFactory.decodeFile(photos.get(position).url,op);
        if(bmp==null) Log.i(TAG, "getView: "+"bmp empty!");
        holder.img.setImageBitmap(bmp);*/
        String name=photos.get(position).url;
        File cacheFile=new File(Environment.getExternalStorageDirectory()+APIAddress.WebcachePath+name);
        String photoPath= APIAddress.WEB_IMG_URL+name;
        if(cacheFile.exists())
            Glide.with(context).load(cacheFile.getPath()).override(100,100).centerCrop().into(holder.img);
        else
            Glide.with(context).load(photoPath).override(100,100).centerCrop().into(holder.img);
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,"img clicked  "+position+"/"+position,Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(context, RealPicture.class);
                intent.putExtra("local",false);
                intent.putExtra("position",position);
                intent.putExtra("self",self);
                intent.putParcelableArrayListExtra("photos", (ArrayList<? extends Parcelable>) photos);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
    private static class ViewHolder{
        ImageView img;
    }
}
