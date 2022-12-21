package com.dtvt05.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtvt05.messenger.Fragment.ChatFragment;
import com.dtvt05.messenger.Fragment.FriendFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity{
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    private Toolbar toolbar;
    private TextView title;
    private CircleImageView avatar;
    private ImageView newMessage, addFriend;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser =firebaseAuth.getCurrentUser();

        //Check đăng nhập chưa và xác nhận mail chưa?
        databaseRef = FirebaseDatabase.getInstance().getReference();
        if(currentUser == null){
            startActivity(new Intent(this,StartPage.class));
        }
        if(!currentUser.isEmailVerified()){
            startActivity(new Intent(this,VerifyEmail.class));
        }

        //tạo cái bottom nav
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        //mặc định chat fragment
        newMessage = (ImageView) findViewById(R.id.new_mess);
        addFriend = (ImageView) findViewById(R.id.add_friend);
        addFriend.setVisibility(View.INVISIBLE);
        newMessage.setVisibility(View. VISIBLE);
        showFragment(new ChatFragment());

        //Set title và set ảnh trên toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = toolbar.findViewById(R.id.title);
        title.setText("Tin nhắn");
        String currentUserID = currentUser.getUid();

        //down ảnh về để hiển thị, giới  hạn 1MB
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("avatar").child(currentUserID + ".png");
        final long ONE_MEGABYTE = 1024 * 1024;
        mStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bitmap);
            }
        });

        //Click vào ảnh trên  toolbar
        avatar = toolbar.findViewById(R.id.avatar_toolbar);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        });

        newMessage = (ImageView) toolbar.findViewById(R.id.new_mess);
        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NewMessage.class));
            }
        });
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                    newMessage = (ImageView) findViewById(R.id.new_mess);
//                    addFriend = (ImageView) findViewById(R.id.add_friend);
                    switch(item.getItemId()){
                        case R.id.nav_chat:
                            addFriend.setVisibility(View.INVISIBLE);
                            newMessage.setVisibility(View. VISIBLE);
                            showFragment(new ChatFragment());
                            title.setText("Tin nhắn");
                            break;
                        case R.id.nav_friend:
                            addFriend.setVisibility(View.VISIBLE);
                            newMessage.setVisibility(View.INVISIBLE);
                            showFragment(new FriendFragment());
                            title.setText("Bạn bè");
                            break;
                    }
                    return true;
                }
            };

    //hiển thị Fragment
    public void showFragment(Fragment fragment){
       getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }
}
