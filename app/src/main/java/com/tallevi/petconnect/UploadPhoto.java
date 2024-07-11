package com.tallevi.petconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class UploadPhoto extends AppCompatActivity {
    StorageReference storageReference;
    LinearProgressIndicator progressIndicator;
    Uri image;
    MaterialButton uploadImage, selectImage;
    ImageView imageView;
    EditText editPetName, editDescription, editPhone, editType, editAge, editZone;

    Spinner spinnerGender;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            uploadImage.setEnabled(true);
                            image = result.getData().getData();
//                            Glide.with(getApplicationContext()).load(image).into(imageView);
                        }
                    } else {
                        Toast.makeText(UploadPhoto.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        FirebaseApp.initializeApp(UploadPhoto.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressIndicator = findViewById(R.id.progress);
//        imageView = findViewById(R.id.imageView);
        selectImage = findViewById(R.id.selectImage);
        uploadImage = findViewById(R.id.uploadImage);
        MaterialButton backToMainScreen = findViewById(R.id.backToMainScreen);

        // Initialize EditText fields
        editPetName = findViewById(R.id.editPetName);
        editDescription = findViewById(R.id.editDescription);
        editPhone = findViewById(R.id.editPhone);
        editType = findViewById(R.id.editType);
        editAge = findViewById(R.id.editAge);
        editZone = findViewById(R.id.editZone);
        spinnerGender = findViewById(R.id.spinnerGender);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        spinnerGender.setAdapter(adapter);
        // Set click listener for back to main screen button
        backToMainScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(UploadPhoto.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get text input values
                String petName = editPetName.getText().toString().trim();
                String description = editDescription.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String type = editType.getText().toString().trim();
                String age = editAge.getText().toString().trim();
                String zone = editZone.getText().toString().trim();
                String gender = spinnerGender.getSelectedItem().toString().trim();
                // Validate inputs
                if (image != null && !petName.isEmpty() && !description.isEmpty() && !phone.isEmpty()) {
                    // Create metadata for the image
                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setCustomMetadata("pet_name", petName)
                            .setCustomMetadata("description", description)
                            .setCustomMetadata("phone", phone)
                            .setCustomMetadata("type", type)
                            .setCustomMetadata("age", age)
                            .setCustomMetadata("zone", zone)
                            .setCustomMetadata("gender", gender)
                            .build();

                    // Call uploadImage with image URI and metadata
                    uploadImage(image, metadata);
                } else {
                    Toast.makeText(UploadPhoto.this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImage(Uri file, StorageMetadata metadata) {
        // Generate a unique filename for the image
        String imageName = UUID.randomUUID().toString();

        // Create a reference to the image location in Firebase Storage
        StorageReference ref = storageReference.child("images/" + imageName);

        // Upload the file with metadata
        UploadTask uploadTask = ref.putFile(file, metadata);

        // Monitor the upload process
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(UploadPhoto.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                // Navigate back to MainActivity
                Intent intent = new Intent(UploadPhoto.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadPhoto.this, "Failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                progressIndicator.setMax(Math.toIntExact(taskSnapshot.getTotalByteCount()));
                progressIndicator.setProgress(Math.toIntExact(taskSnapshot.getBytesTransferred()));
            }
        });
    }
}
