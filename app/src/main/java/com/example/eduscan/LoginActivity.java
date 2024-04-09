package com.example.eduscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button buttonLogin;
    Button buttonCreateAccount;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String username = loginUsername.getText().toString();
                String password = loginPassword.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter a username!", Toast.LENGTH_SHORT).show();

                }
                 else if (password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please enter a password!", Toast.LENGTH_SHORT).show();


                }else {

                    //check user exist
                    LiveData<Boolean> usernameExists = DatabaseConnection.getInstance().checkUsername(username);

//                    usernameExists.observe(LoginActivity.this, result -> {
//
//                        if (result){
//
//                            LiveData<Boolean> correctPassword = DatabaseConnection.getInstance().checkPassword(username, password);
//
//                            correctPassword.observe(LoginActivity.this, result1 -> {
//
//                                if (result1){
//
//                                    //save user
//                                    DatabaseConnection.getInstance().saveUser(username);
//
//                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                    startActivity(intent);
//
//                                }else {
//                                    Toast.makeText(LoginActivity.this, "The password is incorrect!", Toast.LENGTH_SHORT).show();
//
//                                }
//
//                            });
//
//                        }else {
//                            Toast.makeText(LoginActivity.this, "The username doesn't exist!", Toast.LENGTH_SHORT).show();
//
//                        }
//                    });

                    usernameExists.observeForever(new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean result) {

                            if (result){
                                LiveData<Boolean> correctPassword = DatabaseConnection.getInstance().checkPassword(username, password);

                                correctPassword.observeForever(new Observer<Boolean>() {
                                    @Override
                                    public void onChanged(Boolean result1) {
                                        if (result1){
                                            //save user
                                            DatabaseConnection.getInstance().saveUser(username);

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }else {
                                            Toast.makeText(LoginActivity.this, "The password is incorrect!", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                            }else {
                                Toast.makeText(LoginActivity.this, "The username doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        });


        buttonCreateAccount.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SingUpActivity.class);
                startActivity(intent);
            }
        });


    }
}