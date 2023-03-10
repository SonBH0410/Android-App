package com.dtvt05.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private TextInputLayout mEmail, mDisplayName, mPassword, mPassword2;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = (TextInputLayout) findViewById(R.id.email_SignUp);
        mDisplayName = (TextInputLayout) findViewById(R.id.displayName_SignUp);
        mPassword = (TextInputLayout) findViewById(R.id.password_SignUp);
        mPassword2 = (TextInputLayout) findViewById(R.id.password_SignUp2);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_SignUp);

        firebaseAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getEditText().getText().toString().trim();
                final String displayName = mDisplayName.getEditText().getText().toString().trim();
                final String password = mPassword.getEditText().getText().toString().trim();
                String password2 = mPassword2.getEditText().getText().toString().trim();

//
//                //Ki???m tra r???ng
//                if (TextUtils.isEmpty(email)) {
//                    mEmail.setError("Email tr???ng.");
//                    return;
//                }
//                else {
//                    mEmail.setErrorEnabled(false);
//                }
//                if (TextUtils.isEmpty(displayName)) {
//                    mDisplayName.setError("Email tr???ng.");
//                    return;
//                }
////                else {
////                    mDisplayName.setErrorEnabled(false);
////                }
//                if (TextUtils.isEmpty(password)) {
//                    mPassword.setError("Email tr???ng.");
//                    return;
//                }
////                else {
////                    mPassword.setErrorEnabled(false);
////                }
//                if (TextUtils.isEmpty(password2)) {
//                    mPassword2.setError("Email tr???ng.");
//                    return;
//                }
////                else {
////                    mPassword2.setErrorEnabled(false);
////                }
                checkIsEmpty(email,mEmail);
                checkIsEmpty(password,mPassword);
                checkIsEmpty(displayName,mDisplayName);
                checkIsEmpty(password2,mPassword2);
                if(!password.equals(password2))
                {
                    mPassword2.setError("M???t kh???u kh??ng kh???p.");
                    return;
                }
                //Ki???m tra ????? d??i password
                if (password.length() < 6) {
                    mPassword.setError("M???t kh???u ph???i l???n h??n 6 k?? t???.");
                    return;
                }

                //Hi???n v??ng quay tr??n tr??n =))
                progressBar.setVisibility(View.VISIBLE);

                //T???o tk b???ng firebase
                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            String userID = user.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            HashMap<String,String> users = new HashMap<>();
                            users.put("Email",email);
                            users.put("DisplayName",displayName);
                            users.put("Avatar","null");
                            users.put("PhoneNumber","null");
                            users.put("Status","null");
                            users.put("Password",password);
                            databaseReference.child(userID).setValue(users);
                            //setDisplayName
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName).build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("tag", "???? c???p nh???t th??ng tin th??nh c??ng");
                                            }
                                        }
                                    });

                            //Gui mail xac thuc
                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUp.this, "Email x??c nh???n ???? ???????c g???i.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("tag", "Email ch??a ???????c g???i" + e.getMessage());
                                }
                            });

                            Toast.makeText(SignUp.this, "????ng k?? th??nh c??ng!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUp.this,VerifyEmail.class));
                        } else {
                            Toast.makeText(SignUp.this, "L???i!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    public void checkIsEmpty(String s, TextInputLayout t){
        if(TextUtils.isEmpty(s)){
            t.setError("Kh??ng ???????c ????? tr???ng!");
            return;
        }
        else {
            t.setErrorEnabled(false);
        }
    }

}
