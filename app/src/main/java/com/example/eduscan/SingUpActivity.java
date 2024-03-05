package com.example.eduscan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class SingUpActivity extends AppCompatActivity {

    EditText signUpName, signUpSurname, signUpEmail, signUpPassword, signUpConfirmPassword;
    Button buttonSignUp;
    FirebaseDatabase database;
    DatabaseReference reference;
    TextView textSignUpMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        textSignUpMessage = findViewById(R.id.textSignUpMessage);

        signUpName = findViewById(R.id.signUpName);
        signUpSurname = findViewById(R.id.signUpSurname);
        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPassword = findViewById(R.id.signUpPassword);
        signUpConfirmPassword = findViewById(R.id.signUpConfirmPassword);

        buttonSignUp = findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String name = signUpName.getText().toString();
                String surname = signUpSurname.getText().toString();
                String email = signUpEmail.getText().toString();
                String password = signUpPassword.getText().toString();
                String confirm_password = signUpConfirmPassword.getText().toString();

                User user;
                if(password.equals(confirm_password)){
                    user = new User(name, surname, email, password);
                }else{
                    textSignUpMessage.setVisibility(View.VISIBLE);
                }

                //reference.child(username).setValue(user);
                setContentView(R.layout.activity_login);
            }
        });

    }
}