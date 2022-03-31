package com.example.loginprac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RegisterUser extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private TextView tinMessage;
    private EditText editText_eMail,editText_age,editText_password,editText_fullName;
    private ProgressBar progressBar;
    Button registerUser;
    CheckBox isMaster;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        tinMessage=findViewById(R.id.tinMessage);
        tinMessage.setOnClickListener(this::onClick);
        registerUser=findViewById(R.id.registerUser);
        registerUser.setOnClickListener(this::onClick);

        editText_eMail=findViewById(R.id.eMail);
        editText_age=findViewById(R.id.age);
        editText_password=findViewById(R.id.password);
        editText_fullName=findViewById(R.id.fullName);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        isMaster = findViewById(R.id.isMaster);
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tinMessage:
                startActivity(new Intent(this,MainActivity.class));
                break;
            case R.id.registerUser:
                registerUser();
                break;
        }
    }
    private void registerUser(){
        String email = editText_eMail.getText().toString().trim();
        String age = editText_age.getText().toString().trim();
        String password = editText_password.getText().toString().trim();
        String name = editText_fullName.getText().toString().trim();

        if(name.isEmpty()){
            editText_fullName.setError("Full name is required");
            editText_fullName.requestFocus();
            return;
        }
        if(age.isEmpty()){
            editText_age.setError("age is required");
            editText_age.requestFocus();
            return;
        }
        if(email.isEmpty()){
            editText_eMail.setError("email is required");
            editText_eMail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editText_eMail.setError("Provide valid email");
            editText_eMail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editText_password.setError("password is required");
            editText_password.requestFocus();
            return;
        }
        if(password.length()<6){
            editText_password.setError("password to short");
            editText_password.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    KeyPairGenerator keyPairGenerator = null;
                    try {
                        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    assert keyPairGenerator != null;
                    // learn why 4096// 128-256 bit encryption
                    keyPairGenerator.initialize(4096);

                    // Generate the KeyPair
                    KeyPair keyPair = keyPairGenerator.generateKeyPair();
                    // Get the public and private key
                    PublicKey publicKey = keyPair.getPublic();
                    PrivateKey privateKey = keyPair.getPrivate();
                    Log.i("test",publicKey.toString());
                    String publicK = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
                    String privateK = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
                    boolean iM;
                    iM=isMaster.isChecked();
                    Log.i("is Master","check");
                    System.out.println(iM);
                    User user = new User(name,email,age, publicK, privateK,iM);

                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(RegisterUser.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }else{
                                    Toast.makeText(RegisterUser.this, "User registration failed try again1111", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                }else{
                    Toast.makeText(RegisterUser.this, "User registration failed try again", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
