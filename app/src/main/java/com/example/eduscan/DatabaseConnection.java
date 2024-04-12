package com.example.eduscan;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.net.Uri;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

//                                        user.setEmail(email);
//                                        user.setPassword(password);

                                        // Autentificarea a reușit, obțineți utilizatorul curent
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser != null) {

                                            // Salvare date utilizator în aplicație
                                            DatabaseReference reference = database.getReference("users");
                                            Query query = reference.orderByChild("email").equalTo(email);

                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()){

                                                        for (DataSnapshot userSnapshot : snapshot.getChildren()){
                                                            String userId = userSnapshot.getKey();
                                                            String name = userSnapshot.child("name").getValue(String.class);
                                                            String username = userSnapshot.child("username").getValue(String.class);

//                                                            user.setName(name);
//                                                            user.setUsername(username);

                                                            user = new User(name, username, email, password);
                                                            user.setId(userId);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    System.out.println("error");
                                                }
                                            });


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


    public interface PdfUploadListener {
        void onPdfUploadedSuccess();
        void onPdfUploadedFailure(String errorMessage);
    }

    public void addPdfFile(String downloadUrl, String filename, PdfUploadListener listener) {

        DatabaseReference userFilesDatabaseRef = database.getReference()
                .child("users")
                .child(user.getId())
                .child("files"); // Referința către nodul "files" al userului

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("nume", filename);
        fileData.put("adresa_url", downloadUrl);

        userFilesDatabaseRef.child(filename).setValue(fileData)
                .addOnSuccessListener(aVoid -> {
                    // Fișierul a fost salvat cu succes în baza de date
                    listener.onPdfUploadedSuccess();
                })
                .addOnFailureListener(e -> {
                    // A apărut o eroare la salvarea fișierului în baza de date
                    listener.onPdfUploadedFailure("Eroare la salvarea fișierului în baza de date: " + e.getMessage());
                });
    }

    public interface FilesUpdateListener {
        void onFilesUpdated(ArrayList<FileModel> fileList);
        void onDatabaseError(DatabaseError databaseError);
    }


    public void updateFiles(FilesUpdateListener listener){

        // Caută utilizatorul cu cheia specificată în baza de date
        DatabaseReference reference = database.getReference("users");
        Query query = reference.orderByKey().equalTo(user.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Obține lista de fișiere ale utilizatorului
                        DataSnapshot filesSnapshot = userSnapshot.child("files");
                        for (DataSnapshot fileSnapshot : filesSnapshot.getChildren()) {
                            String fileName = fileSnapshot.child("nume").getValue(String.class);
                            String filePath = fileSnapshot.child("adresa_url").getValue(String.class);
                            user.addFile(new FileModel(fileName, filePath));
                        }
                        listener.onFilesUpdated(user.getFiles());
                    }
                } else {
                    // Utilizatorul nu există în baza de date
                    // Poți trata această situație aici
                    Log.e(TAG, "UpdateFiles: key not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                listener.onDatabaseError(databaseError);
            }
        });
    }


    public interface DatabaseActionListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }


    public void editFileName(String oldName, String newName, DatabaseActionListener listener) throws IOException {

        // Creează o referință către fișierul din baza de date Firebase
        DatabaseReference fileRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getId())
                .child("files")
                .child(oldName);

        // Actualizează numele fișierului folosind metoda updateChildren()
        Map<String, Object> updates = new HashMap<>();
        updates.put("nume", newName);
        fileRef.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        listener.onSuccess();
                        Log.d(TAG, "File name updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // A apărut o eroare la actualizarea numelui fișierului
                        listener.onFailure("Error updating file name in Realtime Database");
                        Log.e(TAG, "Error updating file name: " + e.getMessage());
                    }
                });


        //

        // Obține referința către fișierul vechi din Firebase Storage
        StorageReference oldFileRef = FirebaseStorage.getInstance().getReference()
                .child("files/")
                .child(user.getId())
                .child(oldName);

        // Descarcă fișierul vechi într-un fișier temporar pe dispozitivul local
        File localFile = File.createTempFile("temp_file", /* suffix */ null);
        oldFileRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Descărcarea fișierului vechi a fost realizată cu succes

                        // Obține referința către noul fișier din Firebase Storage
                        StorageReference newFileRef = FirebaseStorage.getInstance().getReference()
                                .child("files")
                                .child(user.getId())
                                .child(newName);

                        // Încarcă fișierul descărcat înapoi pe Firebase Storage cu noul nume
                        newFileRef.putFile(Uri.fromFile(localFile))
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Încărcarea fișierului nou cu noul nume a fost realizată cu succes

                                        // Șterge fișierul vechi din Firebase Storage
                                        oldFileRef.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Ștergerea fișierului vechi a fost realizată cu succes
                                                        listener.onSuccess();
                                                        Log.d(TAG, "Old file deleted successfully");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // A apărut o eroare la ștergerea fișierului vechi
                                                        listener.onFailure("Error deleting old file");
                                                        Log.e(TAG, "Error deleting old file: " + e.getMessage());
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // A apărut o eroare la încărcarea fișierului nou cu noul nume
                                        listener.onFailure("Error updating file name in Storage Database");

                                        Log.e(TAG, "Error uploading new file: " + e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // A apărut o eroare la descărcarea fișierului vechi
                        Log.e(TAG, "Error downloading old file: " + e.getMessage());
                    }
                });


    }





}
