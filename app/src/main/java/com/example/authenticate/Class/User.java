package com.example.authenticate.Class;


import android.net.Uri;

public class User {

    public String username;
    public String email;
    public String picId;
    public String uid;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email,String picId,String uid) {
        this.username = username;
        this.email = email;
        this.picId=picId;
        this.uid=uid;


    }

}