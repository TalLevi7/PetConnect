package com.tallevi.petconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Spinner Type_spinner;
    private Spinner Age_spinner;
    private Spinner Location_spinner;
    private Button Send_button;

    private LocationBroadcastReceiver locationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: SettingsActivity started");
        setContentView(R.layout.activity_settings);

        Type_spinner = findViewById(R.id.type_spinner);
        Age_spinner = findViewById(R.id.age_spinner);
        Location_spinner = findViewById(R.id.location_spinner);
        Send_button = findViewById(R.id.btn_save);

        // Set default values for Location_spinner
        ArrayAdapter<CharSequence> defaultAdapter = ArrayAdapter.createFromResource(this,
                R.array.locations, android.R.layout.simple_spinner_item);
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Location_spinner.setAdapter(defaultAdapter);

        // Load preferences from SharedPreferences
        loadPreferences();

        Send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = Type_spinner.getSelectedItem().toString();
                String age = Age_spinner.getSelectedItem().toString();
                String location = Location_spinner.getSelectedItem().toString();

                // Save user preferences in SharedPreferences
                savePreferences(type, age, location);

                // Send user to MainActivity with the filter preferences
                Toast.makeText(getApplicationContext(), "Preferences saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("filter_type", type);
                intent.putExtra("filter_age", age);
                intent.putExtra("filter_location", location);
                startActivity(intent);
            }
        });

        // Register broadcast receiver for unique locations
        locationBroadcastReceiver = new LocationBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.tallevi.petconnect.LOCATION_DATA");
        registerReceiver(locationBroadcastReceiver, filter);
        Log.d(TAG, "onCreate: Broadcast receiver registered");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SettingsActivity destroyed");
        unregisterReceiver(locationBroadcastReceiver);
        Log.d(TAG, "onDestroy: Broadcast receiver unregistered");
    }

    private void savePreferences(String type, String age, String location) {
        SharedPreferences sharedPreferences = getSharedPreferences("PetConnectPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("filter_type", type);
        editor.putString("filter_age", age);
        editor.putString("filter_location", location);
        editor.apply();
        Log.d(TAG, "Preferences saved: type=" + type + ", age=" + age + ", location=" + location);
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("PetConnectPrefs", MODE_PRIVATE);
        String type = sharedPreferences.getString("filter_type", "All");
        String age = sharedPreferences.getString("filter_age", "All ages");
        String location = sharedPreferences.getString("filter_location", "Any location");

        setSpinnerSelection(Type_spinner, type);
        setSpinnerSelection(Age_spinner, age);
        setSpinnerSelection(Location_spinner, location);

        // Load locations from SharedPreferences
        String json = sharedPreferences.getString("unique_locations", null);
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                Set<String> uniqueLocationsSet = new HashSet<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    uniqueLocationsSet.add(jsonArray.getString(i));
                }
                List<String> uniqueLocationsList = new ArrayList<>(uniqueLocationsSet);
                uniqueLocationsList.add(0, "Any location");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, uniqueLocationsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Location_spinner.setAdapter(adapter);
                Log.d(TAG, "Location spinner updated with stored locations");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        spinner.setSelection(position);
    }

    private class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Broadcast received");
            ArrayList<String> locations = intent.getStringArrayListExtra("locations");
            Log.d(TAG, "Received locations: " + locations);
            if (locations != null) {
                Set<String> uniqueLocationsSet = new HashSet<>(locations);
                List<String> uniqueLocationsList = new ArrayList<>(uniqueLocationsSet);
                uniqueLocationsList.add(0, "Any location");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_spinner_item, uniqueLocationsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Location_spinner.setAdapter(adapter);
                Log.d(TAG, "Location spinner updated with new locations");

                // Save the new locations in SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("PetConnectPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                JSONArray jsonArray = new JSONArray(uniqueLocationsList);
                editor.putString("unique_locations", jsonArray.toString());
                editor.apply();
                Log.d(TAG, "New locations saved in SharedPreferences");
            } else {
                Log.d(TAG, "Received locations is null");
            }
        }
    }
}
