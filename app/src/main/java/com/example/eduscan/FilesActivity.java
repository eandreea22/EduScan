package com.example.eduscan;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFiles;
    private FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));

        // Inițializarea adaptorului și setarea RecyclerView-ului cu lista de fișiere
        fileAdapter = new FileAdapter(new ArrayList<>());
        recyclerViewFiles.setAdapter(fileAdapter);

        DatabaseConnection.getInstance().updateFiles(new DatabaseConnection.FilesUpdateListener() {
            @Override
            public void onFilesUpdated(ArrayList<FileModel> fileList) {

                fileAdapter.updateFiles(fileList);
                fileAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDatabaseError(DatabaseError databaseError) {
                // Tratarea erorilor de la baza de date
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });


    }
}