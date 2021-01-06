package top.xb.imgspace.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;

import java.util.Date;
import java.util.List;

import top.xb.imgspace.R;
import top.xb.imgspace.RealPicture;
import top.xb.imgspace.application.ImgSpaceApplication;
import top.xb.imgspace.bean.Message;
import top.xb.imgspace.bean.Photo;
import top.xb.imgspace.utils.DisplayUtil;
import top.xb.imgspace.utils.HttpUtil;

public class MyListAdapter extends BaseAdapter {
    private List<Message> messages=null;
    private LayoutInflater inflater;
    private final Context context;
    private List<List<Photo>> photoslist=null;

    DeletemsgTask deletemsgTask;
    EditmsgTask editmsgTask;

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
        holder.userspace_time.setText("发布:"+DisplayUtil.timeFormat.format(messages.get(position).sendTime));
        Date alterTime=messages.get(position).alterTime;
        if(alterTime!=null)
            holder.userspace_altertime.setText("修改:"+DisplayUtil.AllFormat.format(alterTime));
        else
            holder.userspace_altertime.setText("");
        holder.userspace_content.setText(messages.get(position).content);
        if(holder.userspace_photogrid!=null){
            gridAdapter=new MyGridAdapter(photoslist.get(position),context,position);
            holder.userspace_photogrid.setAdapter(gridAdapter);
        }
        holder.userspace_content.setOnLongClickListener(new View.OnLongClickListener() {
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
        holder.userspace_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,"content clicked"+position,Toast.LENGTH_SHORT).show();
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
