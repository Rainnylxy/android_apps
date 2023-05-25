package com.soulvia.ideacreate.entity;

public class Note {
    private int id;
    private String content;
    private String time;
    private String tag;

    public Note(){}

    public Note(String Content, String Time, String Tag){
        content = Content;
        time = Time;
        tag = Tag;
    }

    public Note(int Id, String Content, String Time, String Tag){
        id = Id;
        content = Content;
        time = Time;
        tag = Tag;
    }

    public void setId(int Id){id = Id;}
    public void setContent(String Content){content = Content;}
    public void setTime(String Time){time = Time;}
    public void setTag(String Tag){tag = Tag;}

    public int getId(){return id;}
    public String getContent(){return content;}
    public String getTime(){return time;}
    public String getTag(){return tag;}
}
