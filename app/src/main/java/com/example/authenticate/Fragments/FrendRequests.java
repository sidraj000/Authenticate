package com.example.authenticate.Fragments;


import android.content.Context;
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

import com.example.authenticate.Class.FriendRequest;
import com.example.authenticate.Class.MyFriends;
import com.example.authenticate.Class.User;
import com.example.authenticate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class FrendRequests extends Fragment {

    private RecyclerView mRecycler;
    private FriendRequestAdapter mAdapter;
    private LinearLayoutManager mManager;
    private DatabaseReference mFriendsReference;
    private FirebaseUser user;



    public FrendRequests() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_frend_requests, container, false);
        mRecycler=view.findViewById(R.id.friendReqList);
        user=FirebaseAuth.getInstance().getCurrentUser();
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("friendRequest").child(user.getUid());
        mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(mManager);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        mAdapter = new FriendRequestAdapter(getContext(), mFriendsReference);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.cleanupListener();
    }

    public   class FriendRequestViewHolder extends RecyclerView.ViewHolder {

        public TextView fName;
        public ImageView fPic,fAdd;
        public TextView fEmail;
        public ImageView fCancel;
        public FriendRequestViewHolder( View itemView) {
            super(itemView);
            fName=itemView.findViewById(R.id.freqName);
            fPic=itemView.findViewById(R.id.freqPic);
            fAdd=itemView.findViewById(R.id.freqAdd);
            fEmail=itemView.findViewById(R.id.freqEmail);
            fCancel=itemView.findViewById(R.id.freqCancel);
        }
    }
    private  class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestViewHolder>
    {


        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> frId = new ArrayList<>();
        private List<FriendRequest> fr = new ArrayList<>();

        private FriendRequestAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;


                // Create child event listener
                // [START child_event_listener_recycler]
                ChildEventListener childEventListener = new ChildEventListener() {
                   @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                        //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                        // A new comment has been added, add it to the displayed list

                        FriendRequest fReq = dataSnapshot.getValue(FriendRequest.class);

                        // [START_EXCLUDE]
                        // Update RecyclerView
                        frId.add(dataSnapshot.getKey());
                        fr.add(fReq);
                        notifyItemInserted(fr.size() - 1);
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                        Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                        // A comment has changed, use the key to determine if we are displaying this
                        // comment and if so displayed the changed comment.
                        FriendRequest fReq = dataSnapshot.getValue(FriendRequest.class);
                        String userKey = dataSnapshot.getKey();

                        // [START_EXCLUDE]
                        int reqIndex = frId.indexOf(userKey);
                        if (reqIndex > -1) {
                            // Replace with the new data
                            fr.set(reqIndex, fReq);

                            // Update the RecyclerView
                            notifyItemChanged(reqIndex);
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
                        int fIndex = frId.indexOf(userKey);
                        if (fIndex > -1) {
                            // Remove data from the list
                            frId.remove(fIndex);
                            fr.remove(fIndex);

                            // Update the RecyclerView
                            notifyItemRemoved(fIndex);
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
        public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_friendrequest, viewGroup, false);
            return new FriendRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final FriendRequestViewHolder friendViewHolder, int i) {

            final FriendRequest freq=fr.get(i);
            friendViewHolder.fName.setText(freq.uName);
            friendViewHolder.fEmail.setText(freq.uEmail);
            friendViewHolder.fAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend(freq);
                    delete(freq);
                    Toast.makeText(mContext, "userFragment Added", Toast.LENGTH_SHORT).show();
                }
            });
            friendViewHolder.fCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete(freq);
                    Toast.makeText(mContext, "userFragment Request Cancelled", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return fr.size();
        }
        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
    public void addFriend(FriendRequest fr)
    {

        String uId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        MyFriends fList=new MyFriends(fr.uName,fr.uEmail,fr.uId,fr.uPId);
        MyFriends fListMe=new MyFriends(user.getDisplayName(),user.getEmail(),user.getUid(),user.getPhotoUrl().toString());
        mDatabase.child("friend").child(uId).child(fr.uId).setValue(fList);
        mDatabase.child("friend").child(fr.uId).child(uId).setValue(fListMe);
    }
    public void delete(FriendRequest fr)
    {
        DatabaseReference mDatabaseRef;
        mDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("friendRequest").child(user.getUid()).child(fr.uId);
        mDatabaseRef.removeValue();
    }


}
