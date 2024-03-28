package com.example.eduscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    TextView textLoginMessage;
    Button buttonLogin;
    Button buttonCreateAccount;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textLoginMessage = findViewById(R.id.textLoginMessage);

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
                    textLoginMessage.setText("Please enter a username!");
                    textLoginMessage.setVisibility(View.VISIBLE);

                }
                 else if (password.isEmpty()){
                    textLoginMessage.setText("Please enter a password!");
                    textLoginMessage.setVisibility(View.VISIBLE);

                }else {

                    //check user exist

                    Query checkUsername = reference.orderByChild("username").equalTo(username);

                    checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (!snapshot.exists()){
                                textLoginMessage.setText("The username doesn't exist!");
                                textLoginMessage.setVisibility(View.VISIBLE);

                            }else{
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("users");
                                Query checkPassword = reference1.orderByChild("password").equalTo(password);

                                checkPassword.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);

                                        }else {
                                            textLoginMessage.setText("The password is incorrect!");
                                            textLoginMessage.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        System.out.println("The read failed: " + error.getCode());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            System.out.println("The read failed: " + error.getCode());
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