package com.soulvia.ideacreate.entity;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormalizeTag {
    private String content;

    public FormalizeTag(String content){this.content = content;}
    //取出多个标签
    public List<String> getAllTags(){
        List<String> list = new ArrayList<>();
        Pattern regex  = Pattern.compile("#([^#]*)#");
        Matcher matcher = regex.matcher(content);
        while (matcher.find()){
            Log.d("matcher", "getAllTags: "+matcher.group(1));
            list.add(matcher.group(1));
        }
        return list;
    }
    //把标签处理成一级标签、二级标签等等
    public List<String> splitTags(String longTag){
        List<String> list = new ArrayList<>();
        String[] res = longTag.split("/");
        list = Arrays.asList(res);
        return list;
    }

    public void setContent(String content){this.content = content;}
}
