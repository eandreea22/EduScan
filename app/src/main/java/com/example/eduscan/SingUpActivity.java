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
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;




public class SingUpActivity extends AppCompatActivity {

    EditText signUpName, signUpEmail, signUpPassword;
    Button buttonSignUp;

    TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);


        signUpName = findViewById(R.id.signUpName);

        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPassword = findViewById(R.id.signUpPassword);

        buttonSignUp = findViewById(R.id.buttonSignUp);

        loginRedirect = findViewById(R.id.loginRedirect);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {


//              startActivity(new Intent(SingUpActivity.this, PopUpNewAccount.class));


                String name = signUpName.getText().toString();
                String email = signUpEmail.getText().toString();
                String password = signUpPassword.getText().toString();


                if (password.isEmpty()){
                    Toast.makeText(SingUpActivity.this, "Please enter a password!", Toast.LENGTH_SHORT).show();

                }else if(name.isEmpty()){
                    Toast.makeText(SingUpActivity.this, "Please enter your name!", Toast.LENGTH_SHORT).show();


                }else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    Toast.makeText(SingUpActivity.this, "Please enter a valid email!", Toast.LENGTH_SHORT).show();


                } else if (password.length()<8) {
                    Toast.makeText(SingUpActivity.this, "The password must have at least 8 characters!", Toast.LENGTH_SHORT).show();

                } else {

                    // verif if username exists
                    LiveData<Boolean> usernameExists = DatabaseConnection.getInstance().checkUsername(email);

                    usernameExists.observe(SingUpActivity.this, result -> {

                        if (result){
                            Toast.makeText(SingUpActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();

                        }else {

                            // add user
                            User user = new User();
                            user.setName(name);
                            user.setEmail(email);
                            user.setPassword(password);

                            DatabaseConnection.getInstance().addUser(user);

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