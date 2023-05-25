package com.soulvia.ideacreate.activity;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.soulvia.ideacreate.NoteDatabase;
import com.soulvia.ideacreate.R;
import com.soulvia.ideacreate.entity.Note;

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
import java.util.Collection;
import java.util.List;
import java.util.zip.Inflater;

public class PyqActivity extends AppCompatActivity {
    private NoteDatabase noteDatabase;
    private String[] friends = new String[1024];
    private String username;
    private List<Note> noteList = new ArrayList<>();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.soulvia.ideacreate.R.layout.activity_pyq);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        noteDatabase = new NoteDatabase(this,username);
        setPyq();
    }

    public void setPyq(){
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
                    noteDatabase = new NoteDatabase(PyqActivity.this,username);
                    System.out.println("user"+username);
                    noteDatabase.open();
                    noteList.addAll(noteDatabase.getAllNotes(1));
                    for(int i=0;i<friends.length;i++){
                        if(friends[i] == null){break;}
                        noteDatabase = new NoteDatabase(PyqActivity.this,friends[i]);
                        System.out.println(friends[i]);
                        noteDatabase.open();
                        if(noteDatabase.getAllNotes(1) == null){continue;}
                        noteList.addAll(noteDatabase.getAllNotes(1));
                    }
                    for(int i=0;i<noteList.size()&i<3;i++){
                        TextView temp;
                        switch (i){
                            case 0:
                                temp = findViewById(R.id.text1);
                                break;
                            case 1:
                                temp = findViewById(R.id.text2);
                                break;
                            case 2:
                                temp = findViewById(R.id.text3);
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + i);
                        }
                        temp.setText(noteList.get(i).getContent().toString());
                    }
                }
            });
        }).start();
    }

}