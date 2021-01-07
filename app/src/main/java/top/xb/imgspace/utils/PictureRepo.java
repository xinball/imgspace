package top.xb.imgspace.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import top.xb.imgspace.bean.Picture;

public class PictureRepo {
    private DBHelper dbHelper;

    public PictureRepo(Context context){
        dbHelper=new DBHelper(context);
    }

    public int insert(Picture picture){
        //打开连接，写入数据
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("name",picture.name);
        values.put("path",picture.path);
        values.put("created",DisplayUtil.dateFormat.format(picture.created));
        //
        long pid=db.insert("pid",null,values);
        db.close();
        return (int)pid;
    }

    public void delete(int pid){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.delete("Picture","pid=?", new String[]{String.valueOf(pid)});
        db.close();
    }
    public void update(Picture picture){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put("name",picture.name);
        values.put("path",picture.path);
        values.put("created",DisplayUtil.dateFormat.format(picture.created));

        db.update("Picture",values,"pid=?",new String[] { String.valueOf(picture.pid) });
        db.close();
    }

    public ArrayList<HashMap<String, String>> getPictureList(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                "pid,"+
                "name,"+
                "path,"+
                "created"+
                "FROM Picture";
        ArrayList<HashMap<String,String>> pictureList=new ArrayList<HashMap<String, String>>();
        Cursor cursor=db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> picture=new HashMap<String,String>();
                picture.put("id",cursor.getString(cursor.getColumnIndex("pid")));
                picture.put("name",cursor.getString(cursor.getColumnIndex("name")));
                picture.put("path",cursor.getString(cursor.getColumnIndex("path")));
                picture.put("created",cursor.getString(cursor.getColumnIndex("created")));
                pictureList.add(picture);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return pictureList;
    }

    public Picture getStudentById(int Id) throws ParseException {
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                "pid," +
                "name," +
                "path," +
                "created" +
                " FROM Picture"
                + " WHERE pid=?";
        int iCount=0;
        Picture picture=new Picture();
        Cursor cursor=db.rawQuery(selectQuery,new String[]{String.valueOf(Id)});
        if(cursor.moveToFirst()){
            do{
                picture.pid =cursor.getInt(cursor.getColumnIndex("pid"));
                picture.name =cursor.getString(cursor.getColumnIndex("name"));
                picture.path  =cursor.getString(cursor.getColumnIndex("path"));
                picture.created =DisplayUtil.dateFormat.parse(cursor.getString(cursor.getColumnIndex("created")));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return picture;
    }
}