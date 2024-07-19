package com.tallevi.petconnect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UploadPhoto extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private StorageReference storageReference;
    private LinearProgressIndicator progressIndicator;
    private Uri image;
    private MaterialButton uploadImage, selectImage;
    private ImageView imageView;
    private EditText editPetName, editDescription, editPhone, editAge, editZone;
    private Spinner spinnerGender, spinnerType;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            uploadImage.setEnabled(true);
                            image = result.getData().getData();
                            // Load the selected image into ImageView
                            Glide.with(UploadPhoto.this)
                                    .load(image)
                                    .into(imageView);
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

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressIndicator = findViewById(R.id.progress);
        selectImage = findViewById(R.id.selectImage);
        uploadImage = findViewById(R.id.uploadImage);

        // Initialize ImageView
        imageView = findViewById(R.id.imageView);

        // Initialize EditText fields
        editPetName = findViewById(R.id.editPetName);
        editDescription = findViewById(R.id.editDescription);
        editPhone = findViewById(R.id.editPhone);
        editAge = findViewById(R.id.editAge);
        editZone = findViewById(R.id.editZone);

        // Initialize Spinners
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerType = findViewById(R.id.spinnerType);

        // Create an ArrayAdapter for Gender
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Create an ArrayAdapter for Type
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Check for permissions and request if necessary
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

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
                String type = spinnerType.getSelectedItem().toString().trim();
                String age = editAge.getText().toString().trim();
                String gender = spinnerGender.getSelectedItem().toString().trim();
                String zone = editZone.getText().toString().trim(); // Use the value from EditText

                // Validate age input
                if (!age.matches("\\d+")) { // Check if age contains only digits
                    Toast.makeText(UploadPhoto.this, "Please enter a valid age (numbers only)", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate inputs
                if (image != null && !petName.isEmpty() && !description.isEmpty() && !phone.isEmpty() && !zone.isEmpty()) {
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

        // Automatically set the zone field when the activity starts
        setCurrentZone();
    }

    private void setCurrentZone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Use the location to get the current zone (e.g., through reverse geocoding)
                                String zone = getZoneFromLocation(location);
                                editZone.setText(zone); // Set the zone in the EditText
                            } else {
                                editZone.setText("Unknown Zone"); // Fallback if location is null
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            editZone.setText("Unknown Zone"); // Fallback if permission is not granted
        }
    }

    private String getZoneFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getLocality(); // You can choose other address fields if needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Zone"; // Fallback if geocoding fails
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
