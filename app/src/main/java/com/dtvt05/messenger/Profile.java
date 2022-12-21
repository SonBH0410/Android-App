package com.dtvt05.messenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    private ImageView back, changeAvatar;
    private TextView logOut, disPlayName, email, phoneNumber, status;
    private ProgressDialog mProgressDialog;
    private CircleImageView avatar;
    private Toolbar toolbar;

    private FirebaseAuth fAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;
    private StorageReference mAvatar;

    private final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        disPlayName = (TextView) findViewById(R.id.my_name);
        email = (TextView) findViewById(R.id.my_email);
        phoneNumber = (TextView) findViewById(R.id.my_phoneNumber);
        status = (TextView) findViewById(R.id.my_status);
        avatar = (CircleImageView) findViewById(R.id.avatar_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mAvatar = FirebaseStorage.getInstance().getReference();

        logOut = (TextView) findViewById(R.id.tv_logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this,StartPage.class));
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String currentUserID = currentUser.getUid();
        final String linkAvatar = currentUserID + ".png";
        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String Name = dataSnapshot.child("DisplayName").getValue().toString();
                String Email = dataSnapshot.child("Email").getValue().toString();
                String PhoneNumber = dataSnapshot.child("PhoneNumber").getValue().toString();
                String Avatar = dataSnapshot.child("Avatar").getValue().toString();
                if(PhoneNumber.equals("null")){PhoneNumber = "000000000";}
                String Status = dataSnapshot.child("Status").getValue().toString();
                if(Status.equals("null")){Status = "Độc thân";}

                disPlayName.setText(Name);
                email.setText(Email);
                status.setText(Status);
                phoneNumber.setText(PhoneNumber);
                if(!Avatar.equals("null")) {
                    //Hiển thị ảnh của mình, giới hạn 1mb
                    StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("avatar").child(linkAvatar);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    mStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            avatar.setImageBitmap(bitmap);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //bấm nút thay đổi ảnh sẽ vào thư viện máy
        changeAvatar = (ImageView) findViewById(R.id.change_avatar);
        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
    }

    //Crop ảnh và upload lên firebase, code crop lấy trên github :v
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(Profile.this);
                mProgressDialog.setTitle("Đang tải ảnh lên...");
                mProgressDialog.setMessage("Vui lòng đợi.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                assert result != null;
                Uri resultUri = result.getUri();
                final String currentUserID = currentUser.getUid();



                final StorageReference filepath = mAvatar.child("avatar").child(currentUserID + ".png");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //Lấy link dowload ảnh lưu vào database để dùng ......... nhưng mà thôi k cần nữa nhưng
                            //cứ cmt để đây lỡ cần :V
//                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                databaseRef.child("Avatar").setValue(uri.toString());
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception exception) {
//                                    // Handle any errors
//                                }
//                            });

                            //Lưu ID vào Avatar vì đặt tên file dạng ID + png
                            databaseRef.child("Avatar").setValue(currentUserID);

                            Toast.makeText(Profile.this,"Đã tải ảnh lên.",Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        } else {
                            Toast.makeText(Profile.this,"Lỗi rồi huhu...",Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
            }
        }


    }
}
