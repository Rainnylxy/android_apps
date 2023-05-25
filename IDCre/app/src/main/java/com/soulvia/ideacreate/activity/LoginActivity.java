package com.soulvia.ideacreate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.soulvia.ideacreate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private Button login;
    private Button register;
    private EditText emailAddress;
    private EditText passWord;
    private String email;
    private String password;
    private TextView hints;

    private Handler handler = new Handler();
    private String url = "http://172.20.10.6:8000/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        hints = findViewById(R.id.hints);
        emailAddress = findViewById(R.id.emailAddress);
        passWord = findViewById(R.id.password);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailAddress.getText().toString();
                password = passWord.getText().toString();

                new Thread(() -> {
                    try {
                        URL httpUrl = new URL(url);
                        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                        conn.setReadTimeout(5000);
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        OutputStream out = conn.getOutputStream();
                        String content="email="+email+"&password="+password;
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
                                    String username = resultJson.get("username").toString();
                                    if(code.equals("200")){
                                        Intent goToMain = new Intent(LoginActivity.this,MainActivity.class);
                                        goToMain.putExtra("username",username);
                                        startActivity(goToMain);
                                    }else if(code.equals("401")){
                                        hints.setText("该用户不存在!");
                                    }else if(code.equals("402")){
                                        hints.setText("密码输入错误！");
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
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(goToRegister);
            }
        });
    }
}