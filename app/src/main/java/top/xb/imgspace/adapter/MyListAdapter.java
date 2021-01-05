package top.xb.imgspace.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import top.xb.imgspace.R;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.utils.DisplayUtil;

public class MyListAdapter extends BaseAdapter {
    private List<Message> messages;
    private LayoutInflater inflater;
    private final Context context;
    private List<List<Photo>> photoslist;

    private static final String TAG = "ListAdapter";
    private MyGridAdapter gridAdapter;
    public MyListAdapter(List<Message> messages,List<List<Photo>> photoslist,Context mContext){
        super();
        this.messages=messages;
        this.context=mContext;
        this.photoslist=photoslist;
        this.inflater=LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
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
            convertView= inflater.inflate(R.layout.userspacelist,null,false);
            holder.userspace_Day=(TextView) convertView.findViewById(R.id.userspace_Day);
            holder.userspace_Year=(TextView)convertView.findViewById(R.id.userspace_Year);
            holder.userspace_time=(TextView)convertView.findViewById(R.id.userspace_time);
            holder.userspace_altertime=(TextView)convertView.findViewById(R.id.userspace_altertime);
            holder.userspace_content=(TextView)convertView.findViewById(R.id.userspace_content);
            holder.userspace_photogrid=(GridView)convertView.findViewById(R.id.userspace_photogrid);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.userspace_Day.setText(DisplayUtil.dayFormat.format(messages.get(position).sendTime));
        holder.userspace_Year.setText(DisplayUtil.yearFormat.format(messages.get(position).sendTime));
        holder.userspace_time.setText(DisplayUtil.timeFormat.format(messages.get(position).sendTime));
        holder.userspace_altertime.setText(DisplayUtil.AllFormat.format(messages.get(position).alterTime));
        holder.userspace_content.setText(messages.get(position).content);
        if(holder.userspace_photogrid!=null){
            gridAdapter=new MyGridAdapter(photoslist.get(position),context,position);
            holder.userspace_photogrid.setAdapter(gridAdapter);
        }
        holder.userspace_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"content clicked"+position,Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }
    private static class ViewHolder{
        TextView userspace_Day;
        TextView userspace_Year;
        TextView userspace_content;
        TextView userspace_time;
        TextView userspace_altertime;
        GridView userspace_photogrid;
    }
}
