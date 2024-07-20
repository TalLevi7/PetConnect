package com.tallevi.petconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PetDetailActivity extends AppCompatActivity {
    private ImageView petImageView;
    private TextView petNameTextView;
    private TextView petDescriptionTextView;
    private TextView petPhoneTextView;
    private TextView petTypeTextView;
    private TextView petAgeTextView;
    private TextView petZoneTextView;
    private TextView petGenderTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);

        petImageView = findViewById(R.id.pet_detail_image);
        petNameTextView = findViewById(R.id.pet_detail_name);
        petDescriptionTextView = findViewById(R.id.pet_detail_description);
        petPhoneTextView = findViewById(R.id.pet_detail_phone);
        petTypeTextView = findViewById(R.id.pet_detail_type);
        petAgeTextView = findViewById(R.id.pet_detail_age);
        petZoneTextView = findViewById(R.id.pet_detail_zone);
        petGenderTextView = findViewById(R.id.pet_detail_gender);

        // Get the Pet object from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("pet")) {
            Pet pet = intent.getParcelableExtra("pet");

            // Update the UI with the pet details
            if (pet != null) {
                Glide.with(this).load(pet.getImageUrl()).into(petImageView);
                petNameTextView.setText(pet.getName());
                petDescriptionTextView.setText(pet.getDescription());
                petPhoneTextView.setText(pet.getPhone());
                petTypeTextView.setText(pet.getType());
                petAgeTextView.setText(pet.getAge());
                petZoneTextView.setText(pet.getZone());
                petGenderTextView.setText(pet.getGender());
            }
        }
    }
}
