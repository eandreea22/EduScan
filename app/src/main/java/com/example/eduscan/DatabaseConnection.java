package com.example.eduscan;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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


                        // Dacă adresa de email este găsită, folosește credențialele pentru autentificare
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {


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

                                                            user = new User(name, email, password);
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
                                        listener.onUserSaveFailed("Please enter the correct email and password ");
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

                        ArrayList<FileModel> filesFromDatabase = new ArrayList<>();

                        //luam fisierele din baza de date

                        DataSnapshot filesSnapshot = userSnapshot.child("files");
                        for (DataSnapshot fileSnapshot : filesSnapshot.getChildren()) {
                            String fileName = fileSnapshot.child("nume").getValue(String.class);
                            String filePath = fileSnapshot.child("adresa_url").getValue(String.class);
                            filesFromDatabase.add(new FileModel(fileName, filePath));
                        }


                        //verificam daca s au facut schimbari
                        if (!user.getFiles().equals(filesFromDatabase)){

                            ArrayList<FileModel> newFiles = new ArrayList<>();

                            for (FileModel newFile : filesFromDatabase) {

                                if (!user.getFiles().contains(newFile)) {
                                    newFiles.add(newFile);

                                }
                            }

                            for (FileModel file : newFiles){
                                user.addFile(file);
                            }
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


    public void editFileNameRealtime(String oldName, String newName, DatabaseActionListener listener){

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

                        DatabaseReference userFilesRef = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(user.getId())
                                .child("files");

                        // Verifică dacă există fișierul cu cheia veche (oldName)
                        userFilesRef.child(oldName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Obține datele fișierului existent
                                    Object fileData = dataSnapshot.getValue();

                                    // Șterge fișierul vechi cu cheia veche
                                    userFilesRef.child(oldName).removeValue();

                                    // Adaugă fișierul cu cheia nouă și datele actualizate
                                    userFilesRef.child(newName).setValue(fileData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Actualizarea cheii și numele fișierului s-a efectuat cu succes
                                                    listener.onSuccess();
                                                    Log.d(TAG, "File name and key updated successfully");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // A apărut o eroare la actualizarea cheii și numele fișierului
                                                    listener.onFailure("Error updating file name and key in Realtime Database");
                                                    Log.e(TAG, "Error updating file name and key: " + e.getMessage());
                                                }
                                            });
                                } else {
                                    // Fișierul cu cheia veche nu există în baza de date
                                    listener.onFailure("File with old key does not exist in Realtime Database");
                                    Log.e(TAG, "File with old key does not exist in Realtime Database");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // A apărut o eroare în timpul accesării datelor din baza de date
                                listener.onFailure("Error accessing file data in Realtime Database");
                                Log.e(TAG, "Error accessing file data: " + databaseError.getMessage());
                            }
                        });

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

    }


    public void editFileNameStorage(String oldName, String newName, DatabaseActionListener listener) {
        // Obține referința către fișierul vechi din Firebase Storage
        StorageReference oldFileRef = FirebaseStorage.getInstance().getReference()
                .child("files/")
                .child(oldName + ".pdf");

        // Descarcă fișierul vechi într-un fișier temporar pe dispozitivul local
        File localFile;
        try {
            localFile = File.createTempFile("temp_file", "*");
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFailure("Error creating temporary file");
            return;
        }

        oldFileRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    // Descărcarea fișierului vechi a fost realizată cu succes

                    // Obține referința către noul fișier din Firebase Storage
                    StorageReference newFileRef = FirebaseStorage.getInstance().getReference()
                            .child("files/")
                            .child(newName);

                    // Încarcă fișierul descărcat înapoi pe Firebase Storage cu noul nume
                    newFileRef.putFile(Uri.fromFile(localFile))
                            .addOnSuccessListener(taskSnapshot1 -> {
                                // Încărcarea fișierului nou cu noul nume a fost realizată cu succes

                                // Șterge fișierul vechi din Firebase Storage
                                oldFileRef.delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Ștergerea fișierului vechi a fost realizată cu succes
                                            listener.onSuccess();
                                            Log.d(TAG, "Old file deleted successfully");
                                        })
                                        .addOnFailureListener(e -> {
                                            // A apărut o eroare la ștergerea fișierului vechi
                                            listener.onFailure("Error deleting old file");
                                            Log.e(TAG, "Error deleting old file: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                // A apărut o eroare la încărcarea fișierului nou cu noul nume
                                listener.onFailure("Error uploading new file");
                                Log.e(TAG, "Error uploading new file: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    // A apărut o eroare la descărcarea fișierului vechi
                    listener.onFailure("Error downloading old file");
                    Log.e(TAG, "Error downloading old file: " + e.getMessage());
                });
    }


    public void downloadFiles(Context context, ArrayList<String> fileNames, DatabaseActionListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        AtomicInteger filesDownloaded = new AtomicInteger(0);
        int numFiles = fileNames.size();

        for (String fileName : fileNames) {
            StorageReference storageRef = storage.getReference().child("files/" + fileName);
            File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName + ".pdf");

            storageRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        int downloaded = filesDownloaded.incrementAndGet();
                        if (downloaded == numFiles) {
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(exception -> {
                        listener.onFailure("Failed to download file " + fileName);
                    });
        }
    }



    ///
    public void deleteFilesStorage(ArrayList<String> files, DatabaseActionListener listener){

        FirebaseStorage storage = FirebaseStorage.getInstance();

        AtomicInteger filesDeleted = new AtomicInteger(0);
        int numFiles = files.size();

        for (String fileName: files){
            StorageReference fileRef = storage.getReference().child("files/" + fileName);

            fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    int deleted = filesDeleted.incrementAndGet();
                    if (deleted == numFiles) {
                        listener.onSuccess();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onFailure("Error deleting file " + e);
                }
            });
        }

    }

    public void deleteFilesRealtime(ArrayList<String> files, DatabaseActionListener listener){


        DatabaseReference databaseRef = database.getReference().child("users").child(user.getId()).child("files"); // Înlocuiți "userId" cu id-ul utilizatorului corespunzător

        AtomicInteger filesDeleted = new AtomicInteger(0);

        for (String fileName : files) {
            databaseRef.child(fileName).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        int count = filesDeleted.incrementAndGet();
                        Log.d(TAG, "File deleted successfully: " + fileName);
                        if (count == files.size()) {
                            // Toate fișierele au fost șterse
                            Log.d(TAG, "All files deleted successfully Realtime");
                            listener.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Realtime Error deleting file " + fileName + ": " + e.getMessage());
                        listener.onFailure("Error deleting file " + e);
                    });
        }

    }

    // Definește interfața pentru ascultătorul acțiunilor din bază de date
    public interface MultipleFileUrlListener {
        void onMultipleFileUrlsReceived(ArrayList<String> fileUrls);
    }

    public void getUrlForMultipleFiles(ArrayList<String> fileNames, MultipleFileUrlListener listener) {

        DatabaseReference filesReference = database.getReference().child("users").child(user.getId()).child("files");

        ArrayList<String> fileUrls = new ArrayList<>();

        // Iterează prin fiecare nume de fișier și obține URL-ul corespunzător
        for (String fileName : fileNames) {
            filesReference.child(fileName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fileUrl = dataSnapshot.child("adresa_url").getValue(String.class);
                        fileUrls.add(fileUrl);

                        // Dacă am obținut URL-ul pentru toate fișierele, trimite lista către activitate
                        if (fileUrls.size() == fileNames.size()) {
                            listener.onMultipleFileUrlsReceived(fileUrls);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Tratează cazul în care citirea din baza de date nu reușește
                    Log.d("Firebase Error", databaseError.getMessage());
                }
            });
        }
    }







}
