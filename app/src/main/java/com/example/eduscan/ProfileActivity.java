package com.example.eduscan;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eduscan.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    Button profileButtonBack;
    Button profileButtonEdit;

    AutoCompleteTextView profileCompleteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileButtonBack = findViewById(R.id.profileButtonBack);
        profileButtonEdit = findViewById(R.id.profileButtonEdit);

        profileCompleteName = findViewById(R.id.profileCompleteName);
        profileCompleteName.setText("nameeeee");

        profileButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}