package com.soulvia.ideacreate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.soulvia.ideacreate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    private EditText userNameEdit;
    private EditText emailEdit;
    private EditText pwd1Edit;
    private EditText pwd2Edit;
    private TextView hints;
    private EditText code;
    private Button sendCode;
    private String userName;
    private String email;
    private String pwd1;
    private String pwd2;
    private Button registerBtn;
    private Handler handler = new Handler();
    private String specifyCode = "";

    private String url = "http://172.20.10.6:8000/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerBtn = findViewById(R.id.register_button);
        userNameEdit = findViewById(R.id.userName);
        emailEdit = findViewById(R.id.email);
        pwd1Edit = findViewById(R.id.pwd1);
        pwd2Edit = findViewById(R.id.pwd2);
        hints = findViewById(R.id.hints1);
        code = findViewById(R.id.code);
        sendCode = findViewById(R.id.send_code);

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEdit.getText().toString();
                if(! email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")){
                    hints.setText("请输入正确的邮箱");
                    return;
                }
                new Thread(() -> {
                    try {
                        URL httpUrl = new URL("http://172.20.10.6:8000/sendcode");
                        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                        conn.setReadTimeout(5000);
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        OutputStream out = conn.getOutputStream();
                        String content="email="+email;
                        out.write(content.getBytes());
                        out.flush();
                        out.close();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        reader.close();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this,"发送验证码成功！",Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = userNameEdit.getText().toString();
                email = emailEdit.getText().toString();
                pwd1 = pwd1Edit.getText().toString();
                pwd2 = pwd2Edit.getText().toString();
                specifyCode = code.getText().toString();

                if(!pwd1.equals(pwd2)){
                    hints.setText("两次输入密码不同");
                    return;
                }
                if(! email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")){
                    hints.setText("请输入正确的邮箱");
                    return;
                }
                if(specifyCode.equals("")){
                    hints.setText("请输入验证码");
                    return;
                }
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
                        String content="email="+email+"&password="+pwd1+"&userName="+userName+"&code="+specifyCode;
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
                                    switch (code) {
                                        case "200":
                                            Intent goToLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(goToLogin);
                                            break;
                                        case "401":
                                            hints.setText("该账号已注册过!");
                                            break;
                                        case "403":
                                            hints.setText("验证码错误！");
                                            break;
                                        default:
                                            hints.setText("注册出错！");
                                            break;
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
    }
}