package com.example.eduscan;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity implements FileAdapter.SelectionChangeListener{

    private RecyclerView recyclerViewFiles;
    private FileAdapter fileAdapter;
    private ImageView goToProfile;
    private ImageView editFile;
    private ImageView deleteFile;
    private ImageView shareFile;
    private ImageView viewFile;
    private ImageView downloadFile;

    //
    private View.OnClickListener viewFileClickListener;
    private View.OnTouchListener viewFileTouchListener;
    private View.OnClickListener editFileClickListener;
    private View.OnTouchListener editFileTouchListener;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        //

        //
        goToProfile = findViewById(R.id.goToProfile);
        editFile = findViewById(R.id.editFile);
        deleteFile = findViewById(R.id.deleteFile);
        shareFile = findViewById(R.id.shareFile);
        viewFile = findViewById(R.id.viewFile);
        downloadFile = findViewById(R.id.downloadFile);

        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));

        // Inițializarea adaptorului și setarea RecyclerView-ului cu lista de fișiere
        fileAdapter = new FileAdapter(new ArrayList<>());
        fileAdapter.setSelectionChangeListener(this);
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

        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FilesActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        //
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        editFileTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(70);
                }
                return false;
            }
        };

        viewFileTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(70);
                }
                return false;
            }
        };

        deleteFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(70);
                }
                return false;
            }


        });

        downloadFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(70);
                }
                return false;
            }


        });

        shareFile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(70);
                }
                return false;
            }


        });


        //
        editFileClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Afișează un dialog de progres pentru a întreba utilizatorul
                ProgressDialog progressDialog = new ProgressDialog(FilesActivity.this);
                progressDialog.setTitle("Rename File");
                progressDialog.setMessage("Do you want to rename this file?");
                progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Afișează un dialog de introducere a textului pentru a introduce noul nume al fișierului
                        AlertDialog.Builder builder = new AlertDialog.Builder(FilesActivity.this);
                        builder.setTitle("Enter New Name");
                        final EditText input = new EditText(FilesActivity.this);
                        builder.setView(input);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = input.getText().toString();

                                // Actualizează numele fișierului în ArrayList - User

                                String fileName = fileAdapter.getSelectedFiles().get(0);
                                DatabaseConnection.getInstance().getUser().editFileName(fileName, newName);

                                fileAdapter.updateFiles(DatabaseConnection.getInstance().getUser().getFiles());
                                fileAdapter.notifyDataSetChanged();

                                // Actualizează numele fișierului în Firebase Realtime Database și Firebase Storage
                                try {
                                    DatabaseConnection.getInstance().editFileName(fileName, newName, new DatabaseConnection.DatabaseActionListener() {

                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(FilesActivity.this, "Done", Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            // Operația în Realtime Database a eșuat
                                            // Afișează sau gestionează mesajul de eroare
                                            Toast.makeText(FilesActivity.this, "error ", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                progressDialog.show();
            }
        };

        viewFileClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acțiunea care trebuie luată atunci când viewFile este apăsat
            }
        };


    }

    @Override
    public void onSelectionChanged(int numSelected) {
        // Verifică numărul de fișiere selectate și actualizează opacitatea icoanei în consecință
        if (numSelected > 1) {
            editFile.setAlpha(0.5f); // Setează opacitatea la jumătate
            viewFile.setAlpha(0.5f);

            // Dezactivează click-ul pe imaginile editFile și viewFile
            editFile.setOnClickListener(null);
            viewFile.setOnClickListener(null);
            editFile.setOnTouchListener(null);
            viewFile.setOnTouchListener(null);

        } else {
            editFile.setAlpha(1.0f); // Resetare opacitate la normal
            viewFile.setAlpha(1.0f);

            // Re-activează click-ul pe imaginile editFile și viewFile
            editFile.setOnClickListener(editFileClickListener);
            viewFile.setOnClickListener(viewFileClickListener);

            editFile.setOnTouchListener(editFileTouchListener);
            viewFile.setOnTouchListener(viewFileTouchListener);
        }
    }
}