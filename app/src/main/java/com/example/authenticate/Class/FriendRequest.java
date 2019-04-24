package com.example.authenticate.Class;

public class FriendRequest {
    public String uName;
    public String uPId;
    public String uEmail;
    public String uId;
    public FriendRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public FriendRequest(String uName, String uPId, String uEmail,String uId) {
        this.uName = uName;
        this.uPId = uPId;
        this.uEmail = uEmail;
        this.uId=uId;
    }
}
