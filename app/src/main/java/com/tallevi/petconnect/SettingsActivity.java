package com.tallevi.petconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerPetType;
    private Spinner spinnerAge;
    private Spinner spinnerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinnerPetType = findViewById(R.id.spinnerPetType);
        spinnerAge = findViewById(R.id.spinnerAge);
        spinnerLocation = findViewById(R.id.spinnerLocation);

    }
    protected void onPause() {
        super.onPause();
    }
}