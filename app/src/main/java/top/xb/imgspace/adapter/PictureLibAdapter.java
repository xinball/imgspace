package top.xb.imgspace.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import top.xb.imgspace.R;
import top.xb.imgspace.RealPicture;
import top.xb.imgspace.bean.PictureLib;

public class PictureLibAdapter extends RecyclerView.Adapter<PictureLibAdapter.ViewHolder> {
    private Context mContext;
    private final List<PictureLib> mPictureLibList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView pictureLibImage;
        TextView pictureLibName;

        public ViewHolder(View view){
            super(view);
            cardView=(CardView)view;
            pictureLibImage=(ImageView)view.findViewById(R.id.first_image);
            pictureLibName=(TextView)view.findViewById(R.id.first_name);
        }
    }

    public PictureLibAdapter(List<PictureLib> PictureLibList){
        mPictureLibList=PictureLibList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.picture_lib,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PictureLib pictureLib=mPictureLibList.get(position);
        holder.pictureLibName.setText(pictureLib.getName());
        Glide.with(mContext).load(pictureLib.getImageId()).into(holder.pictureLibImage);
        ViewHolder vh=(ViewHolder)holder;
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=vh.getLayoutPosition();
                PictureLib picturelib=mPictureLibList.get(position);
                Intent intent=new Intent(v.getContext(), RealPicture.class);
                intent.putExtra("image",picturelib.getImageId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return mPictureLibList.size();
    }
}
