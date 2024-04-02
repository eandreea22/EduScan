package com.example.eduscan;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



public class ProfileActivity extends AppCompatActivity {

    Button profileButtonBack;
    Button profileButtonEdit;
    AutoCompleteTextView profileCompleteName;
    AutoCompleteTextView profileCompleteUsername;
    AutoCompleteTextView profileCompleteEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        profileButtonBack = findViewById(R.id.profileButtonBack);
        profileButtonEdit = findViewById(R.id.profileButtonEdit);

        profileCompleteName = findViewById(R.id.profileCompleteName);
        profileCompleteName.setText(DatabaseConnection.getInstance().getUser().getName());

        profileCompleteUsername = findViewById(R.id.profileCompleteUsername);
        profileCompleteUsername.setText(DatabaseConnection.getInstance().getUser().getUsername());

        profileCompleteEmail = findViewById(R.id.profileCompleteEmail);
        profileCompleteEmail.setText(DatabaseConnection.getInstance().getUser().getEmail());



        profileButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newName = profileCompleteName.getText().toString().trim(); //fara spatii

                if (!newName.isEmpty() && !newName.equals(DatabaseConnection.getInstance().getUser().getName())){

                    DatabaseConnection.getInstance().changeName(newName, new DatabaseUpdateListener() {
                        @Override
                        public void onUpdateSuccess() {
                            runOnUiThread(() -> {
                                profileCompleteName.setText(newName);
                                Toast.makeText(ProfileActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();

                            });
                        }

                        @Override
                        public void onUpdateFailure(String errorMessage) {
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();

                            });
                        }
                    });


                }else {
                    Toast.makeText(ProfileActivity.this, "Not a valid name!", Toast.LENGTH_SHORT).show();

                }

                String newEmail = profileCompleteEmail.getText().toString().trim();
                if(!newEmail.isEmpty() && !newEmail.equals(DatabaseConnection.getInstance().getUser().getEmail()) && isValidEmail(newEmail)){

                    DatabaseConnection.getInstance().changeEmail(newEmail, new DatabaseUpdateListener() {
                        @Override
                        public void onUpdateSuccess() {
                            runOnUiThread(() -> {
                                profileCompleteEmail.setText(newEmail);
                                Toast.makeText(ProfileActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();

                            });
                        }

                        @Override
                        public void onUpdateFailure(String errorMessage) {
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();

                            });
                        }
                    });


                }else {
                    Toast.makeText(ProfileActivity.this, "Not a valid email!", Toast.LENGTH_SHORT).show();

                }

                String newUsername = profileCompleteUsername.getText().toString().trim();
                if (!newUsername.isEmpty() && !newUsername.equals(DatabaseConnection.getInstance().getUser().getUsername()) && isValidUsername(newUsername)) {

                    DatabaseConnection.getInstance().changeUsername(newUsername, new DatabaseUpdateListener() {
                        @Override
                        public void onUpdateSuccess() {
                            runOnUiThread(() -> {
                                profileCompleteUsername.setText(newUsername);
                                Toast.makeText(ProfileActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();

                            });
                        }

                        @Override
                        public void onUpdateFailure(String errorMessage) {
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();

                            });
                        }
                    });

                }else {
                    Toast.makeText(ProfileActivity.this, "Not a valid username!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        profileButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private boolean isValidEmail(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidUsername(CharSequence username) {
        return username.toString().matches("^[a-zA-Z0-9_.]{3,16}$");
    }


}