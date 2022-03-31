package com.example.loginprac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private TextView register;
    private ProgressBar progressBar;
    private Button loginButton;
    private EditText editText_email,editText_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        register = findViewById(R.id.register);
        register.setOnClickListener(this::onClick);
        progressBar=findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        loginButton=findViewById(R.id.login);
        loginButton.setOnClickListener(this::onClick);

        editText_email=findViewById(R.id.eMail);
        editText_password=findViewById(R.id.password);

        mAuth=FirebaseAuth.getInstance();
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.register:
                startActivity(new Intent(this,RegisterUser.class));
                break;
            case R.id.login:
                userLogin();
                break;
        }
    }

    private void userLogin() {
        String email=editText_email.getText().toString().trim();
        String password = editText_password.getText().toString().trim();

        if(email.isEmpty()){
            editText_email.setError("email is required");
            editText_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editText_email.setError("Provide valid email");
            editText_email.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editText_password.setError("password is required");
            editText_password.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.i("check","transfer to user page");
                    startActivity(new Intent(MainActivity.this, userPage.class));
                }else{
                    Toast.makeText(MainActivity.this, "Incorrect Id or Password", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
