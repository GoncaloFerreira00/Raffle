package com.example.company;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyBD extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "info.db";
    public static final String TABLE_NAME = "info";
    public static final String COLUMN1_NAME = "id";
    public static final String COLUMN2_NAME = "name";
    public static final String COLUMN3_NAME = "admin";

    public Context ctx;

    public MyBD(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{

            String sql = "CREATE TABLE Info ( ";
            sql += COLUMN1_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
            sql += COLUMN2_NAME + " VARCHAR (120) NOT NULL,";
            sql += COLUMN3_NAME + " INTEGER NOT NULL);";

            db.execSQL(sql);

        }catch (Exception e){
            Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean createUser(String name, int admin_){
        try{

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            int id = 1;
            cv.put(COLUMN1_NAME, id);
            cv.put(COLUMN2_NAME, name);
            cv.put(COLUMN3_NAME, admin_);
            long a = db.insert("Info",null, cv);
            return (a>0)?true:false;

        }catch (Exception e){
            Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public int getUserId(int value){
        String result = null;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT id FROM Info", null);
        if(c.moveToNext())
        {
            result = c.getString(0);
            value = Integer.parseInt(result);
            return value;
        }
        return value;
    }

    public String getUserName(String value){
        String result = null;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM Info", null);
        if(c.moveToNext())
        {
            result = c.getString(0);
            value = result;
            return value;
        }
        return value;
    }

    public int getAdmin(int value){
        String result = null;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT admin FROM Info", null);
        if(c.moveToNext())
        {
            result = c.getString(0);
            value = Integer.parseInt(result);
            return value;
        }
        return value;
    }

}
