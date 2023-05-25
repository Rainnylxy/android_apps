package com.soulvia.ideacreate.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.soulvia.ideacreate.R;
import com.soulvia.ideacreate.entity.TreeNode;

import java.util.List;

public class TreeViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<TreeNode> mfilelist;
    private Bitmap mIconCollapse;   //- ， 收缩
    private Bitmap mIconExpand;  	//+ ，展开
    private Context context;

    public TreeViewAdapter(Context context, List<TreeNode> treeNodes) {
        super();
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mfilelist = treeNodes;
        mIconCollapse = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_add_24);
        mIconExpand = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_minimize_24);
    }

    public int getCount() {
        return mfilelist.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        convertView = mInflater.inflate(R.layout.tree_node, null);
        holder = new ViewHolder();
        holder.text = (TextView) convertView.findViewById(R.id.treetext);
        holder.icon = (ImageButton) convertView.findViewById(R.id.icon);
        convertView.setTag(holder);

        final TreeNode obj = mfilelist.get(position);

        int level = obj.getTagid();
        if(level!=0){
            holder.text.setPadding(	50 * level,
                    holder.text.getPaddingTop(), 0,
                    holder.text.getPaddingBottom());
        }
        holder.text.setText(obj.getTag());
        if (obj.isHasChildren()&& (obj.isExpanded() == false)) {
           holder.icon.setBackground(context.getResources().getDrawable(R.drawable.ic_baseline_add_24,null));
        } else if (obj.isHasChildren() && (obj.isExpanded() == true)) {
           holder.icon.setBackground(context.getResources().getDrawable(R.drawable.ic_baseline_minimize_24,null));
        } else if (!obj.isHasChildren()) {
            holder.icon.setBackground(context.getResources().getDrawable(R.drawable.ic_baseline_add_24,null));
            holder.icon.setVisibility(View.INVISIBLE);
        }

        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemIconListner.onIconClick(position);
            }
        });
        return convertView;
    }

    public interface onItemIconListner{
        void onIconClick(int i);
    }

    private onItemIconListner mOnItemIconListner;

    public void setmOnItemIconListner(onItemIconListner mOnItemIconListner){
        this.mOnItemIconListner = mOnItemIconListner;
    }


    class ViewHolder {
        TextView text;
        ImageButton icon;
    }
}
