package com.example.eduscan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;

public class TextRecognitionActivity extends AppCompatActivity {

    //layout
    TextView backHome;
    ImageView goToProfile;
    Button buttonRecognizeText;
    Button buttonTakeImageCamera;
    Button buttonTakeImageGallery;

    private RecycleAdapter adapter;

    //tag
    private static final String TAG = "MAIN_TAG";


    //uri img from camera/gallery
    private Uri imageUri = null;
    private ArrayList<Uri> uriArrayList = new ArrayList<>(); // Lista de URI-uri pentru pozele selectate


    //handle camera permis
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 23;


    //arrays of perms required to pick from camera, gallery
    private String[] cameraPermissions;


    //progress dialog
    private ProgressDialog progressDialog;

    //text recognizer
    private TextRecognizer textRecognizer;

    //pt a construi textul recunoscut
    private StringBuilder recognizedTextBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);


        //init
        buttonRecognizeText = findViewById(R.id.buttonRecognizeText);
        buttonTakeImageCamera =  findViewById(R.id.buttonTakeImageCamera);
        buttonTakeImageGallery = findViewById(R.id.buttonTakeImageGallery);

        backHome = findViewById(R.id.backHome);
        goToProfile = findViewById(R.id.goToProfile);

        //init arrays of perms
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);


        //
        RecyclerView recyclerView = findViewById(R.id.recyclerView_Galley_Images);
        adapter = new RecycleAdapter(uriArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // Afișați pozele într-o grilă cu 3 coloane


        //basic stuff
        backHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TextRecognitionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TextRecognitionActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        buttonTakeImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkStoragePermission()){
                    pickImageGallery();
                }else {
                    requestStoragePermission();
                }
            }
        });

        buttonTakeImageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraPermissions()){
                    pickImageCamera();
                }else {
                    requestCameraPermissions();
                }
            }
        });


        buttonRecognizeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (uriArrayList == null){
                    Toast.makeText(TextRecognitionActivity.this, "Pick image first..", Toast.LENGTH_SHORT).show();
                }else {
                    showSaveOptionsDialog();
                }
            }
        });

    }

    ////////////////////////////////

    //take image from camera/gallery, else -> permisiuni

    //pick img camera
    private void pickImageCamera(){
        //get img ready for data
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description");
        //img uri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        if (imageUri != null) {
                            uriArrayList.add(imageUri); // Adăugare URI-ul imaginii în lista de URI-uri
                            adapter.notifyDataSetChanged(); // Actualizare RecyclerView pentru a afișa noua imagine
                            continueOrFinishCapturingImages(); // Afisează dialogul pentru a continua sau a termina capturarea imaginilor
                        }
                    } else {
                        Toast.makeText(TextRecognitionActivity.this, "Capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void continueOrFinishCapturingImages() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Continue capturing images?")
                .setMessage("Do you want to continue capturing images?")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Lansează din nou activitatea de capturare a imaginilor
                        pickImageCamera();
                    }
                })
                .setNegativeButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Continuă cu procesarea imaginilor existente sau cu altă acțiune
                        adapter.notifyDataSetChanged();
                    }
                })
                .setCancelable(false)
                .show();
    }



    //pick img gallery
    private void pickImageGallery(){

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permiterea selectării mai multor imagini
        galleryActivityResultLauncher.launch(intent);
    }


    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(

            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                uriArrayList.add(imageUri); // Adăugați URI-ul imaginii în lista de URI-uri
                            }
                        } else if (data != null && data.getData() != null) {
                            Uri imageUri = data.getData();
                            uriArrayList.add(imageUri); // Adăugați URI-ul imaginii în lista de URI-uri
                        }
                        adapter.notifyDataSetChanged(); // Notificați adapterul despre schimbările din lista de URI-uri
                    }
                }
            }
    );


    //permisiuni camera+gallery
    private boolean checkStoragePermission(){


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        }else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }

    }

    private void requestStoragePermission(){


        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){

                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                                }else{
                                    Toast.makeText(TextRecognitionActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //Below android 11

                            }
                        }
                    });

    private boolean checkCameraPermissions(){
        //check camera & storage perms allowed
        //true allowed, false not

        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return cameraResult&&storageResult;
    }


    private void requestCameraPermissions(){
        //request camera perms
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }


    //handle perms results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Verificăm ce permisiune a fost cerută
        if (requestCode == CAMERA_REQUEST_CODE) {
            // Dacă toate permisiunile cerute au fost acordate
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use camera.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0){
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if(read && write){
                    pickImageGallery();
                }else{
                    Toast.makeText(TextRecognitionActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }


    }


    ////////////////////////////////
    enum SaveFormat {
        PDF,
        WORD
    }



    // recognize text from image
    private void showSaveOptionsDialog() {
        final String[] saveOptions = {"Save as PDF", "Save as Word Document"};

        AlertDialog.Builder builder = new AlertDialog.Builder(TextRecognitionActivity.this);
        builder.setTitle("Choose save format");

        builder.setItems(saveOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SaveFormat selectedFormat = (which == 0) ? SaveFormat.PDF : SaveFormat.WORD;
                // Lansează dialogul pentru numele fișierului după ce se alege formatul
                showFileNameDialog(selectedFormat);
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showFileNameDialog(SaveFormat format) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File Name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString();
                if (!fileName.isEmpty()) {
                    progressDialog.setMessage("Recognizing text..");
                    progressDialog.show();
                    recognizeTextFromImages(uriArrayList, format, fileName);
                } else {
                    Toast.makeText(TextRecognitionActivity.this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
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

    private void recognizeTextFromImages(ArrayList<Uri> uriArrayList, SaveFormat format, String fileName) {
        for (Uri imgUri : uriArrayList) {
            recognizeTextFromImage(imgUri, format, fileName);
        }
    }

    private void recognizeTextFromImage(Uri imgUri, SaveFormat format, String fileName) {
        try {
            InputImage inputImage = InputImage.fromFilePath(this, imgUri);

            textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            updateRecognizedText(text, format, fileName, imgUri);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(TextRecognitionActivity.this, "Failed to recognize text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecognizedText(Text text, SaveFormat format, String fileName, Uri imgUri) {
        recognizedTextBuilder.append(text.getText());
        if (imgUri.equals(uriArrayList.get(uriArrayList.size() - 1))) {
            saveText(format, fileName);
        }
    }

    private void saveText(SaveFormat format, String filename) {
        String recognizedText = recognizedTextBuilder.toString();
        switch (format) {
            case PDF:
                saveTextAsPDF(recognizedText, filename);
                break;
            case WORD:
                saveTextAsWord(recognizedText, filename);
                break;
        }
    }



    // save as pdf / word
    private void saveTextAsWord(String recognizedText, String filename) {

        //create + save in files
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);

                // Aici creezi documentul Word folosind biblioteca aleasă
                // Exemplu simplificat cu docx4j (trebuie adaptat la biblioteca ta):

                WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
                MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
                mainDocumentPart.addParagraphOfText(recognizedText);
                wordPackage.save(outputStream);


                Toast.makeText(TextRecognitionActivity.this, "Word document saved to Downloads", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(TextRecognitionActivity.this, "Failed to save Word document", Toast.LENGTH_SHORT).show();
        }
    }



    private void saveTextAsPDF(String recognizedText, String filename) {



        //firebase
        // Convertiți textul recunoscut într-un șir de octeți

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            byte[] pdfBytes = null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph(recognizedText));
            document.close();
            pdfBytes = outputStream.toByteArray();

            // Încărcați șirul de octeți în Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference pdfRef = storageRef.child("files/").child(filename);
            pdfRef.putBytes(pdfBytes)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obțineți URL-ul de descărcare al PDF-ului încărcat
                        pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            // Apelați metoda addPdfFile din DatabaseConnection pentru a salva URL-ul de descărcare în baza de date
                            DatabaseConnection.getInstance().addPdfFile(downloadUrl, filename, new DatabaseConnection.PdfUploadListener() {
                                @Override
                                public void onPdfUploadedSuccess() {
                                    // PDF-ul a fost salvat cu succes în baza de date
                                    Toast.makeText(getApplicationContext(), "PDF-ul a fost salvat cu succes în baza de date.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPdfUploadedFailure(String errorMessage) {
                                    // A apărut o eroare la salvarea PDF-ului în baza de date
                                    Toast.makeText(TextRecognitionActivity.this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
                                }
                            });

                            progressDialog.dismiss();

                        });
                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();
                        Toast.makeText(TextRecognitionActivity.this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
                    });

        }else {
            Toast.makeText(getApplicationContext(), "Utilizatorul nu este autentificat.", Toast.LENGTH_SHORT).show();

        }





    }


}