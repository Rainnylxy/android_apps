package com.soulvia.ideacreate.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soulvia.ideacreate.R;
import com.soulvia.ideacreate.activity.MyFriendActivity;

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
import java.util.List;

public class FriendAdapter extends BaseAdapter {
    private Context context;
    private List<String> friends;
    private String username;
    private int flag;


    //0:代表需要同意 1：代表已经是好友
    public FriendAdapter(Context context,List<String> friends,int flag,String username){
        this.context = context;
        this.friends = friends;
        this.flag = flag;
        this.username = username;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("WrongConstant")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.friend,null);
        String str = friends.get(position);
        TextView textView = view.findViewById(R.id.textView2);
        ImageView imageView = view.findViewById(R.id.imageView4);
        Button btn = view.findViewById(R.id.agree);
        Button disBtn = view.findViewById(R.id.disagree);
        imageView.setImageResource(R.drawable.tou);
        if(flag == 0){
            btn.setVisibility(0);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(()->{
                        try {
                            URL httpUrl = new URL("http://172.20.10.6:8000/agreefriendapply");
                            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                            conn.setReadTimeout(5000);
                            conn.setConnectTimeout(5000);
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            OutputStream out = conn.getOutputStream();
                            String content="name="+friends.get(position)+"&username="+username;
                            System.out.println(friends.get(position));
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
                    }).start();
                }
            });


            disBtn.setVisibility(0);
            disBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("disBtn");
                    new Thread(()->{
                        try {
                            URL httpUrl = new URL("http://172.20.10.6:8000/disagreefriendapply");
                            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                            conn.setReadTimeout(5000);
                            conn.setConnectTimeout(5000);
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            OutputStream out = conn.getOutputStream();
                            String content="name="+friends.get(position)+"&username="+username;
                            System.out.println(friends.get(position));
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
                    }).start();
                }
            });
        }
        textView.setText(str);
        return view;
    }
}
