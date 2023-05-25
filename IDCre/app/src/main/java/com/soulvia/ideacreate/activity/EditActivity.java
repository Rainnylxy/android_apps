package com.soulvia.ideacreate.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.soulvia.ideacreate.NoteDatabase;
import com.soulvia.ideacreate.R;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditActivity extends AppCompatActivity {

    EditText edit;
    ImageView upload_photo;
    ImageView save_button;
    ImageView tag_button;
    ImageView b_button;
    ImageView i_button;
    LinearLayout linear_text;
    LinearLayout image_layout;
    int b_index=0;
    int nb_index=0;
    private String old_content = "";
    private String old_time;
    private int old_Tag = 1;
    private long id = 0;
    private int openMode = 0;
    Intent intent = new Intent();
    String fontStyle;
    private Toolbar myToolbar;
    private boolean isBold=false;
    private boolean isBoldClick=false;
    private boolean isItalic=false;
    private boolean isItalicClick=false;
    private Uri originalUri;
    private Bitmap originalBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        edit = findViewById(R.id.edit);
        upload_photo = findViewById(R.id.upload_photo);
        save_button = findViewById(R.id.save_button);
        tag_button = findViewById(R.id.tag_button);
        b_button = findViewById(R.id.b_button);
        i_button = findViewById(R.id.italic_button);
        linear_text = findViewById(R.id.linear_text);
        myToolbar = findViewById(R.id.mytoolbar);
        image_layout = findViewById(R.id.image_layout);
        Intent getIntent = getIntent();

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar取代actionbar
        myToolbar.setNavigationOnClickListener(new back_listener());


        openMode = getIntent.getIntExtra("mode", 0);
        if (openMode == 3) {   //打开已存在的note，对已存在的mode进行修改
            id = getIntent.getIntExtra("id", 0);
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");
            old_Tag = getIntent.getIntExtra("tag", 1);
            edit.setText(old_content);
            edit.setSelection(old_content.length());//设置光标位置到最后
            //mySpinner.setSelection(old_Tag - 1);
        }
        save_button.setOnClickListener(new back_listener());
        tag_button.setOnClickListener(new tag_listener());
        b_button.setOnClickListener(new boldListener());
        i_button.setOnClickListener(new italicListener());
        edit.addTextChangedListener(new editTextChangedListener());
        upload_photo.setOnClickListener(new upload_photo_listener());
    }

    private class upload_photo_listener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent getImage = new Intent();
            getImage.setType("image/*");
            getImage.setAction(Intent.ACTION_GET_CONTENT);
            getImage.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(getImage,1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);
        ContentResolver resolver = getContentResolver();
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                originalUri = intent.getData();
                try {
                    originalBitmap = BitmapFactory.decodeStream(resolver
                            .openInputStream(originalUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (originalBitmap != null) {
                    insertImage(originalBitmap);
                } else {
                    Toast.makeText(EditActivity.this, "获取图片失败",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void insertImage(Bitmap bitmap) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        imageView.setImageBitmap(bitmap);
        image_layout.addView(imageView);
    }


    //返回主页面listener，可以是点击save返回，也可以是点击左上角返回
    private class back_listener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            autoSetIntent(openMode);
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    //设置tag的监听器，为文本添加tag
    private class tag_listener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            edit.getText().insert(edit.length(),"##");
            edit.setSelection(edit.getText().length()-1);
        }
    }


    private class editTextChangedListener implements TextWatcher{
        private int charCount;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(charCount != edit.length()){
                charCount = edit.length();
                //定义SpannableString，它主要的用途就是可以改变editText,TextView中部分文字的格式，以及向其中插入图片等功能
                SpannableString ss = new SpannableString(s);
                if(isBold) {
                    for (int i = start; i < start + count; i++) {
                        ss.setSpan(new StyleSpan(Typeface.BOLD), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if(isItalic){
                    for (int i = start; i < start + count; i++) {
                        ss.setSpan(new StyleSpan(Typeface.ITALIC), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                edit.setText(ss);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            edit.setSelection(s.length());
        }
    }

    private class boldListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(isBoldClick){
                isBold = false;
            }else {
                isBold = true;
            }
            isBoldClick = !isBoldClick;
        }
    }

    public class italicListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if(isItalicClick){
                isItalic = false;
            }else {
                isItalic = true;
            }
            isItalicClick = !isItalicClick;
            Log.e("isItalic", "onClick: "+isItalic);
        }
    }
//    按下返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event){

//        如果按下home键
        if(keyCode == KeyEvent.KEYCODE_HOME){
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
//            返回键，直接存储数据
//            但是应该是提示是否保存输入的内容
            autoSetIntent(openMode);
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    /*
    openMode: 4 代表新建一个note
              3 代表修改已有的note
     */
    public void autoSetIntent(int openMode){
        switch (openMode){
            case 4:
                if(edit.getText().toString().length() == 0){
                    intent.putExtra("mode",-1);//什么都没写，啥也不做
                }else{
                    intent.putExtra("content",edit.getText().toString());
                    intent.putExtra("time",dateToStr());
                    intent.putExtra("tag","tag0");
                    intent.putExtra("mode",4);
                }
                break;
            case 3:
                if(edit.getText().toString().equals(old_content)){
                    intent.putExtra("mode",-1);//什么都没写，啥也不做
                }else{
                    intent.putExtra("content",edit.getText().toString());
                    intent.putExtra("time",old_time);
                    intent.putExtra("tag","tag0");
                    intent.putExtra("mode",3);
                    intent.putExtra("id",(int)id);
                }
                break;
            default:
                break;
        }
    }
    public String dateToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.edit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
//        switch (menuItem.getItemId()){
//            case R.id.menu_delete:
//                new AlertDialog.Builder(EditActivity.this)
//                        .setMessage("确定要删除嘛")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if(openMode == 4){
//                                    intent.putExtra("mode",-1);
//                                }else if(openMode == 3){
//                                    intent.putExtra("mode",2);
//                                    intent.putExtra("id",(int)id);
//                                }
//                                setResult(RESULT_OK,intent);
//                                finish();
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).create().show();
//                break;
//            case R.id.note_upload:
//                NoteDatabase noteDatabase = new NoteDatabase(this);
//                noteDatabase.open();
//                SQLiteDatabase sqLiteDatabase = noteDatabase.getNotedb();
//                String sql="UPDATE notes SET state=1 WHERE id="+id;
//                sqLiteDatabase.execSQL(sql);
//                Toast.makeText(this,"发布到好友圈成功！",Toast.LENGTH_SHORT).show();
//                noteDatabase.close();
//        }
        return super.onOptionsItemSelected(menuItem);
}
    }