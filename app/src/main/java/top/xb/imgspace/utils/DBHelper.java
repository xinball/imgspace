package top.xb.imgspace.utils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import top.xb.imgspace.bean.Picture;


public class DBHelper extends SQLiteOpenHelper {
    //数据库版本号
    private static final int DATABASE_VERSION=4;

    //数据库名称
    private static final String DATABASE_NAME="picture.db";

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据表
        String CREATE_TABLE_PICTURE="CREATE TABLE Picture ("+
                "pid Integer PRIMARY KEY AUTOINCREMENT ,"+
                "name String,"+
                "path String,"+
                "created Date)";
        db.execSQL(CREATE_TABLE_PICTURE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //如果旧表存在，删除，所以数据将会消失
        db.execSQL("DROP TABLE IF EXISTS Picture");
        //再次创建表
        onCreate(db);
    }
}