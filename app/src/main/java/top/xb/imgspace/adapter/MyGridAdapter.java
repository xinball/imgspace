package top.xb.imgspace.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import top.xb.imgspace.R;
import top.xb.imgspace.bean.Photo;

public class MyGridAdapter extends BaseAdapter {
    private List<Photo> photos;
    private Context context;
    private int pos;
    private LayoutInflater inflater;
    private static final String TAG = "MyGridAdapter";
    public MyGridAdapter(List<Photo> photos,Context mcontext,int p) {
        this.photos=photos;
        context=mcontext;
        inflater=LayoutInflater.from(mcontext);
        pos=p;
    }
    @Override
    public int getCount() {
        Log.i(TAG, "getCount: "+photos.size());
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
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
        Glide.with(context).load(photos.get(position).url).into(holder.img);
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"img clicked  "+pos+"/"+position,
                        Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }
    private static class ViewHolder{
        ImageView img;
    }
}
