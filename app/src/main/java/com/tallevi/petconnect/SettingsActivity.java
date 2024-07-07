package com.tallevi.petconnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private Spinner Type_spinner;
    private Spinner Age_spinner;
    private Spinner Location_spinner;
    private Button Send_button;
    private static SettingsActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Type_spinner = findViewById(R.id.type_spinner);
        Age_spinner = findViewById(R.id.age_spinner);
        Location_spinner = findViewById(R.id.location_spinner);
        Send_button = findViewById(R.id.btn_save);
        instance = this;

        Send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View V)
            {
                int type = Type_spinner.getSelectedItemPosition();
                int age = Age_spinner.getSelectedItemPosition();
                int location = Location_spinner.getSelectedItemPosition();

//                we can use this function to see the value of each attribute selected, and then show
//                the relevant pets in the MainActivity
//                showDialog(type,age,location);

                // send user to MainActivity with the relevant pets
                Toast.makeText(getApplicationContext(), "Preferences saved",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
        );
    }

    private void showDialog(int type, int age, int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Check")
                .setMessage("Type: " + type + "\nAge: " + age+ "\nLocation: " + location )
                .setPositiveButton("OK", null);
        builder.create().show();
    }

    protected void onPause() {
        super.onPause();
    }
}