package com.example.eduscan;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DatabaseConnection {

    private User user;

    private static DatabaseConnection instance;
    private FirebaseDatabase database;



    DatabaseConnection() {
        database = FirebaseDatabase.getInstance();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public void addUser(User user){

        DatabaseReference reference = database.getReference("users");

        reference.child(user.getUsername()).setValue(user);
    }

//    public boolean checkUsername(String username){
//
//        DatabaseReference reference = database.getReference("users");
//        Query checkUsername = reference.orderByChild("username").equalTo(username);
//
//        boolean ok = false;
//        checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    ok = true;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                System.out.println("The read failed: " + error.getCode());
//            }
//        });
//
//        return ok;
//    }

}
