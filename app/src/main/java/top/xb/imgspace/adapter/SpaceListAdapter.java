package top.xb.imgspace.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Date;
import java.util.List;

import top.xb.imgspace.Other_user_Activity;
import top.xb.imgspace.R;
import top.xb.imgspace.UserActivity;
import top.xb.imgspace.UserspaceActivity;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class SpaceListAdapter extends BaseAdapter {
    private List<Message> messages=null;
    private LayoutInflater inflater;
    private final Context context;
    private List<List<Photo>> photoslist=null;
    List<String> names=null;
    List<String> avatars=null;

    DeletemsgTask deletemsgTask;
    EditmsgTask editmsgTask;

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
        if(messages!=null)
            return messages.size();
        else
            return 0;
    }

    @Override
    public Message getItem(int position) {
        if(messages!=null)
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
        String avatarFile=messages.get(position).uid+avatars.get(position);
        Log.i(TAG,"avatarFile"+avatarFile);
        String avatarPath=APIAddress.WEB_IMG_URL+avatarFile;
        Log.i(TAG,"avatarPath"+avatarPath);
        File avatarcache=new File(Environment.getExternalStorageDirectory()+APIAddress.WebcachePath+avatarFile);
        Log.i(TAG,"avatarcache"+avatarcache);
        if(avatarcache.exists())
            Glide.with(context).load(avatarcache.getPath()).override(40,40).into(holder.space_avatar);
        else
            Glide.with(context).load(avatarPath).override(40,40).into(holder.space_avatar);
        if(holder.space_photogrid!=null){
            gridAdapter=new MyGridAdapter(photoslist.get(position),context,position);
            holder.space_photogrid.setAdapter(gridAdapter);
        }
        holder.space_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, UserspaceActivity.class);
                intent.putExtra("uid",messages.get(position).uid);
                context.startActivity(intent);
            }
        });
        holder.space_avatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String uid=messages.get(position).uid;
                if(uid.equals(ImgSpaceApplication.getUid())){
                    Intent intent=new Intent(context, UserActivity.class);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }else{
                    Intent intent=new Intent(context, Other_user_Activity.class);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        holder.space_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,"content clicked"+position,Toast.LENGTH_SHORT).show();
            }
        });

        holder.space_content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //显示对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final AlertDialog dialog = builder.create();
                View dia = View.inflate(context, R.layout.dialog_content, null);
                dialog.setView(dia);

                TextView content_copy = (TextView) dia.findViewById(R.id.content_copy);
                TextView content_edit = (TextView) dia.findViewById(R.id.content_edit);
                TextView content_delete = (TextView) dia.findViewById(R.id.content_delete);
                content_copy.setOnClickListener(new View.OnClickListener() {//
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboardManager=(ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData=ClipData.newPlainText("Label",messages.get(position).content);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(context,"复制文本成功！",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                if(messages.get(position).uid.equals(ImgSpaceApplication.getUid())){
                    content_edit.setOnClickListener(new View.OnClickListener() {//
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            final AlertDialog dialog = builder.create();
                            View dia = View.inflate(context, R.layout.dialog_edit, null);
                            dialog.setView(dia);
                            EditText edit_content = (EditText) dia.findViewById(R.id.edit_content);
                            edit_content.setText(messages.get(position).content);
                            TextView edit_sure = (TextView) dia.findViewById(R.id.edit_sure);
                            edit_sure.setOnClickListener(new View.OnClickListener() {//
                                @Override
                                public void onClick(View v) {
                                    String content=edit_content.getText().toString().trim();
                                    Message message=messages.get(position);
                                    editmsgTask=new EditmsgTask(message.uid,ImgSpaceApplication.getPwd(),message.mid,content);
                                    editmsgTask.execute();
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });
                    content_delete.setOnClickListener(new View.OnClickListener() {//
                        @Override
                        public void onClick(View v) {
                            Message message=messages.get(position);
                            deletemsgTask=new DeletemsgTask(message.uid,ImgSpaceApplication.getPwd(),message.mid);
                            deletemsgTask.execute();
                            dialog.dismiss();
                        }
                    });
                }else{
                    content_delete.setVisibility(View.INVISIBLE);
                    content_edit.setVisibility(View.INVISIBLE);
                }
                dialog.show();
                return false;
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


    @SuppressLint("StaticFieldLeak")
    public class DeletemsgTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String pwd="";
        private String mid="";

        DeletemsgTask(String uid, String pwd ,String mid) {
            this.uid=uid;
            this.pwd=pwd;
            this.mid=mid;
            SendData="{\"action\":\"send\",\"method\":\"deletemsg\",\"uid\":\""+uid+"\",\"pwd\":\""+pwd+"\",\"mid\":\""+mid+"\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(context, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            deletemsgTask = null;
            //DisplayUtil.showProgress(context,deleteProgress,null,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        Toast.makeText(context,"消息删除成功！", Toast.LENGTH_SHORT).show();
                        //发送广播
                        Intent intent = new Intent();
                        intent.setAction("SpaceRefresh");
                        context.sendBroadcast(intent);
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(context,returnmsg,true,"删除消息失败","确定",null);
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                //Snackbar.make(real_bottomtitle, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            deletemsgTask = null;
            //DisplayUtil.showProgress(context,null,null,false);
        }
    }


    @SuppressLint("StaticFieldLeak")
    public class EditmsgTask extends AsyncTask<Void, Void, JSONObject> {

        private String SendData = "";
        private String uid="";
        private String pwd="";
        private String mid="";
        private String message="";

        EditmsgTask(String uid, String pwd ,String mid,String message) {
            this.uid=uid;
            this.pwd=pwd;
            this.mid=mid;
            this.message=message;
            SendData="{\"action\":\"send\",\"method\":\"altermsg\",\"uid\":\""+uid+"\",\"pwd\":\""+pwd+"\",\"mid\":\""+mid+"\",\"message\":\""+message+"\"}";
        }
        @Override
        protected JSONObject doInBackground(Void... params) {
            return HttpUtil.postRequest(context, SendData,null);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            editmsgTask = null;
            //DisplayUtil.showProgress(context,deleteProgress,null,false);
            if(result !=null) {
                try {
                    Log.v("JSON", result.toString());
                    if (result.getIntValue("return") == 1) {
                        Toast.makeText(context,"消息内容修改成功！", Toast.LENGTH_SHORT).show();
                        //发送广播
                        Intent intent = new Intent();
                        intent.setAction("SpaceRefresh");
                        context.sendBroadcast(intent);
                    } else {
                        String returnmsg=result.getString("returnmsg");
                        DisplayUtil.dialogProcess(context,returnmsg,true,"消息内容修改失败","确定",null);
                        //Toast.makeText(RegActivity.this, result.getString("returnmsg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                //Snackbar.make(real_bottomtitle, R.string.service_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        @Override
        protected void onCancelled() {
            editmsgTask = null;
            //DisplayUtil.showProgress(context,null,null,false);
        }
    }

}
