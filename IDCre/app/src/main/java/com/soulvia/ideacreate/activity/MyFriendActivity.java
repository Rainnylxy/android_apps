package com.soulvia.ideacreate.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.soulvia.ideacreate.R;
import com.soulvia.ideacreate.adapter.FriendAdapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MyFriendActivity extends AppCompatActivity {
    private ListView friendListView;
    private ListView applyFriListView;
    private Handler handler = new Handler();
    private String[] friends = new String[1024];
    private String[] friends1 = new String[1024];
    private String username;
    private List<String> friendList = new ArrayList<>();
    private List<String> friendList1 = new ArrayList<>();
    private ImageView returnBtn;
    private ImageView addBtn;
    private EditText editText;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friend);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        friendListView = (ListView) findViewById(R.id.friends);
        applyFriListView = findViewById(R.id.apply_friends);
        returnBtn = findViewById(R.id.return_btn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        addBtn = findViewById(R.id.add_friend);
        addBtn.setOnClickListener(new addClickListener());
        refreshFriendList();
    }

    private class addClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyFriendActivity.this);
            builder.setTitle("请输入电子邮箱");
            builder.setIcon(R.drawable.friends);
            editText = new EditText(MyFriendActivity.this);
            builder.setView(editText);
            builder.setPositiveButton("确认添加", (DialogInterface.OnClickListener) new addFriendListener());
            builder.setNegativeButton("取消",null);
            builder.show();
        }
    }

    private class addFriendListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            email = editText.getText().toString();
            if(editText.getText().toString()==null){
                Toast.makeText(MyFriendActivity.this,"请输入正确的用户名",Toast.LENGTH_SHORT).show();
                return;
            }
            new Thread(()->{
                try {
                    URL httpUrl = new URL("http://172.20.10.6:8000/addfriends");
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    OutputStream out = conn.getOutputStream();
                    String content="email="+email+"&username="+username;
                    out.write(content.getBytes());
                    out.flush();
                    out.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    final StringBuffer buffer = new StringBuffer();
                    String str=null;
                    while((str = reader.readLine())!=null){
                        buffer.append(str);
                    }
                    reader.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject resultJson = new JSONObject(String.valueOf(buffer));
                                String code = resultJson.get("code").toString();
                                if(code.equals("200")){
                                    Toast.makeText(MyFriendActivity.this,"发送添加申请成功！",Toast.LENGTH_SHORT).show();
                                }else if(code.equals("401")){
                                    Toast.makeText(MyFriendActivity.this,"该用户不存在！",Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void refreshFriendList(){
        new Thread(() -> {
            try {
                URL httpUrl = new URL("http://172.20.10.6:8000/getfriends");
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                OutputStream out = conn.getOutputStream();
                String content="username="+username;
                out.write(content.getBytes());
                out.flush();
                out.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                final StringBuffer buffer = new StringBuffer();
                String str=null;
                while((str = reader.readLine())!=null){
                    buffer.append(str);
                }
                reader.close();
                JSONObject resultJson = new JSONObject(String.valueOf(buffer));
                String code = resultJson.get("code").toString();
                switch (code){
                    case "200":
                        JSONArray jsonArray = resultJson.getJSONArray("msg");
                        for(int i=0; i<jsonArray.length();i++){
                            friends[i]=jsonArray.getString(i);
                        }
                        break;
                    case "401":
                        break;
                }
            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    friendList.clear();
                    for(int i=0;i<friends.length;i++){
                        if(friends[i] == null)break;
                        friendList.add(friends[i]);
                    }
                    FriendAdapter friendAdapter = new FriendAdapter(getApplicationContext(),friendList,1,username);
                    friendListView.setAdapter(friendAdapter);
                }
            });
        }).start();

        new Thread(() -> {
            try {
                URL httpUrl = new URL("http://172.20.10.6:8000/getfriendsapply");
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                OutputStream out = conn.getOutputStream();
                String content="username="+username;
                out.write(content.getBytes());
                out.flush();
                out.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                final StringBuffer buffer = new StringBuffer();
                String str=null;
                while((str = reader.readLine())!=null){
                    buffer.append(str);
                }
                reader.close();
                JSONObject resultJson = new JSONObject(String.valueOf(buffer));
                String code = resultJson.get("code").toString();
                switch (code){
                    case "200":
                        JSONArray jsonArray = resultJson.getJSONArray("msg");
                        for(int i=0; i<jsonArray.length();i++){
                            friends1[i]=jsonArray.getString(i);
                            System.out.println("aaa "+friends1[i]);
                        }
                        break;
                    case "401":
                        break;
                }
            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    friendList1.clear();
                    for(int i=0;i<friends1.length;i++){
                        if(friends1[i] == null)break;
                        friendList1.add(friends1[i]);
                        System.out.println("hhh "+friendList1.get(i));
                    }
                    FriendAdapter friendAdapter = new FriendAdapter(getApplicationContext(),friendList1,0,username);
                    applyFriListView.setAdapter(friendAdapter);
                }
            });
        }).start();
    }
}