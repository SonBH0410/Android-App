package com.dtvt05.messenger.Adapter;

//Cần adapter để lấy dữ liệu hiển thị lên recycle view

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dtvt05.messenger.ChatActivity;
import com.dtvt05.messenger.Interface.ItemClickListener;
import com.dtvt05.messenger.Model.ListChat;
import com.dtvt05.messenger.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListChatAdapter extends RecyclerView.Adapter<ListChatAdapter.MyViewHolder>  {

    private ArrayList<ListChat> ListChat;
    private Context context;
    private ArrayList<ListChat> mListChat;

    public ArrayList<ListChat> sortList(ArrayList<ListChat> listChat){
        ListChat temp = null;
        for(int i = 0;i < ListChat.size() - 1; i++){
            for(int j = i + 1;j < ListChat.size(); j++){
                if(listChat.get(i).getTime() > listChat.get(j).getTime()){
                    temp = listChat.get(i);
                    listChat.set(i,listChat.get(j));
                    listChat.set(j,temp);
                }
            }
        }
        return listChat;
    }

    public ListChatAdapter(ArrayList<ListChat> listChat,Context context){
        this.ListChat = listChat;
        this.context = context;
        this.mListChat = sortList(listChat);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_chat,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.DisplayName.setText(mListChat.get(position).getDisplayName());
        holder.LastMessage.setText(mListChat.get(position).getLastMessage());
        //load avatar
        final String avatar = mListChat.get(position).getAvatar();
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("avatar").child(avatar + ".png");
        final long ONE_MEGABYTE = 1024 * 1024;
        mStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.Avatar.setImageBitmap(bitmap);
            }
        });
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if(isLongClick)
                    Toast.makeText(context, "Long Click: "+mListChat.get(position), Toast.LENGTH_SHORT).show();
                else {
                    //Đóng gói dữ liệu vào bundle để chuyển sang activity mới, lấy Avatar vì Avatar lưu ID;
                    Intent intent = new Intent(context,ChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("chatUserID",mListChat.get(position).getAvatar());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mListChat.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        TextView DisplayName,LastMessage;
        CircleImageView Avatar;
        ImageView Status;
        ItemClickListener itemClickListener;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            DisplayName = (TextView) itemView.findViewById(R.id.tv_item_name);
            LastMessage = (TextView) itemView.findViewById(R.id.tv_item_last_mess);
            Avatar = (CircleImageView) itemView.findViewById(R.id.civ_item_avatar);
            Status = (ImageView) itemView.findViewById(R.id.online_icon);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener)
        {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),false);
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),true);
            return true;
        }
    }
}
