package com.example.eduscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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




public class SingUpActivity extends AppCompatActivity {

    EditText signUpName, signUpUsername, signUpEmail, signUpPassword;
    Button buttonSignUp;
    TextView textSignUpMessage;
    TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        textSignUpMessage = findViewById(R.id.textSignUpMessage);

        signUpName = findViewById(R.id.signUpName);
        signUpUsername = findViewById(R.id.signUpUsername);
        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPassword = findViewById(R.id.signUpPassword);

        buttonSignUp = findViewById(R.id.buttonSignUp);

        loginRedirect = findViewById(R.id.loginRedirect);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {


//              startActivity(new Intent(SingUpActivity.this, PopUpNewAccount.class));


                String name = signUpName.getText().toString();
                String username = signUpUsername.getText().toString();
                String email = signUpEmail.getText().toString();
                String password = signUpPassword.getText().toString();


                if (username.isEmpty()){
                    textSignUpMessage.setText("Please enter a username!");
                    textSignUpMessage.setVisibility(View.VISIBLE);

                }else if (password.isEmpty()){
                    textSignUpMessage.setText("Please enter a password!");
                    textSignUpMessage.setVisibility(View.VISIBLE);

                }else if(name.isEmpty()){
                    textSignUpMessage.setText("Please enter your name!");
                    textSignUpMessage.setVisibility(View.VISIBLE);

                }else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    textSignUpMessage.setText("Please enter a valid email!");
                    textSignUpMessage.setVisibility(View.VISIBLE);

                } else {

                    // verif if username exists
                    LiveData<Boolean> usernameExists = DatabaseConnection.getInstance().checkUsername(username);

                    usernameExists.observe(SingUpActivity.this, result -> {

                        if (result){
                            textSignUpMessage.setText("Username already exists!");
                            textSignUpMessage.setVisibility(View.VISIBLE);
                        }else {

                            // add user
                            User user = new User();
                            user.setUsername(username);
                            user.setName(name);
                            user.setEmail(email);
                            user.setPassword(password);

                            DatabaseConnection.getInstance().addUser(user);

//                                startActivity(new Intent(SingUpActivity.this, PopUpNewAccount.class));


                            Intent intent = new Intent(SingUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }

                    });


                }


            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}