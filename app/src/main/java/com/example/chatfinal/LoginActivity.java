package com.example.chatfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email,pass;
    Button login;
    TextView signUp;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    ProgressDialog progressDialog;

    FirebaseUser currentuser;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
// code from here
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Login to Account");
        progressDialog.setMessage("Loading");

        email = findViewById(R.id.editTextTextEmailAddress);
        pass= findViewById(R.id.Rpassword);
        login = findViewById(R.id.btn_SignIn);
        signUp = findViewById(R.id.tvClickSignUp);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateField()) {
                    Toast.makeText(LoginActivity.this, "Fill All Field", Toast.LENGTH_SHORT).show();

                } else
                {
                    progressDialog.show();

                    auth.signInWithEmailAndPassword(email.getText().toString().trim(), pass
                            .getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "you are not user", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser!= null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private boolean validateField() {
        int flag=0;

        if (email.getText().toString().isEmpty())
        {
            email.setError("Enter Email");
            flag =1;
        }

        if (pass.getText().toString().isEmpty())
        {
            pass.setError("Enter Password");
            flag=1;
        }

        if (flag==1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}