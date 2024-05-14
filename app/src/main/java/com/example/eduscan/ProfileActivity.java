package com.example.eduscan;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



public class ProfileActivity extends AppCompatActivity {


    //
    TextView profileTextViewName;
    TextView profileTextViewEmail;

    //
    LinearLayout LayoutEditName;
    LinearLayout LayoutEditEmail;

    //
    AutoCompleteTextView autoCompleteTextViewName;

    AutoCompleteTextView autoCompleteTextViewEmail;

    //
    Button profileButtonBack;
    TextView changePassword;

    //
    ImageView buttonEditName;

    ImageView buttonEditEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        profileButtonBack = findViewById(R.id.profileButtonBack);
        changePassword = findViewById(R.id.changePassword);

        // init
        profileTextViewName = findViewById(R.id.profileTextViewName);
        profileTextViewEmail = findViewById(R.id.profileTextViewEmail);

        profileTextViewName.setText(DatabaseConnection.getInstance().getUser().getName());
        profileTextViewEmail.setText(DatabaseConnection.getInstance().getUser().getEmail());

        LayoutEditName = findViewById(R.id.LayoutEditName);
        LayoutEditEmail = findViewById(R.id.LayoutEditEmail);

        autoCompleteTextViewName = findViewById(R.id.autoCompleteTextViewName);
        autoCompleteTextViewEmail = findViewById(R.id.autoCompleteTextViewEmail);

        autoCompleteTextViewName.setText(DatabaseConnection.getInstance().getUser().getName());
        autoCompleteTextViewEmail.setText(DatabaseConnection.getInstance().getUser().getEmail());

        buttonEditName = findViewById(R.id.buttonEditName);
        buttonEditEmail = findViewById(R.id.buttonEditEmail);


        profileTextViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutEditName.setVisibility(View.VISIBLE);
                profileTextViewName.setVisibility(View.GONE);

                buttonEditName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newName = autoCompleteTextViewName.getText().toString().trim(); //fara spatii



                        if (!newName.isEmpty() && !newName.equals(DatabaseConnection.getInstance().getUser().getName().trim())){

                            DatabaseConnection.getInstance().changeName(newName, new DatabaseUpdateListener() {
                                @Override
                                public void onUpdateSuccess() {
                                    runOnUiThread(() -> {
                                        profileTextViewName.setText(newName);
                                        autoCompleteTextViewName.setText(newName);

                                        LayoutEditName.setVisibility(View.GONE);
                                        profileTextViewName.setVisibility(View.VISIBLE);

                                        Toast.makeText(ProfileActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();

                                    });
                                }

                                @Override
                                public void onUpdateFailure(String errorMessage) {
                                    runOnUiThread(() -> {
                                        LayoutEditName.setVisibility(View.GONE);
                                        profileTextViewName.setVisibility(View.VISIBLE);
                                        Toast.makeText(ProfileActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();

                                    });
                                }
                            });


                        }else if(newName.equals(DatabaseConnection.getInstance().getUser().getName().trim())) {
                            LayoutEditName.setVisibility(View.GONE);
                            profileTextViewName.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();
                        }else {
                            LayoutEditName.setVisibility(View.GONE);
                            profileTextViewName.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Not a valid name!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });


        profileTextViewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutEditEmail.setVisibility(View.VISIBLE);
                profileTextViewEmail.setVisibility(View.GONE);

                buttonEditEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String newEmail = autoCompleteTextViewEmail.getText().toString().trim();
                        if(!newEmail.isEmpty() && !newEmail.equals(DatabaseConnection.getInstance().getUser().getEmail()) && isValidEmail(newEmail)){

                            DatabaseConnection.getInstance().changeEmail(newEmail, new DatabaseUpdateListener() {
                                @Override
                                public void onUpdateSuccess() {
                                    runOnUiThread(() -> {
                                        autoCompleteTextViewEmail.setText(newEmail);
                                        profileTextViewEmail.setText(newEmail);

                                        LayoutEditEmail.setVisibility(View.GONE);
                                        profileTextViewEmail.setVisibility(View.VISIBLE);
                                        Toast.makeText(ProfileActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();

                                    });
                                }

                                @Override
                                public void onUpdateFailure(String errorMessage) {
                                    runOnUiThread(() -> {

                                        LayoutEditEmail.setVisibility(View.GONE);
                                        profileTextViewEmail.setVisibility(View.VISIBLE);
                                        Toast.makeText(ProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();

                                    });
                                }
                            });


                        } else if (newEmail.equals(DatabaseConnection.getInstance().getUser().getEmail())) {
                            LayoutEditEmail.setVisibility(View.GONE);
                            profileTextViewEmail.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Nothing changed", Toast.LENGTH_SHORT).show();
                        } else {
                            LayoutEditEmail.setVisibility(View.GONE);
                            profileTextViewEmail.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Not a valid email!", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        });




        profileButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean isValidEmail(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}