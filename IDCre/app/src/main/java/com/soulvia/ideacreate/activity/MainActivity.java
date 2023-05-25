package com.soulvia.ideacreate.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IInterface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.soulvia.ideacreate.entity.FormalizeTag;
import com.soulvia.ideacreate.entity.Note;
import com.soulvia.ideacreate.adapter.NoteAdapter;
import com.soulvia.ideacreate.NoteDatabase;
import com.soulvia.ideacreate.R;
import com.soulvia.ideacreate.entity.TreeNode;
import com.soulvia.ideacreate.adapter.TreeViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private FloatingActionButton btn;
    private FloatingActionButton pyqBtn;
    private ListView listView;
    private NoteDatabase noteDatabase;
    private NoteAdapter noteAdapter;
    private static List<Note> nteList = new ArrayList<>();
    private Toolbar myToolbar;
    private FormalizeTag formalizeTag;
    private SQLiteDatabase database;

    //弹出菜单
    private PopupWindow popupWindow;
    //覆盖层
    private PopupWindow popupCover;
    private ViewGroup customView;
    private ViewGroup coverView;
    private LayoutInflater layoutInflater;
    private RelativeLayout main;
    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;
    private TextView goToCan;
    private String username;

    //弹出窗口
    private ListView popListView;

    //左侧菜单栏
    private TextView myFriends;
    private TextView allIdeas;
    private TextView lese;

    //初始化侧边弹出框的部分信息（布局）

    public void initPopupView(){
        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = (ViewGroup) layoutInflater.inflate(R.layout.setting_layout, null);
        coverView = (ViewGroup) layoutInflater.inflate(R.layout.setting_cover_layout,null);
        main = findViewById(R.id.main_layout);
        goToCan = customView.findViewById(R.id.goToCan);
        myFriends = customView.findViewById(R.id.my_friends);
        allIdeas = customView.findViewById(R.id.all_ideas);
        windowManager = getWindowManager();
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    }
    //具体设置侧边弹出框，宽度，高度等并展示出
    @SuppressLint("Range")
    public void showPopupView(){
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        //初始化popupWindow, popupCover
        popupWindow = new PopupWindow(customView,(int)(width*0.7),height,true);
        popupCover = new PopupWindow(coverView,width,height,false);
        //给popupWindow, popupCover设置背景颜色
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupCover.setBackgroundDrawable(new ColorDrawable(Color.GRAY));

        popListView = customView.findViewById(R.id.lv_tag);
        noteDatabase.open();
        database = noteDatabase.getNotedb();
        String[] columns = {"tag","parenttag","tagid"};

        //执行一个非重复查询，得到所有tag与parenttag，并根据此构造tag树
        Cursor allTagsCursor = database.query(true,"taglist",columns,null,null,null,null,null,null);

        List<TreeNode> allTags = new ArrayList<TreeNode>();
        while(allTagsCursor.moveToNext()){
            allTags.add(new TreeNode(allTagsCursor.getString(allTagsCursor.getColumnIndex("tag")),
                    allTagsCursor.getString(allTagsCursor.getColumnIndex("parenttag")),
                    allTagsCursor.getInt(allTagsCursor.getColumnIndex("tagid"))
                    ));
        }
        List<TreeNode> changeTags = buildTags(allTags);
        TreeViewAdapter treeViewAdapter = new TreeViewAdapter(getApplicationContext(),changeTags);

        //在主界面加载成功后，显示弹出框
        findViewById(R.id.main_layout).post(new Runnable() {
            @Override
            public void run() {
                popupCover.showAtLocation(main,Gravity.NO_GRAVITY,0,0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY,0,0);
                popListView.setAdapter(treeViewAdapter);
            }
        });
        //设置弹出菜单消失后，覆盖层也消失
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupCover.dismiss();
            }
        });
        //设置poplistview中子控件（button）的点击事件
        treeViewAdapter.setmOnItemIconListner(new TreeViewAdapter.onItemIconListner(){
            @Override
            public void onIconClick(int position) {
                if(! changeTags.get(position).isHasChildren()){return;}
                // 假如 此父节点下有子节点，且 已展开  关闭的时候删除
                if (changeTags.get(position).isExpanded()) {
                    changeTags.get(position).setExpanded(false);
                    TreeNode element = changeTags.get(position); // 先得到该 父节点

                    ArrayList<TreeNode> temp = new ArrayList<TreeNode>();
                    // 循环得到 子节点
                    for (int i = position + 1; i < changeTags.size(); i++) {
                        if (element.getTagid() >= changeTags.get(i).getTagid()) {
                            break;
                        }
                        temp.add(changeTags.get(i));
                    }

                    changeTags.removeAll(temp);
                    treeViewAdapter.notifyDataSetChanged();
                } else {
                    // 假如此父节点未展开，则展开的时候增加
                    TreeNode obj = changeTags.get(position);
                    obj.setExpanded(true);
//                    int level = obj.getTagid(); // 父节点级数
//                    int nextLevel = level + 1; // 子节点级数 = 父节点级数+1
                    int temp = position;//临时位置
                    // 循环 得到 此父节点的所有子节点
                    for (TreeNode element : obj.getChildren()) {
                        //element.setLevel(nextLevel);
                        element.setExpanded(false);
                        temp = temp+1;
                        changeTags.add(temp, element);
                    }
                    // 将已变化的nodes列表刷新
                    treeViewAdapter.notifyDataSetChanged();
                }
            }

        });

        //设置poplistview自身item的点击事件，找到对应笔记并在主页展示出来
        popListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int depth = changeTags.get(position).getTagid();
                String noteTag="";
                if(changeTags.get(position).getTagid() == 0){
                    noteTag += changeTags.get(position).getTag();
                }else{
                    for(int i=1;i>0;i++){
                        if(changeTags.get(position-i).getTagid() == changeTags.get(position).getTagid()){continue;}
                        noteTag = changeTags.get(position-i).getTag() + "/"+noteTag;
                        if(changeTags.get(position-i).getTagid() == 0){break;}
                    }
                }
                String t = "[\\s\\S]*#("+noteTag+"/{0,1}[^#]*)#[\\s\\S]*";
                Cursor allNotes = database.rawQuery("SELECT id FROM notes WHERE content REGEXP ? ",new String[]{t});
                Log.d("onItemClick", " "+changeTags.get(position).getTagid());
                allNotes.moveToFirst();
                if(nteList.size()>0){nteList.clear();}
                nteList.add(noteDatabase.getOneNote(allNotes.getInt(allNotes.getColumnIndex("id"))));
                while (allNotes.moveToNext()){
                    nteList.add(noteDatabase.getOneNote(allNotes.getInt(allNotes.getColumnIndex("id"))));
                }
                noteAdapter = new NoteAdapter(getApplicationContext(),nteList);
                listView.setAdapter(noteAdapter);
                popupWindow.dismiss();
            }
        });

        goToCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goTocan = new Intent(MainActivity.this, CalendarActivity.class);
                goTocan.putExtra("username",username);
                startActivity(goTocan);
            }
        });

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMyfriends = new Intent(MainActivity.this,MyFriendActivity.class);
                goToMyfriends.putExtra("username",username);
                startActivity(goToMyfriends);
            }
        });

        allIdeas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });


    }

  static List<TreeNode> buildTags(List<TreeNode> treeNodes){
        List<TreeNode> trees = new ArrayList<TreeNode>();

        for (TreeNode treeNode : treeNodes) {
            if (treeNode.getParenttag() == null) {
                trees.add(treeNode);
            }
            for (TreeNode it : treeNodes) {
                String a=it.getParenttag();
                String b=treeNode.getTag();
                if (a!=null && a.equals(b)) {
                    if (!treeNode.isHasChildren()) {
                        treeNode.setChildren(new ArrayList<TreeNode>());
                    }
                    treeNode.hasChildren(true);
                    treeNode.getChildren().add(it);
                }
            }
        }
        return trees;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (FloatingActionButton) findViewById(R.id.fab);
        pyqBtn = findViewById(R.id.penyouquan);
        listView = (ListView)findViewById(R.id.list_view);
        myToolbar = findViewById(R.id.myToolbar);


        //popListView = (ListView) LayoutInflater.from(MainActivity.this).inflate(R.layout.setting_layout,null).findViewById(R.id.lv_tag);

        Intent getintent = getIntent();
        username = getintent.getStringExtra("username");
        noteDatabase = new NoteDatabase(this,username);


        //初始化主页面
        noteDatabase.open();
        nteList = noteDatabase.getAllNotes(0);
        noteDatabase.close();
        initPopupView();
        refreshListView();

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar取代actionbar
        myToolbar.setNavigationIcon(R.drawable.ic_menu_dehaze_24);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupView();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode",4);
                startActivityForResult(intent,0);
            }
        });

        pyqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PyqActivity.class);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(this);
    }

    public void onStart() {
        super.onStart();
    }

    //接收startActivityForResult的结果，并对返回的笔记等进行处理
    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        String content = data.getStringExtra("content");
        String time = data.getStringExtra("time");
        String tag = data.getStringExtra("tag");
        int id = data.getIntExtra("id",0);
        int mode = data.getIntExtra("mode",-1);


        /*
        mode :
            2: 需要删除该note
            3: 已有note被修改，更新数据库
            4; 创建新的note，插入数据库
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
        }else if(mode == 4){ //插入新的note
            note.setId(noteDatabase.insert(note));
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
        nteList = noteDatabase.getAllNotes(0);
        noteDatabase.close();
    }

    public void onResume() {
        super.onResume();
        refreshListView();
    }

    public void refreshListView(){
        if(nteList!=null && nteList.size()>0) {
            nteList.clear();
        }else{
            listView.setAdapter(null);
            return;
        }
        noteDatabase.open();
        List<Note> note = noteDatabase.getAllNotes(0);
        if(note != null){
            nteList.addAll(note);
        }
        noteAdapter = new NoteAdapter(getApplicationContext(),nteList);
        noteDatabase.close();
        noteAdapter.notifyDataSetChanged();
        listView.setAdapter(noteAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        switch (parent.getId()) {
//            case R.id.list_view:
//                Note curNote = (Note) parent.getItemAtPosition(position);
//                Intent intent = new Intent(MainActivity.this, EditActivity.class);
//                intent.putExtra("content", curNote.getContent());
//                intent.putExtra("id", curNote.getId());
//                intent.putExtra("time", curNote.getTime());
//                intent.putExtra("mode", 3);     // MODE of 'click to edit'
//                intent.putExtra("tag", curNote.getTag());
//                Log.e("intent",curNote.getContent());
//                startActivityForResult(intent, 1);      //collect data from edit
//               // overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
//                break;
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        //search setting
        MenuItem mSearch = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
//            case R.id.menu_search:
//                if(nteList.size()>0) nteList.clear();
//                noteDatabase.open();
//                List<Note> note = noteDatabase.getAllNotes(0);
//                if(note != null){
//                    nteList.addAll(note);
//                }
//                noteAdapter = new NoteAdapter(getApplicationContext(),nteList);
//                noteDatabase.close();
//                noteAdapter.notifyDataSetChanged();
//                listView.setAdapter(noteAdapter);
//                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}