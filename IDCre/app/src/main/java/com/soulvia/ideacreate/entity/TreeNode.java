package com.soulvia.ideacreate.entity;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String tag;
    private boolean hasChildren;
    private int tagid;
    private String parenttag;
    private List<TreeNode> children = new ArrayList<TreeNode>();
    private boolean expanded;

    public void addChild(TreeNode node){
        this.hasChildren = true;
        this.children.add(node);
    }

    public TreeNode(String tag,String parenttag,int tagid){this.tag=tag;this.parenttag = parenttag;this.tagid = tagid;}

    public String getTag(){return tag;}

    public String getParenttag(){return parenttag;}

    public void setTag(String tag){this.tag = tag;}

    public int getTagid(){return tagid;}

    public void setTagid(int level){this.tagid = level;}

    public void setExpanded(boolean expanded){this.expanded = expanded;}

    public boolean isExpanded(){return expanded;}

    public boolean isHasChildren(){return hasChildren;}

    public void hasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public void setHasChildren(boolean hasChildren){this.hasChildren = hasChildren;}

    public List<TreeNode> getChildren(){return children;}

    public void setChildren(List<TreeNode> children){
        this.hasChildren = true;
        this.children = children;
    }

}
