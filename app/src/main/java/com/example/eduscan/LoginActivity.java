package com.example.eduscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import org.apache.http.util.TextUtils;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button buttonLogin;
    Button buttonCreateAccount;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                // Validarea emailului și parolei
                if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    loginEmail.setError("Enter a valid email address");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    loginPassword.setError("Enter a password");
                    return;
                }

                // Dacă datele sunt valide, trimite cererea către DatabaseConnection
                DatabaseConnection.getInstance().saveUser(email, password, new DatabaseConnection.UserSaveListener() {
                    @Override
                    public void onUserSaved(User user) {
                        // Redirectează utilizatorul către activitatea principală după autentificare
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Închide activitatea de login
                    }

                    @Override
                    public void onUserSaveFailed(String errorMessage) {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
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