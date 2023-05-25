package com.soulvia.ideacreate;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.soulvia.ideacreate.entity.Note;

import java.util.ArrayList;
import java.util.List;

//create retrieve（读取） update delete
public class NoteDatabase {

    public static final String TABLE_NAME = "notes";
    public static final String CONTENT = "content";
    public static final String ID = "id"; //短横线代表为主键
    public static final String TIME = "time";
    public static final String TAG = "tag";
    public static final String USERNAME = "username";
    public String username;

    private static final String[] columns = {ID,CONTENT, TIME, TAG};
    private SQLiteDatabase notedb;
    private NotedbHelper notedbHelper;
    private final Context context;

    public NoteDatabase(Context _context,String username) {
        context = _context;
        this.username = username;
    }

    public NoteDatabase(Context _context) {
        context = _context;
    }

    /*
    打开数据库
     */
    public void open() throws SQLiteException{
        notedbHelper = new NotedbHelper(context);
        try {
            notedb = notedbHelper.getWritableDatabase();
        }catch (SQLiteException e){
            notedb = notedbHelper.getReadableDatabase();
        }
    }

    public SQLiteDatabase getNotedb(){return notedb;}
    /*
    关闭数据库
     */
    public void close() throws SQLiteException{
        notedb.close();
    }

    /*
        添加一条记录
         */
    @SuppressLint("Range")
    public int insert(Note note){
        ContentValues newNote = new ContentValues();
        newNote.put(CONTENT, note.getContent());
        newNote.put(TIME,note.getTime());
        newNote.put(TAG,note.getTag());
        newNote.put("username",username);
        notedb.insert(TABLE_NAME,null,newNote);
        Cursor cursor = notedb.query(TABLE_NAME, columns, null, null, null, null, ID +" DESC", "1");
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex(ID));
    }

    /*
    读取特定记录
     */
    public Note getOneNote(int id){
        Cursor results = notedb.query(TABLE_NAME,columns,ID + "=?",new String[]{String.valueOf(id)},null,null,null,null);
        if(results != null){
            results.moveToFirst();
        }else{
            return null;
        }
       @SuppressLint("Range") Note res = new Note(results.getInt(results.getColumnIndex(ID)),
                results.getString(results.getColumnIndex(CONTENT)),
                results.getString(results.getColumnIndex(TIME)),
                results.getString(results.getColumnIndex(TAG))
                );
        return res;
    }

    /*
    读取所有记录
     */
    @SuppressLint("Range")
    public List<Note> getAllNotes(int flag){
        Cursor results;
        if (flag==0){
            results = notedb.query(TABLE_NAME,columns,"username=?", new String[]{username},null,null,null);
        }else {
            results = notedb.query(TABLE_NAME,columns,"username=? and state=?", new String[]{username,"1"},null,null,null);
        }

        int resultConunts = results.getCount();
        if(resultConunts == 0 || !results.moveToFirst()){
            return null;
        }
        List<Note> notes = new ArrayList<>();
        for(int i=0;i<resultConunts;i++){
            Note note = new Note();
            note.setId(results.getInt(results.getColumnIndex(ID)));
            note.setContent(results.getString(results.getColumnIndex(CONTENT)));
            note.setTime(results.getString(results.getColumnIndex(TIME)));
            note.setTag(results.getString(results.getColumnIndex(TAG)));
            notes.add(note);
            results.moveToNext();
        }
        return notes;
    }

    /*
    更新某条记录
     */
    public int updateNotes(Note note){
        ContentValues values = new ContentValues();
        values.put(CONTENT,note.getContent());
        values.put(TIME,note.getTime());
        values.put(TAG,note.getTag());
        int a = notedb.update(TABLE_NAME,values,ID+"=?",new String[]{String.valueOf(note.getId())});
        return a;
    }
    /*
    删除某条记录
     */
    public int deleteNotes(Note note){
        notedb.delete("taglist",ID+"="+note.getId(),null);
        return notedb.delete(TABLE_NAME,ID+"="+note.getId(),null);
    }


    private static class NotedbHelper extends SQLiteOpenHelper{

        public NotedbHelper(Context context){
            super(context,"notes",null,1);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_NAME
                    + "("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + CONTENT + " TEXT,"
                    + USERNAME + " TEXT,"
                    + TIME + " TEXT NOT NULL,"
                    + TAG + " TEXT,"
                    + "state INTEGER DEFAULT 0)" //state代表是否传到云端，0，代表未上传，1，代表已上传
            );
            db.execSQL("CREATE TABLE IF NOT EXISTS taglist("+ ID + " INTEGER, "
                    +"tagid INTEGER NOT NULL, "
                    +"parenttag TEXT, "
                    + TAG + " TEXT, PRIMARY KEY("+ID+",tag,tagid));"
            );
            //创建删除tag的触发器
//            db.execSQL("CREATE TRIGGER del_tag AFTER DELETE ON " + TABLE_NAME
//                    + " BEGIN " + "DELETE FROM taglist WHERE taglist."+ ID
//                    + "=" + TABLE_NAME +"." + ID
//                    + ";END;"
//            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for(int i = oldVersion; i < newVersion; i++) {
                switch (i) {
                    case 1:
                        break;
                    case 2:
                        updateMode(db);
                    default:
                        break;
                }
            }
        }
        //更新数据库版本
        private void updateMode(SQLiteDatabase db){
            //version 1 -> 2, 增加 mode -- notes的分类，默认为1
            db.execSQL("alter table "+ TABLE_NAME + " add column " + TAG);
            Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
            while(cursor.moveToNext()){
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(CONTENT));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex(TIME));
                @SuppressLint("Range") String tag = cursor.getString(cursor.getColumnIndex(TAG));
                ContentValues values = new ContentValues();
                values.put(CONTENT, content);
                values.put(TIME, time);
                values.put(TAG,tag);
                db.update(TABLE_NAME, values, CONTENT +"=?", new String[]{content});
            }
            Log.d("Base", "update db 1 - 2");
        }
    }
}


