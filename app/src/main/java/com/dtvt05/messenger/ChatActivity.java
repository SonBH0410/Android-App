package com.dtvt05.messenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

//import com.dtvt05.messenger.Adapter.ChatAdapter;
import com.dtvt05.messenger.Adapter.ChatAdapter;
import com.dtvt05.messenger.Model.Chats;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Switch AES;
    private String currentUserID, chatUserID;
    private RecyclerView recyclerView;
    private ImageView send;
    private EditText message;

    private DatabaseReference dataRef;
    private FirebaseUser currentUser;

    private final static String ON = "AES: On";
    private final static String OFF = "AES: Off";

    private final ArrayList<Chats> listChats =  new ArrayList<>();
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Hiển thị toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Lấy dữ liệu từ bundle đã được gửi sang từ ChatFragment ( ở ListChatAdapter.java)
        Bundle bundle = getIntent().getExtras();
        chatUserID = bundle.getString("chatUserID");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = currentUser.getUid();


        //currentUser là người đang dùng = mình, chatUser là người mình chat cùng
        dataRef = FirebaseDatabase.getInstance().getReference();
        dataRef.child("Users").child(chatUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Hiển  thị tên
                String chatUserName = dataSnapshot.child("DisplayName").getValue().toString();
                getSupportActionBar().setTitle(chatUserName);

                //Hiển thị ảnh
                String chatUserAvatar = dataSnapshot.child("Avatar").getValue().toString();
                final CircleImageView avatar = (CircleImageView) findViewById(R.id.Chat_Avatar);
                StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("avatar").child(chatUserID + ".png");
                final long ONE_MEGABYTE = 1024 * 1024;
                mStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        avatar.setImageBitmap(bitmap);

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //đây là set cho cái switch on, off sẽ hiển thị ON,OFF :v
        AES = (Switch) findViewById(R.id.switch2);
        AES.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TextView on = (TextView) findViewById(R.id.on_off_aes);
                    on.setText(ON);
                } else {
                    TextView off = (TextView) findViewById(R.id.on_off_aes);
                    off.setText(OFF);
                }

            }
        });

        chatAdapter = new ChatAdapter(listChats,ChatActivity.this);
        recyclerView = findViewById(R.id.recycleView_chat_activity);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.smoothScrollToPosition(listChats.size());
//        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(chatAdapter);
        //Load tin nhắn để hiển thị
        dataRef.child("Chats").child(currentUserID).child(chatUserID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    Chats chat = dataSnapshot.getValue(Chats.class);
                    listChats.add(chat);
                    recyclerView.smoothScrollToPosition(listChats.size());
                    chatAdapter.notifyDataSetChanged();
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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Gửi tin nhắn
        send =(ImageView) findViewById(R.id.iv_send);
        message = (EditText) findViewById(R.id.Chat_txt);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String mess = message.getText().toString().trim();
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                            if(chatUserID.equals(snapshot.getKey()) && !mess.equals("")){

                                //Xử lý phần AES trước  khi gửi
                                //lấy ID của người nhận là key,  ID 28 kí tự mà key 256bit = 32 kí tự,initVector 16 kí tự
                                //Lấy ID + ID = 56 kí tự, lấy 32 kí tự đầu  là key, 16 kí tự tiếp theo là initvector.
                                AES ase = new AES();
                                String CreateKeyAndVector = chatUserID + chatUserID;
                                String Key = CreateKeyAndVector.substring(0,32);
                                String initVector = CreateKeyAndVector.substring(32,48);

                                //Check xem bật AES hay không?
                                TextView on_off = (TextView) findViewById(R.id.on_off_aes);
                                CharSequence status = on_off.getText();
                                String AESMessage = null;
                                if (status.toString().equals(ON)) {
                                    AESMessage = ase.encrypt(mess, Key, initVector);
                                }
                                if (status.toString().equals(OFF)) {
                                    AESMessage = mess;
                                }

                                //Gửi tin nhắn và lưu vào chats
                                Map<String, Object> map = new HashMap<>();
                                map.put("Message", AESMessage);
                                map.put("From", currentUserID);
                                map.put("To",chatUserID);
                                map.put("Seen", false);
                                map.put("Time", ServerValue.TIMESTAMP);
                                DatabaseReference data = FirebaseDatabase.getInstance().getReference();
                                data.child("Chats").child(chatUserID).child(currentUserID).push().setValue(map);
                                data.child("Chats").child(currentUserID).child(chatUserID).push().setValue(map);

                                //Lưu vào tin nhắn  cuối để hiển thị bên  ngoài.
                                String DisplayName = getSupportActionBar().getTitle().toString();

                                dataRef.child("ListChat4Recycleview").child(currentUserID).child(chatUserID).child("LastMessage")
                                        .setValue(AESMessage);
                                dataRef.child("ListChat4Recycleview").child(chatUserID).child(currentUserID).child("LastMessage")
                                        .setValue(AESMessage);

                                dataRef.child("ListChat4Recycleview").child(currentUserID).child(chatUserID).child("Time")
                                        .setValue(ServerValue.TIMESTAMP);
                                dataRef.child("ListChat4Recycleview").child(chatUserID).child(currentUserID).child("Time")
                                        .setValue(ServerValue.TIMESTAMP);

                                //Lưu lại tên trong trường hợp đổi tên thì sẽ lấy được tên mới
                                dataRef.child("ListChat4Recycleview").child(currentUserID).child(chatUserID).child("DisplayName")
                                        .setValue(DisplayName);
//                                dataRef.child("ListChat4Recycleview").child(chatUserID).child(currentUserID).child("DisplayName")
////                                        .setValue(DisplayName);

                                message.setText("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
