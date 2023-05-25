package com.soulvia.ideacreate.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.soulvia.ideacreate.R;
import com.soulvia.ideacreate.entity.Note;

import java.util.ArrayList;
import java.util.List;

/*
数据库中读取出来的note数据与listview的中介
 */
public class NoteAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private List<Note> backList;
    private List<Note> noteList;
    private MyFilter mFilter;

    public NoteAdapter(Context context, List<Note> notelist) {
        mContext = context;
        noteList = notelist;
        backList = notelist;
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int position) {
        return noteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mContext, R.layout.note_layout, null);
        Note note = noteList.get(position);
        TextView content = (TextView) view.findViewById(R.id.content);
        TextView time = (TextView) view.findViewById(R.id.time);

        content.setText(note.getContent());
        time.setText(note.getTime());
        return view;
    }

    class MyFilter extends Filter {
        //我们在performFiltering(CharSequence charSequence)这个方法中定义过滤规则
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Note> list;
            if (TextUtils.isEmpty(charSequence)) {//当过滤的关键字为空的时候，我们则显示所有的数据
                list = backList;
            } else {//否则把符合条件的数据对象添加到集合中
                list = new ArrayList<>();
                for (Note note : backList) {
                    if (note.getContent().contains(charSequence)) {
                        list.add(note);
                    }

                }
            }
            result.values = list; //将得到的集合保存到FilterResults的value变量中
            result.count = list.size();//将集合的大小保存到FilterResults的count变量中

            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            noteList = (List<Note>)results.values;
            if (results.count>0){
                notifyDataSetChanged();//通知数据发生了改变
            }else {
                notifyDataSetInvalidated();//通知数据失效
            }
        }
    }
}
