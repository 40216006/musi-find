package com.example.colin.musifind;

/**
 * Created by Colin on 27/02/2018.
 */

public class cards {
    private String userId;
    private String name;
    public cards (String userId, String name){
        this.userId = userId;
        this.name = name;

    }
    public String getUserid(){
        return userId;

    }
    public void setUserId(String userId){
        this.userId = userId;

    }
    public String getName(){
        return name;

    }
    public void setName(String name){
        this.name = name;

    }
}
