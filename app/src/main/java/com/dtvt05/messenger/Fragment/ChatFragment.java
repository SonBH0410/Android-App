package com.dtvt05.messenger.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dtvt05.messenger.Adapter.ListChatAdapter;
import com.dtvt05.messenger.Interface.ItemClickListener;
import com.dtvt05.messenger.Model.Chats;
import com.dtvt05.messenger.Model.ListChat;
import com.dtvt05.messenger.Profile;
import com.dtvt05.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment  {
    private RecyclerView recyclerView;
    private DatabaseReference dataRef;
    private FirebaseUser currentUser;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        recyclerView = view.findViewById(R.id.chat_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        final String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        dataRef = FirebaseDatabase.getInstance().getReference("ListChat4Recycleview");
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ListChat> listChats = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equals(currentUserID)){
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            ListChat listChat = snapshot1.getValue(ListChat.class);
                            listChats.add(listChat);
                        }
                    }
                }
                ListChatAdapter listChatAdapter = new ListChatAdapter(listChats,getContext());
                recyclerView.setAdapter(listChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //lấy dữ liệu chats của người dùng về
        return view;
    }

}
