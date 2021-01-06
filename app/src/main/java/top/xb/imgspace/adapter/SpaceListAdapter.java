package top.xb.imgspace.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;

import top.xb.imgspace.R;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.DisplayUtil;

public class SpaceListAdapter extends BaseAdapter {
    private List<Message> messages=null;
    private LayoutInflater inflater;
    private final Context context;
    private List<List<Photo>> photoslist=null;
    List<String> names=null;
    List<String> avatars=null;

    private static final String TAG = "SpaceListAdapter";
    private MyGridAdapter gridAdapter;
    public SpaceListAdapter(List<Message> messages,List<List<Photo>> photoslist,List<String> names,List<String> avatars,Context mContext){
        super();
        this.messages=messages;
        this.context=mContext;
        this.photoslist=photoslist;
        this.names=names;
        this.avatars=avatars;
        this.inflater=LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        if(messages!=null||messages.size()==0)
            return messages.size();
        else
            return 0;
    }

    @Override
    public Message getItem(int position) {
        if(messages!=null||messages.size()==0)
            return messages.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        Log.i(TAG, "getView------------scuccess!!! "+position);
        if(convertView==null){
            holder= new ViewHolder();
            convertView= inflater.inflate(R.layout.spacelist,null,false);
            holder.space_avatar=(ImageView) convertView.findViewById(R.id.space_avatar);
            holder.space_othername=(TextView)convertView.findViewById(R.id.space_othername);
            holder.space_time=(TextView)convertView.findViewById(R.id.space_time);
            holder.space_altertime=(TextView)convertView.findViewById(R.id.space_altertime);
            holder.space_content=(TextView)convertView.findViewById(R.id.space_content);
            holder.space_photogrid=(GridView)convertView.findViewById(R.id.space_photogrid);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.space_othername.setText(names.get(position));
        holder.space_time.setText("发布:"+DisplayUtil.AllFormat.format(messages.get(position).sendTime));
        Date alterTime=messages.get(position).alterTime;
        if(alterTime!=null)
            holder.space_altertime.setText("修改:"+DisplayUtil.AllFormat.format(alterTime));
        else
            holder.space_altertime.setText("");
        holder.space_content.setText(messages.get(position).content);
        String avatarPath=APIAddress.WEB_IMG_URL+messages.get(position).uid+avatars.get(position);
        Log.i(TAG,avatarPath);
        Glide.with(context).load(avatarPath).override(40,40).into(holder.space_avatar);
        if(holder.space_photogrid!=null){
            gridAdapter=new MyGridAdapter(photoslist.get(position),context,position);
            holder.space_photogrid.setAdapter(gridAdapter);
        }
        holder.space_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"content clicked"+position,Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }
    private static class ViewHolder{
        ImageView space_avatar;
        TextView space_othername;
        TextView space_content;
        TextView space_time;
        TextView space_altertime;
        GridView space_photogrid;
    }
}
