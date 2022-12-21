package com.dtvt05.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private TextInputLayout email, password;
    private Button btnLogin;
    private TextView forgotPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        email = (TextInputLayout) findViewById(R.id.email_login);
        password = (TextInputLayout) findViewById(R.id.password_login);
        forgotPassword = (TextView) findViewById(R.id.tv_forgot_pass);
        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        btnLogin = (Button) findViewById(R.id.btn_login);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetPassword = new EditText(v.getContext());
                //Tạo cái hộp thoại thông báo + nhập email để nhận link reset password
                AlertDialog.Builder passwordReset = new AlertDialog.Builder(v.getContext());
                passwordReset.setTitle("Khôi phục mật khẩu");
                passwordReset.setMessage("Nhập email đăng kí tài khoản.");
                passwordReset.setView(resetPassword);
                //Nút yes
                passwordReset.setPositiveButton("Gửi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetPassword.getText().toString();
                        firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Link khôi phục mật khẩu đã được gửi.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Link khôi phục mật khẩu chưa được gửi." + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                //Nút no
                passwordReset.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                passwordReset.create().show();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getEditText().getText().toString().trim();
                String Password = password.getEditText().getText().toString().trim();
                //Kiểm tra rỗng
                if (TextUtils.isEmpty(Email)) {
                    email.setError("Bạn chưa nhập email.");
                    return;
                }
                if (TextUtils.isEmpty(Password)) {
                    password.setError("Bạn chưa nhập mật khẩu.");
                    return;
                }

                //Đăng nhập bằng email + password
                firebaseAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.VISIBLE);
                            Toast.makeText(Login.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(Login.this, MainActivity.class));

                        } else {
                            Toast.makeText(Login.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });
    }
}
