package com.example.eduscan;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        DatabaseReference usersRef = database.getReference("users");

        // Adăugați utilizatorul în baza de date
        DatabaseReference newUserRef = usersRef.push();
        newUserRef.setValue(user);

        // Adăugați sub-nodul "files" pentru utilizatorul nou creat
        DatabaseReference userFilesRef = newUserRef.child("files");
        userFilesRef.setValue("");

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(), user.getPassword());
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


    public void saveUser(String email, String password, final UserSaveListener listener) {
        DatabaseReference reference = database.getReference("users");

        // Caută subnodurile de utilizatori
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    // Verifică fiecare subnod pentru a găsi adresa de email
                    String userEmail = userSnapshot.child("email").getValue(String.class);

                        // Dacă adresa de email este găsită, folosește credențialele pentru autentificare
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        // Autentificarea a reușit, obțineți utilizatorul curent
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser != null) {
                                            // Salvare date utilizator în aplicație
                                            String userId = firebaseUser.getUid();
                                            user.setId(userId);
                                            user.setEmail(email);
                                            // Apelați metoda onUserSaved din listener pentru a notifica activitatea că utilizatorul a fost salvat cu succes
                                            listener.onUserSaved(user);
                                        } else {
                                            // Utilizatorul curent este null, tratați această situație corespunzător
                                            listener.onUserSaveFailed("Current user is null");
                                        }
                                    } else {
                                        // Autentificarea a eșuat, tratați această situație corespunzător
                                        listener.onUserSaveFailed("Authentication failed: " + task.getException().getMessage());
                                    }
                                });
                        // Întrerupeți bucla după ce ați găsit adresa de email
                        break;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Tratează eroarea de bază de date
                listener.onUserSaveFailed("Database error: " + error.getMessage());
            }
        });
    }


    public interface UserSaveListener {
        void onUserSaved(User user);
        void onUserSaveFailed(String errorMessage);
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

//    public void addPdfFile(String downloadUrl, String filename){
//
//        // Crearea unei referințe către nodul "files" al utilizatorului specificat
//        StorageReference userFilesRef = FirebaseStorage.getInstance().getReference().child("users").child(user.getId()).child("files");
//
//        // Salvați URL-ul de descărcare în baza de date Firebase
//        DatabaseReference userFilesDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getId()).child("files");
//
//        // Actualizarea bazei de date cu URL-ul de descărcare și numele fișierului asociat
//        userFilesDatabaseRef.child(filename).setValue(downloadUrl)
//                .addOnSuccessListener(aVoid -> {
//                    // PDF-ul a fost salvat cu succes în baza de date
//                    Toast.makeText(getApplicationContext(), "PDF-ul a fost salvat cu succes în baza de date.", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    // A apărut o eroare la salvarea PDF-ului în baza de date
//                    Toast.makeText(getApplicationContext(), "Eroare la salvarea PDF-ului în baza de date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    public interface PdfUploadListener {
        void onPdfUploadedSuccess();
        void onPdfUploadedFailure(String errorMessage);
    }

    public void addPdfFile(String downloadUrl, String filename, PdfUploadListener listener) {
        // Crearea unei referințe către nodul "files" al utilizatorului specificat
        DatabaseReference userFilesDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getId()).child("files");

        // Actualizarea bazei de date cu URL-ul de descărcare și numele fișierului asociat
        userFilesDatabaseRef.child(filename).setValue(downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    // PDF-ul a fost salvat cu succes în baza de date
                    listener.onPdfUploadedSuccess();
                })
                .addOnFailureListener(e -> {
                    // A apărut o eroare la salvarea PDF-ului în baza de date
                    listener.onPdfUploadedFailure("Eroare la salvarea PDF-ului în baza de date: " + e.getMessage());
                });
    }




}
