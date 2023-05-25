package com.soulvia.ideacreate.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.soulvia.ideacreate.R;

import java.util.List;

public class testAdapter extends BaseAdapter {
    private Context context;
    private List<String> friends;
    public testAdapter(Context context,List<String> friends){
        this.context = context;
        this.friends = friends;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(context, R.layout.friend,null);
        String str = friends.get(position);
        TextView textView = view.findViewById(R.id.textView2);

        return view;
    }
}
