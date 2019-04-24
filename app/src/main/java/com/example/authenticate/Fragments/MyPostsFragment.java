package com.example.authenticate.Fragments;

import com.example.authenticate.Fragments.PostListFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyPostsFragment extends PostListFragment {

    public MyPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("userFragment-posts")
                .child(getUid());
    }
}