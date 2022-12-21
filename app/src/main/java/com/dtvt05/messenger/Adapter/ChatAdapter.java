package com.dtvt05.messenger.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dtvt05.messenger.AES;
import com.dtvt05.messenger.ChatActivity;
import com.dtvt05.messenger.Interface.ItemClickListener;
import com.dtvt05.messenger.Login;
import com.dtvt05.messenger.Model.Chats;
import com.dtvt05.messenger.Model.ListChat;
import com.dtvt05.messenger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{
    private ArrayList<Chats> chat;
    private Context context;
    FirebaseAuth firebaseAuth;

    public ChatAdapter(ArrayList<Chats> chat, Context context) {
        this.chat = chat;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_chat_chat_user,parent,false);
        return new ChatAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserID = currentUser.getUid();
        Chats c = chat.get(position);
        if (c.getFrom().equals(currentUserID)) {
            holder.message.setTextColor(Color.WHITE);
            holder.linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            holder.message.setBackgroundResource(R.drawable.current_user);
        } else {
            holder.message.setTextColor(Color.BLACK);
            holder.linearLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            holder.message.setBackgroundResource(R.drawable.chat_user);
        }
        holder.message.setText(c.getMessage());

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, final int position, boolean isLongClick) {
                if (isLongClick){

                    //Nhấn giữ sẽ hiện thông báo nhập mk để giải mã
                   final String message = chat.get(position).getMessage().toString();
                   final EditText EnterPassword = new EditText(context);
                   //Tạo cái hộp thoại thông báo + nhập mật khẩu để giải mã
                    AlertDialog.Builder AES = new AlertDialog.Builder(context);
                    AES.setTitle("Giải mã");
                    AES.setMessage("Nhập mật khẩu để giải mã.");
                    AES.setView(EnterPassword);
                    //Nút yes
                    AES.setPositiveButton("Gửi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            final String password = EnterPassword.getText().toString();
                            final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
                            dataRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String Password = dataSnapshot.child("Password").getValue().toString();
                                            //Lây ID để xem tin nhắn của người nào để dùng đúng key giải mã.
                                            String ID = chat.get(position).getFrom().toString();
                                            String IDCreatekey;
                                            if(ID.equals(currentUserID)){
                                                IDCreatekey = chat.get(position).getTo().toString();
                                            } else {
                                                IDCreatekey = currentUserID;
                                            }

                                            //Thực hiện giải mã
                                            if(password.equals(Password)){
                                                AES ase = new AES();
                                                String CreateKeyAndVector = IDCreatekey + IDCreatekey;
                                                String Key = CreateKeyAndVector.substring(0,32);
                                                String initVector = CreateKeyAndVector.substring(32,48);

                                                String AESMessage = ase.decrypt(message,Key,initVector);
                                                showDialog(context,AESMessage);
                                            }
                                            else {
                                                Toast.makeText(context,"Lỗi, mật khẩu không chính xác", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    }
                            );
                        }
                    });
                    AES.create().show();
                }
                else {
                    Toast.makeText(context, "Click: " + chat.get(position), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return chat.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView message;
        LinearLayout linearLayout;
        ItemClickListener itemClickListener;
//        RelativeLayout relativeLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.item_message);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.chat);
//            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.chat);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), true);
            return true;
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    }

    public void showDialog(Context context, String msg){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_aes);

        TextView message = (TextView) dialog.findViewById(R.id.msg);
        message.setText(msg);

        dialog.show();
    }

}