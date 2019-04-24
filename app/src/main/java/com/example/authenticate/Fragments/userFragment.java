package com.example.authenticate.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.authenticate.Class.Comment;
import com.example.authenticate.Class.FriendRequest;
import com.example.authenticate.Class.User;
import com.example.authenticate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class userFragment extends Fragment {
    private RecyclerView mRecycler;
    private FriendAdapter mAdapter;
    private LinearLayoutManager mManager;
    private DatabaseReference mFriendsReference;
    FirebaseUser muser;


    public userFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_friend, container, false);
        mRecycler=view.findViewById(R.id.frendList);
        mFriendsReference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(mManager);
        muser=FirebaseAuth.getInstance().getCurrentUser();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter = new FriendAdapter(getContext(), mFriendsReference);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.cleanupListener();
    }

    private static class FriendViewHolder extends RecyclerView.ViewHolder {

        public TextView fName;
        public ImageView fPic,fAdd,sendMsg;
        public FriendViewHolder( View itemView) {
            super(itemView);
            fName=itemView.findViewById(R.id.friendName);
            fPic=itemView.findViewById(R.id.friendPic);
            fAdd=itemView.findViewById(R.id.friendAdd);
            sendMsg=itemView.findViewById(R.id.sendMsg);
        }
    }
    private  class FriendAdapter extends RecyclerView.Adapter<FriendViewHolder>
    {



        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mUserIds = new ArrayList<>();
        private List<User> mUser = new ArrayList<>();

        public FriendAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    User user = dataSnapshot.getValue(User.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mUserIds.add(dataSnapshot.getKey());
                    mUser.add(user);
                    notifyItemInserted(mUser.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    User user = dataSnapshot.getValue(User.class);
                    String userKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int userIndex = mUserIds.indexOf(userKey);
                    if (userIndex > -1) {
                        // Replace with the new data
                        mUser.set(userIndex, user);

                        // Update the RecyclerView
                        notifyItemChanged(userIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + userKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String userKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int userIndex = mUserIds.indexOf(userKey);
                    if (userIndex> -1) {
                        // Remove data from the list
                        mUserIds.remove(userIndex);
                        mUser.remove(userIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(userIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + userKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_friend, viewGroup, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final FriendViewHolder friendViewHolder, int i) {

            final User user=mUser.get(i);
            friendViewHolder.fName.setText(user.username);
             new DownLoadImageTask(friendViewHolder.fPic).execute(user.picId);
             friendViewHolder.fAdd.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     sendFriendrequest(user);
                     friendViewHolder.fAdd.setImageResource(R.drawable.done);
                     Toast.makeText(mContext, "userFragment Request Send", Toast.LENGTH_SHORT).show();
                 }
             });
             friendViewHolder.sendMsg.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     sendMsg(user);

                 }
             });


        }

        @Override
        public int getItemCount() {
            return mUser.size();
        }
        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }





    private static class DownLoadImageTask extends AsyncTask<String,Void, Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlOfImage = strings[0];
            Bitmap logo = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            }
            catch(IOException e)
            {
            }
            return logo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    public  void sendFriendrequest(User user)
    {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FriendRequest fList=new FriendRequest(muser.getDisplayName(),muser.getPhotoUrl().toString(),muser.getEmail(),muser.getUid());
        mDatabase.child("friendRequest").child(user.uid).child(muser.getUid()).setValue(fList);

    }
    public void sendMsg(User user)
    {
       Intent intent=new Intent(getActivity(),MessageFragment.class);
       intent.putExtra(MessageFragment.key,user.uid);
       startActivity(intent);

    }



}
