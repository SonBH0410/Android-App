package com.dtvt05.messenger;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dtvt05.messenger.Model.Chats;
import com.dtvt05.messenger.Model.Users;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewMessage extends AppCompatActivity {
    private Toolbar toolbar;
    private Button send;
    private Switch AES;
    private TextInputLayout email, message;
    private DatabaseReference dataRef;
    private FirebaseUser currentUser;
    private final static String ON = "AES: On";
    private final static String OFF = "AES: Off";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Gửi tin nhắn");

        AES = (Switch) findViewById(R.id.switch1);
        email = (TextInputLayout) findViewById(R.id.email_sendMess);
        message = (TextInputLayout) findViewById(R.id.txt_mess);
        send = (Button) findViewById(R.id.btn_send);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String currentUserID = currentUser.getUid();
        AES.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TextView on = (TextView) findViewById(R.id.on_off);
                    on.setText("AES: On");
                } else {
                    TextView off = (TextView) findViewById(R.id.on_off);
                    off.setText("AES: Off");
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users");
                dataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String Email = email.getEditText().getText().toString().trim();
                        String Message = message.getEditText().getText().toString();
                        int check = 0;
                        for (DataSnapshot Snapshot : dataSnapshot.getChildren()) {
                            Users user = Snapshot.getValue(Users.class);

                            assert user != null;
                            if (user.getEmail().equals(Email) && !Message.isEmpty()) {
                                String chatUserID = Snapshot.getKey();   //ID người nhận
                                String chatUserName = user.getDisplayName();  //Tên người nhân

                                //Xử lý phần AES trước  khi gửi
                                //lấy ID của người nhận là key,  ID 28 kí tự mà key 256bit = 32 kí tự,initVector 16 kí tự
                                //Lấy ID + ID = 56 kí tự, lấy 32 kí tự đầu  là key, 16 kí tự tiếp theo là initvector.
                                AES ase = new AES();
                                String CreateKeyAndVector = chatUserID + chatUserID;
                                String Key = CreateKeyAndVector.substring(0,32);
                                String initVector = CreateKeyAndVector.substring(32,48);


                                //Check xem bật AES hay không?
                                TextView on_off = (TextView) findViewById(R.id.on_off);
                                CharSequence status = on_off.getText();
                                String AESMessage = null;
                                if (status.toString().equals(ON)) {
                                    AESMessage = ase.encrypt(Message, Key, initVector);
                                }
                                if (status.toString().equals(OFF)) {
                                    AESMessage = Message;
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

                                //Lưu tên, avatar, tin nhắn (chính là tin nhắn cuối để hiện thị ở ChatFragment
                                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    if(snapshot.getKey().equals(currentUserID)){
                                        Users mUser = snapshot.getValue(Users.class);
                                        assert mUser != null;
                                        String mName = mUser.getDisplayName();

                                        Map<String, Object> forChatUser = new HashMap<>();
                                        forChatUser.put("DisplayName",mName);
                                        forChatUser.put("Avatar",currentUserID);   //lưu id để  lấy ảnh vì ảnh tên dạng ID + .png
                                        forChatUser.put("LastMessage", AESMessage);
                                        forChatUser.put("Time", ServerValue.TIMESTAMP);
                                        data.child("ListChat4Recycleview").child(chatUserID).child(currentUserID)
                                                .setValue(forChatUser);

                                        Map<String, Object> forCurrentUser = new HashMap<>();
                                        forCurrentUser.put("DisplayName",chatUserName);
                                        forCurrentUser.put("Avatar",chatUserID);
                                        forCurrentUser.put("LastMessage",AESMessage);
                                        forCurrentUser.put("Time", ServerValue.TIMESTAMP);
                                        data.child("ListChat4Recycleview").child(currentUserID).child(chatUserID)
                                                .setValue(forCurrentUser);
                                    }
                                }
                                email.getEditText().setText("");
                                message.getEditText().setText("");
                                Toast.makeText(getApplicationContext(), "Đã gửi", Toast.LENGTH_SHORT).show();
                                check = 1;
                            }
                        }
                        if (check == 0) {
                            Toast.makeText(getApplicationContext(), "Email chưa đúng hoặc tin nhắn trống.", Toast.LENGTH_SHORT).show();
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
