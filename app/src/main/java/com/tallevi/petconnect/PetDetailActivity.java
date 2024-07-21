package com.tallevi.petconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PetDetailActivity extends AppCompatActivity {
    private ImageView petImageView;
    private TextView petNameTextView;
    private TextView petDescriptionTextView;
    private TextView petTypeTextView;
    private TextView petAgeTextView;
    private TextView petZoneTextView;
    private TextView petGenderTextView;
    private Button deleteButton;
    private Button phoneButton;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        petImageView = findViewById(R.id.pet_detail_image);
        petNameTextView = findViewById(R.id.pet_detail_name);
        petDescriptionTextView = findViewById(R.id.pet_detail_description);
        petTypeTextView = findViewById(R.id.pet_detail_type);
        petAgeTextView = findViewById(R.id.pet_detail_age);
        petZoneTextView = findViewById(R.id.pet_detail_zone);
        petGenderTextView = findViewById(R.id.pet_detail_gender);
        deleteButton = findViewById(R.id.button_delete);
        phoneButton = findViewById(R.id.pet_detail_phone_button);

        // Get the Pet object from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("pet")) {
            Pet pet = intent.getParcelableExtra("pet");

            // Update the UI with the pet details
            if (pet != null) {
                Glide.with(this).load(pet.getImageUrl()).into(petImageView);
                petNameTextView.setText(pet.getName());
                petDescriptionTextView.setText(pet.getDescription());
                petTypeTextView.setText(pet.getType());

                double ageNumDouble = Double.parseDouble(pet.getAge());
                if (ageNumDouble < 1) {
                    ageNumDouble = ageNumDouble * 12;
                    int ageNumInt = (int) Math.round(ageNumDouble);
                    petAgeTextView.setText(String.valueOf(ageNumInt) + " months");
                } else {
                    int ageNumInt = (int) Math.round(ageNumDouble);
                    petAgeTextView.setText(String.valueOf(ageNumInt) + " years");
                }

                petZoneTextView.setText(pet.getZone());
                petGenderTextView.setText(pet.getGender());

                // Check if the current user is the owner of the pet post
                if (currentUser != null && currentUser.getUid().equals(pet.getUserId())) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else {
                    deleteButton.setVisibility(View.GONE);
                }

                // Handle delete button click
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletePet(pet.getImageUrl());
                    }
                });

                // Handle phone button click
                phoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + pet.getPhone()));
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void deletePet(String imageUrl) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PetDetailActivity.this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                // Navigate back to MainActivity
                Intent intent = new Intent(PetDetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(PetDetailActivity.this, "Failed to delete pet: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
