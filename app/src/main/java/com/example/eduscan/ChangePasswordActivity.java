package com.example.eduscan;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangePasswordActivity extends AppCompatActivity {

    //
    AutoCompleteTextView autoCompleteTextViewPassword;
    AutoCompleteTextView autoCompleteTextViewNewPassword;
    AutoCompleteTextView autoCompleteTextViewConfirmPassword;

    //
    Button buttonChangePassword;
    Button buttonBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //
        autoCompleteTextViewPassword = findViewById(R.id.autoCompleteTextViewPassword);
        autoCompleteTextViewNewPassword = findViewById(R.id.autoCompleteTextViewNewPassword);
        autoCompleteTextViewConfirmPassword = findViewById(R.id.autoCompleteTextViewConfirmPassword);

        //
        buttonBack = findViewById(R.id.buttonBack);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = autoCompleteTextViewPassword.getText().toString().trim();

                if(!currentPassword.isEmpty() && currentPassword.equals(DatabaseConnection.getInstance().getUser().getPassword())){

                    String newPassword = autoCompleteTextViewNewPassword.getText().toString().trim();
                    String confirmPassword = autoCompleteTextViewConfirmPassword.getText().toString().trim();

                    if (newPassword.length()>=8){
                        if (newPassword.equals(confirmPassword) && !newPassword.equals(currentPassword)){

                            DatabaseConnection.getInstance().changePassword(newPassword, new DatabaseUpdateListener() {
                                @Override
                                public void onUpdateSuccess() {
                                    runOnUiThread(() -> {
                                        Toast.makeText(ChangePasswordActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();

                                    });
                                }

                                @Override
                                public void onUpdateFailure(String errorMessage) {
                                    runOnUiThread(() -> {

                                        Toast.makeText(ChangePasswordActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();

                                    });
                                }
                            });

                        }else {
                            Toast.makeText(ChangePasswordActivity.this, "The new password doesn't match", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(ChangePasswordActivity.this, "The password must have at least 8 characters!", Toast.LENGTH_SHORT).show();

                    }


                }else {
                    Log.e(TAG, "current pass: " + DatabaseConnection.getInstance().getUser().getPassword());
                    Log.e(TAG, "current pass: " + currentPassword );
                    Toast.makeText(ChangePasswordActivity.this, "The current password doesn't match", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}