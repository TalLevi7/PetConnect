package com.tallevi.petconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Spinner Type_spinner;
    private Spinner Age_spinner;
    private Spinner Location_spinner;
    private Button Send_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Type_spinner = findViewById(R.id.type_spinner);
        Age_spinner = findViewById(R.id.age_spinner);
        Location_spinner = findViewById(R.id.location_spinner);
        Send_button = findViewById(R.id.btn_save);

        Send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = Type_spinner.getSelectedItem().toString();
                String age = Age_spinner.getSelectedItem().toString();
                String location = Location_spinner.getSelectedItem().toString();

                // Send user to MainActivity with the filter preferences
                Toast.makeText(getApplicationContext(), "Preferences saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("filter_type", type);
                intent.putExtra("filter_age", age);
                intent.putExtra("filter_location", location);
                startActivity(intent);
            }
        });
    }

    protected void onPause() {
        super.onPause();
    }
}
