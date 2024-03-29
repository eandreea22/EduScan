package com.example.eduscan;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DatabaseConnection {

    private User user = new User();

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


    public User getUser() {
        return user;
    }

    public void addUser(User user){

        DatabaseReference reference = database.getReference("users");

        reference.push().setValue(user);
    }

    public MutableLiveData<Boolean> checkUsername(String username) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        DatabaseReference reference = database.getReference("users");
        Query checkUsername = reference.orderByChild("username").equalTo(username);

        checkUsername.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                result.setValue(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("error");
            }
        });

        return result;
    }

    public MutableLiveData<Boolean> checkPassword(String username, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        DatabaseReference reference = database.getReference("users");
        Query query = reference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    for (DataSnapshot userSnapshot : snapshot.getChildren()){
                        String password_good = userSnapshot.child("password").getValue(String.class);
                        result.setValue(password_good.equals(password));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("error");
            }
        });



        return result;
    }


    public void saveUser(String username){


        DatabaseReference reference = database.getReference("users");
        Query query = reference.orderByChild("username").equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    for (DataSnapshot userSnapshot : snapshot.getChildren()){
                        String userId = userSnapshot.getKey();
                        String password = userSnapshot.child("password").getValue(String.class);
                        String name = userSnapshot.child("name").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);

                        user.setName(name);
                        user.setUsername(username);
                        user.setEmail(email);
                        user.setPassword(password);
                        user.setId(userId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("error");

            }
        });

    }

    public void changeName(String newName, DatabaseUpdateListener listener){

        user.setName(newName);

        DatabaseReference reference = database.getReference("users");
        reference.child(user.getId()).child("name").setValue(newName)
                .addOnSuccessListener(aVoid -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
    }

    public void changeUsername(String newUsername, DatabaseUpdateListener listener){

        user.setUsername(newUsername);

        DatabaseReference reference = database.getReference("users");
        reference.child(user.getId()).child("username").setValue(newUsername)
                .addOnSuccessListener(aVoid -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));

    }

    public void changeEmail(String newEmail, DatabaseUpdateListener listener){

        user.setEmail(newEmail);

        DatabaseReference reference = database.getReference("users");
        reference.child(user.getId()).child("email").setValue(newEmail)
                .addOnSuccessListener(aVoid -> listener.onUpdateSuccess())
                .addOnFailureListener(e -> listener.onUpdateFailure(e.getMessage()));
    }


}
