package top.xb.imgspace.ui.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.xb.imgspace.*;
import top.xb.imgspace.R;
import top.xb.imgspace.config.APIAddress;
import top.xb.imgspace.utils.AuthUtil;
import top.xb.imgspace.utils.DisplayUtil;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private NavController navController;
    private MainActivity mainActivity;
    private static final String TAG = "HomeFrag";
    public static File rootPath= Environment.getExternalStorageDirectory();
    private static File ROOTPATH= Environment.getExternalStorageDirectory();
    private GestureDetector detectior;
    EditText pathEdit;


    private GridView gv;
    private PicTask picTask;
    public static int picCol=3;

    private ListView lv;
    private ListTask listTask;
    public static int listEdge=75;

    private View picView;
    private View listView;
    private View homeProgress;
    private View main_head;
    View root;

    private IntentFilter intentFilter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=((MainActivity)getActivity());
        intentFilter=new IntentFilter();
        intentFilter.addAction("PicRefresh");
        intentFilter.addAction("ListRefresh");
        intentFilter.addAction("Refresh");
        mainActivity.registerReceiver(HomeReceiver,intentFilter);
    }

    private final BroadcastReceiver HomeReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("PicRefresh")){
                //Toast.makeText(mainActivity,"send ok!", Toast.LENGTH_SHORT).show();
                mainActivity.pic();
                pic();
                picTask = new PicTask(root,picCol);
                DisplayUtil.showProgress(mainActivity,homeProgress,main_head,true);
                picTask.execute();
            }else if(action.equals("ListRefresh")){
                mainActivity.list();
                list();
                listTask= new ListTask(root,listEdge);
                DisplayUtil.showProgress(mainActivity,homeProgress,main_head,true);
                listTask.execute();
            }else if(action.equals("Refresh")){
                if(MainActivity.mode== MainActivity.MODE.LIST){
                    mainActivity.list();
                    list();
                    listTask= new ListTask(root,listEdge);
                    DisplayUtil.showProgress(mainActivity,homeProgress,main_head,true);
                    listTask.execute();
                }else if(MainActivity.mode== MainActivity.MODE.PIC){
                    mainActivity.pic();
                    pic();
                    picTask = new PicTask(root,picCol);
                    DisplayUtil.showProgress(mainActivity,homeProgress,main_head,true);
                    picTask.execute();
                }
            }
        }
    };
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity.setNavText(getString(R.string.title_home));

        navController=Navigation.findNavController(mainActivity, R.id.nav_host_fragment);
        AuthUtil.verifyPermissions(mainActivity);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        pathEdit=root.findViewById(R.id.pathEdit);
        pathEdit.setText(rootPath.getPath());
        picView=root.findViewById(R.id.PicView);
        listView=root.findViewById(R.id.ListView);
        main_head=mainActivity.findViewById(R.id.main_head);
        homeProgress=root.findViewById(R.id.homeProgress);
        if(main_head!=null){
            Log.d(TAG,"得到main_head");
        }

        if(MainActivity.mode==MainActivity.MODE.LIST){
            mainActivity.list();
            list();
            MainActivity.mode = MainActivity.MODE.LIST;
            listTask= new ListTask(root,listEdge);
            DisplayUtil.showProgress(mainActivity,homeProgress,main_head,true);
            listTask.execute();
        }else{
            mainActivity.pic();
            pic();
            MainActivity.mode = MainActivity.MODE.PIC;
            picTask = new PicTask(root,picCol);
            DisplayUtil.showProgress(mainActivity,homeProgress,main_head,true);
            picTask.execute();
        }
        /*homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        //final TextView textView = root.findViewById(R.id.nav_text);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });*/
        return root;
    }

    public void list(){
        if(picView!=null)
            picView.setVisibility(View.INVISIBLE);
        if(listView!=null)
            listView.setVisibility(View.VISIBLE);
    }
    public void pic(){
        if(picView!=null)
            picView.setVisibility(View.VISIBLE);
        if(listView!=null)
            listView.setVisibility(View.INVISIBLE);
    }

    private List<Map<String,Object>> getPicData(int dstWidth,int dstHeight) throws FileNotFoundException {
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        @SuppressLint("SdCardPath") File folder= rootPath;
        //@SuppressLint("SdCardPath") File folder= rootPath ;
        File cachePath=new File(ROOTPATH+ APIAddress.LocalcachePath+rootPath);
        if(!cachePath.exists()&&!cachePath.mkdirs()){
            AuthUtil.verifyPermissions(mainActivity);
            ROOTPATH=Environment.getDataDirectory();
            cachePath=new File(ROOTPATH+ APIAddress.LocalcachePath+rootPath);
            if(!cachePath.exists()&&!cachePath.mkdirs())
                Toast.makeText(mainActivity,"缓存目录创建失败！",Toast.LENGTH_SHORT).show();
        }
        String[] all=folder.list();
        Map<String,Object> map;
        if(all!=null)
            for (String file : all) {
                file=file.toLowerCase();
                String scanPath = folder+"/"+file;
                Log.d(TAG,scanPath);
                if(new File(scanPath).isDirectory())
                    continue;
                if(!file.endsWith(".png")&&!file.endsWith(".jpg")&&!file.endsWith(".jpeg"))
                    continue;
                /*
                File cacheFile=new File(ROOTPATH+ APIAddress.LocalcachePath+scanPath);
                if(!cacheFile.exists()){
                    String cachefile=DisplayUtil.saveBitmap(mainActivity,rootPath.getPath(),cachePath.getPath(),file,100);
                    if(cachefile==null){
                        Toast.makeText(mainActivity,"缓存文件创建失败！",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mainActivity,"缓存文件创建成功！",Toast.LENGTH_SHORT).show();
                        Bitmap bmp=BitmapFactory.decodeFile(cachefile);
                        if (bmp != null) {
                            Log.d(TAG,"bmp:"+scanPath);
                            map=new HashMap<String,Object>();
                            map.put("name", file);
                            map.put("img", bmp);
                            list.add(map);
                        }
                    }
                }else{
                    Bitmap bmp=BitmapFactory.decodeFile(cacheFile.getPath());
                    if (bmp != null) {
                        Log.d(TAG,"bmp:"+scanPath);
                        map=new HashMap<String,Object>();
                        map.put("name", file);
                        map.put("img", bmp);
                        list.add(map);
                    }
                }*/
                BitmapFactory.Options op=new BitmapFactory.Options();
                //op.inJustDecodeBounds=true;
                op.inSampleSize=4;
                op.inJustDecodeBounds=false;
                Bitmap bmp=BitmapFactory.decodeFile(scanPath,op);
                if (bmp != null) {
                    Log.d(TAG,"bmp:"+scanPath);
                    int w=Math.min(bmp.getHeight(),bmp.getWidth());
                    int topx=(bmp.getWidth()-w)/2;
                    int topy=(bmp.getHeight()-w)/2;
                    Bitmap bmp0=Bitmap.createBitmap(bmp,topx,topy,w,w);
                    Bitmap bmp1=Bitmap.createScaledBitmap(bmp0,dstWidth,dstHeight,true);
                    bmp.recycle();
                    bmp0.recycle();
                    map=new HashMap<String,Object>();
                    map.put("name", file);
                    map.put("img", bmp1);
                    list.add(map);
                }
            }
        return list;
    }
    private List<Map<String,Object>> getListData(){
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        @SuppressLint("SdCardPath") File files=rootPath;
        String[] all=files.list();
        Map<String,Object> map;
        if(all!=null){
            for (String s : all) {
                s=s.toLowerCase();
                if(!s.endsWith(".png")&&!s.endsWith(".jpg")&&!s.endsWith(".jpeg"))
                    continue;
                String path = files.getPath() + "/" + s;
                Log.d(TAG,path);
                if(new File(path).isDirectory())
                    continue;
                /*Bitmap bmp = BitmapFactory.decodeFile(path);
                if (bmp != null) {
                    Log.d(TAG,"bmp:"+path);
                    map = new HashMap<String, Object>();
                    map.put("img", );
                    map.put("name", s);
                    list.add(map);
                }*/
                Log.d(TAG,"bmp:"+path);
                map = new HashMap<String, Object>();
                map.put("img", path);
                map.put("name", s);
                list.add(map);
            }
        }
        return list;
    }

    @SuppressLint("StaticFieldLeak")
    public class PicTask extends AsyncTask<Void, Void, Void> {
        private final View root;
        private final int picCol;
        private SimpleAdapter adp;
        List<Map<String,Object>> data;
        PicTask(View root,final int picCol) {
            this.root=root;
            this.picCol=picCol;
        }
        @Override
        protected Void doInBackground(Void... params) {
            gv=root.findViewById(R.id.PicView);
            gv.setNumColumns(picCol);

            data=getListData();
            /*try {
                data=getPicData(330,330);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
            adp = new SimpleAdapter(mainActivity,data,R.layout.homepicitem,
                    new String[]{"img"},
                    new int[]{R.id.homeimg});
            /*adp = new SimpleAdapter(mainActivity,data,R.layout.homepicitem,
                    new String[]{"name","img"},
                    new int[]{R.id.homename,R.id.homeimg});*/
            if(adp!=null)
                adp.setViewBinder(new SimpleAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Object data, String textRepresentation) {
                        int width=1000/picCol,height=1000/picCol;
                        if((view instanceof ImageView)&(data instanceof String)){
                            ImageView iv = (ImageView)view;
                            Glide.with(root.getContext()).load(data).override(width,height).centerCrop().into(iv);
                            /*Bitmap bmp = (Bitmap)data;
                            iv.setImageBitmap(bmp);*/
                            return true;
                        }
                        return false;
                    }
                });
                /*adp.setViewBinder(new SimpleAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Object data, String textRepresentation) {
                        if((view instanceof ImageView)&(data instanceof Bitmap)){
                            ImageView iv = (ImageView)view;
                            Bitmap bmp = (Bitmap)data;
                            iv.setImageBitmap(bmp);
                            return true;
                        }
                        return false;
                    }
                });*/
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            picTask = null;
            DisplayUtil.showProgress(mainActivity,homeProgress,main_head,false);

            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(mainActivity,"clicked"+rootPath+data.get(position).get("name"),Toast.LENGTH_SHORT).show();
                    ArrayList<String> path=new ArrayList<>();
                    for(Map<String,Object> d:data){
                        path.add((String) d.get("name"));
                    }
                    Intent intent=new Intent(root.getContext(),RealPicture.class);
                    intent.putExtra("position",position);
                    intent.putStringArrayListExtra("files",path);
                    intent.putExtra("path",rootPath.getPath()+"/");
                    root.getContext().startActivity(intent);
                }
            });
            gv.setAdapter(adp);
        }

        @Override
        protected void onCancelled() {
            picTask = null;
            DisplayUtil.showProgress(mainActivity,homeProgress,main_head,false);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ListTask extends AsyncTask<Void, Void, Void> {
        private final View root;
        private SimpleAdapter adp;
        private int Edge;
        List<Map<String,Object>> data;
        ListTask(View root,int Edge) {
            this.root=root;
            this.Edge=Edge;
        }
        @Override
        protected Void doInBackground(Void... params) {
            lv=root.findViewById(R.id.ListView);
            data=getListData();
            /*try {
                data=getPicData(Edge,Edge);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
            adp = new SimpleAdapter(mainActivity,data,R.layout.homelistitem,
                    new String[]{"img","name"},
                    new int[]{R.id.listimg,R.id.listname});
            if(adp!=null){
                adp.setViewBinder(new SimpleAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Object data, String textRepresentation) {
                        if((view instanceof ImageView)&(data instanceof String)){
                            ImageView iv = (ImageView)view;
                            Glide.with(root.getContext()).load(data).override(Edge,Edge).centerCrop().into(iv);
                            /*Bitmap bmp = (Bitmap)data;
                            iv.setImageBitmap(bmp);*/
                            return true;
                        }
                        return false;
                    }
                });
                /*adp.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if((view instanceof ImageView)&(data instanceof Bitmap)){
                        ImageView iv = (ImageView)view;
                        Bitmap bmp = (Bitmap)data;
                        iv.setImageBitmap(bmp);
                        return true;
                    }
                    return false;
                }
            });*/
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            listTask = null;
            DisplayUtil.showProgress(mainActivity,homeProgress,main_head,false);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(mainActivity,"clicked"+rootPath+data.get(position).get("name"),Toast.LENGTH_SHORT).show();
                    ArrayList<String> path=new ArrayList<>();
                    for(Map<String,Object> d:data){
                        path.add((String) d.get("name"));
                    }
                    Intent intent=new Intent(root.getContext(),RealPicture.class);
                    intent.putExtra("position",position);
                    intent.putStringArrayListExtra("files",path);
                    intent.putExtra("path",rootPath.getPath()+"/");
                    root.getContext().startActivity(intent);
                }
            });
            lv.setAdapter(adp);
        }

        @Override
        protected void onCancelled() {
            listTask = null;
            DisplayUtil.showProgress(mainActivity,homeProgress,main_head,false);
        }
    }

}





/*
        //实例化手势监听器对象参数（参数一：上下文，参数二:手势监听器对象）
        detectior = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            private static final String TAG = "";

            @Override
            public boolean onDown(MotionEvent motionEvent) {
                Log.i(TAG,"onDown");
                return false;
            }
            @Override
            public void onShowPress(MotionEvent motionEvent) {
                Log.i(TAG,"onShowPress");

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                Log.i(TAG,"onSingleTapUp");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                Log.i(TAG,"onScroll");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                Log.i(TAG,"onLongPress");

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i(TAG,"onFing");
                //手势往左滑动20个像素，图片切换到下一张
                if(e2.getX()<e1.getX()-20){
                }
                //手势往右滑动20个像素，图像切换到上一张
                if(e2.getX()>e1.getX()+20){

                }

                //根据变化的之后的图片更新的背景图片
                //root.setBackgroundResource(imgIds[imgIndex]);
                return false;
            }
        });*/