package com.example.authenticate.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.authenticate.Class.MessageClass;
import com.example.authenticate.Class.User;
import com.example.authenticate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    private RecyclerView mRecycler;
    private MessageAdapter mAdapter;
    private LinearLayoutManager mManager;
    private DatabaseReference mMessageReference;
    private FirebaseUser user;
    public static String key;
    private TextView tvMsg;
    private Button btnSend;


    public MessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_message, container, false);
        mRecycler=view.findViewById(R.id.msglist);
        user= FirebaseAuth.getInstance().getCurrentUser();
        mMessageReference = FirebaseDatabase.getInstance().getReference().child("Message").child(user.getUid()).child(key);
        mManager = new LinearLayoutManager(getContext());
        tvMsg=view.findViewById(R.id.tvMsg);
        btnSend=view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMsg(key);
            }
        });
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(mManager);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter = new MessageAdapter(getContext(), mMessageReference);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.cleanupListener();
    }

    public   class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView msgBody;
        public TextView msgTitle;

        public MessageViewHolder(View itemView) {
            super(itemView);
           msgBody=itemView.findViewById(R.id.msgBody);
           msgTitle=itemView.findViewById(R.id.msgTitle);
        }
    }
    private  class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder>
    {


        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> msgId = new ArrayList<>();
        private List<MessageClass> msg = new ArrayList<>();

        private MessageAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;


            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list

                    MessageClass ms = dataSnapshot.getValue(MessageClass.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    msgId.add(dataSnapshot.getKey());
                    msg.add(ms);
                    notifyItemInserted(msg.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_message, viewGroup, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MessageViewHolder MessageViewHolder, int i) {

            final MessageClass ms=msg.get(i);
            MessageViewHolder.msgTitle.setText(ms.SName);
            MessageViewHolder.msgBody.setText(ms.Message);

        }

        @Override
        public int getItemCount() {
            return msg.size();
        }
        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
    public void addMsg(String key)
    {
        final DatabaseReference mData= FirebaseDatabase.getInstance().getReference();
        Query connectedUser = mData.equalTo(key);
        connectedUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                User us;
                String text= (String) tvMsg.getText();

                final DatabaseReference mDatabase=FirebaseDatabase.getInstance().getReference();
               us=dataSnapshot.getValue(User.class);
                MessageClass msg=new MessageClass(text,user.getDisplayName());
                MessageClass nmsg=new MessageClass(text,us.username);
                mDatabase.child("Messages").child(user.getUid()).child(us.uid).setValue(msg);
                mDatabase.child("Messages").child(us.uid).child(user.getUid()).setValue(nmsg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(getActivity(), "Cant send message", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
