package com.soulvia.ideacreate.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;

import com.soulvia.ideacreate.entity.FormalizeTag;
import com.soulvia.ideacreate.entity.Note;
import com.soulvia.ideacreate.adapter.NoteAdapter;
import com.soulvia.ideacreate.NoteDatabase;
import com.soulvia.ideacreate.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private List<Note> noteList = new ArrayList<Note>();
    private NoteDatabase noteDatabase;
    private NoteAdapter noteAdapter;
    private SQLiteDatabase database;
    private ListView canNotesList;
    private FormalizeTag formalizeTag;
    private CalendarView calendarView;
    private int year;
    private int month;
    private int dayOfMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        canNotesList = findViewById(R.id.canList);
        Intent intent = getIntent();
        noteDatabase = new NoteDatabase(this,intent.getStringExtra("username"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());					//放入Date类型数据
        refreshListView(
            calendar.get(Calendar.YEAR),					//获取年份
            calendar.get(Calendar.MONTH),					//获取月份
            calendar.get(Calendar.DATE));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @SuppressLint("Range")
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int Year, int Month, int DayOfMonth) {
                year = Year;
                month = Month;
                dayOfMonth = DayOfMonth;
                refreshListView(year,month,dayOfMonth);
            }
        });
        canNotesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note curNote = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(CalendarActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);     // MODE of 'click to edit'
                intent.putExtra("tag", curNote.getTag());
                Log.e("intent",curNote.getContent());
                startActivityForResult(intent, 1);
            }
        });
    }

    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String content = data.getStringExtra("content");
        String time = data.getStringExtra("time");
        String tag = data.getStringExtra("tag");
        int id = data.getIntExtra("id",0);
        int mode = data.getIntExtra("mode",-1);

        /*
        mode :
            2: 需要删除该note
            3: 已有note被修改，更新数据库
         */
        noteDatabase.open();
        database = noteDatabase.getNotedb();
        Note note = new Note(content,time,tag);
        if(mode == 3){  //已有的note被改变，操作数据库更新，并且需要更新taglist表
            note.setId(id);
            noteDatabase.updateNotes(note);
            formalizeTag = new FormalizeTag(content);
            List<String> tags = formalizeTag.getAllTags();
            database.execSQL("DELETE FROM taglist WHERE id="+note.getId()+";");
            for(int i=0;i<tags.size();i++){
                List<String> splitTags = formalizeTag.splitTags(tags.get(i));
                Log.d("splitTags", "onActivityResult: "+splitTags.get(0));
                database.execSQL("INSERT OR IGNORE INTO taglist(tag,id,tagid)VALUES(\"" + splitTags.get(0)+"\","+note.getId()+",0);");
                for(int j=1;j<splitTags.size();j++){
                    Log.d("splitTags", "onActivityResult: "+splitTags.get(j-1));
                    database.execSQL("INSERT OR IGNORE INTO taglist(tag,id,tagid,parenttag)VALUES(\"" + splitTags.get(j)+"\","+note.getId()+","+j+",\""+splitTags.get(j-1)+"\");");
                }
            }
        }else if(mode == 2){
            note.setId(id);
            noteDatabase.deleteNotes(note);
        }
        noteList = noteDatabase.getAllNotes(0);
        noteDatabase.close();
    }

    public void onResume(){
        super.onResume();
        if(year !=0){
            refreshListView(year,month,dayOfMonth);
        }
    }

    @SuppressLint("Range")
    public void refreshListView(int year,int month,int dayOfMonth){
        String time=""+year;
        if(month<10){
            time = time +"-0"+ (month+1)+"-";
        }else {
            time = time +"-"+ (month+1)+"-";
        }
        if(dayOfMonth <10) {
            time = time+"0"+dayOfMonth;
        }else{
            time = time + dayOfMonth;
        }
        time = "^"+time+".*";
        Log.d("TAG", "refreshListView: "+time);
        noteDatabase.open();
        database = noteDatabase.getNotedb();
        Cursor res = database.rawQuery("SELECT * FROM notes WHERE time REGEXP '"+time+"'",null);
        if(noteList.size()>0){noteList.clear();}
        while (res.moveToNext()){
            noteList.add(noteDatabase.getOneNote(res.getInt(res.getColumnIndex("id"))));
        }
        noteAdapter = new NoteAdapter(getApplicationContext(), noteList);
        canNotesList.setAdapter(noteAdapter);
        noteDatabase.close();
    }
}