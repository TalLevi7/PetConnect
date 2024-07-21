package com.tallevi.petconnect;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static boolean isLoggedIn = false;
    private MenuItem loginlogoutBtn;
    private static MainActivity instance;

    FirebaseAuth auth;
    FirebaseUser user;

    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private List<Pet> petList;
    private Button uploadPhotoButton;
    private PetBroadcastReceiver petBroadcastReceiver;

    private String filterType;
    private String filterAge;
    private String filterLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get filter preferences from Intent
        Intent intent = getIntent();
        filterType = intent.getStringExtra("filter_type");
        filterAge = intent.getStringExtra("filter_age");
        filterLocation = intent.getStringExtra("filter_location");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            isLoggedIn = true;
        }
        instance = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateLoginButton();

        uploadPhotoButton = findViewById(R.id.button_upload_photo);
        setUploadPhotoButtonState();

        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoggedIn) {
                    Intent intent = new Intent(MainActivity.this, UploadPhoto.class);
                    startActivity(intent);
                } else {
                    showAlertMessage("You must log in to post new pet");
                }
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList);
        recyclerView.setAdapter(petAdapter);

        petBroadcastReceiver = new PetBroadcastReceiver(petAdapter, petList);
        IntentFilter filter = new IntentFilter("com.tallevi.petconnect.PET_DATA");
        registerReceiver(petBroadcastReceiver, filter);

        startPetRetrievalService();
    }

    private void startPetRetrievalService() {
        Intent serviceIntent = new Intent(this, PetRetrievalService.class);
        serviceIntent.putExtra("filter_type", filterType);
        serviceIntent.putExtra("filter_age", filterAge);
        serviceIntent.putExtra("filter_location", filterLocation);
        startService(serviceIntent);
    }

    private void setUploadPhotoButtonState() {
        if (isLoggedIn) {
            uploadPhotoButton.setEnabled(true);
            uploadPhotoButton.setBackgroundColor(getResources().getColor(R.color.button_default_color));
        } else {
            uploadPhotoButton.setEnabled(false);
            uploadPhotoButton.setBackgroundColor(getResources().getColor(R.color.button_disabled_color));
        }
    }

    public static boolean isUserLoggedIn() {
        return isLoggedIn;
    }

    public static void setUserLoggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        if (instance != null) {
            instance.updateLoginButton();
            instance.setUploadPhotoButtonState();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        updateLoginButton();
        setUploadPhotoButtonState();
    }

    private void updateLoginButton() {
        if (loginlogoutBtn != null) {
            if (!isLoggedIn) {
                loginlogoutBtn.setTitle("Login");
            } else {
                loginlogoutBtn.setTitle("Logout");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        loginlogoutBtn = menu.findItem(R.id.btnLoginLogout);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (loginlogoutBtn != null) {
            if (!isLoggedIn) {
                loginlogoutBtn.setTitle("Login");
            } else {
                loginlogoutBtn.setTitle("Logout");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btnLoginLogout) {
            if (isLoggedIn) {
                auth.signOut();
                setUserLoggedIn(false);
                updateLoginButton();
                Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            updateLoginButton();
            setUploadPhotoButtonState();
            return true;
        }
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_exit) {
            showExitDialog();
            return true;
        }
        return true;
    }

    private void showAboutDialog() {
        String appName = "PetConnect";
        String osDetails = "Android " + Build.VERSION.RELEASE;
        String submitters = "names"; // Add actual names
        String submissionDate = "21.07.2024"; // Update accordingly

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About")
                .setMessage("App Name: " + appName + "\nVersion: " + "\nOS: " + osDetails +
                        "\nSubmitters: " + submitters + "\nSubmission Date: " + submissionDate)
                .setPositiveButton("OK", null);
        builder.create().show();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null);
        builder.create().show();
    }

    private void showAlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert")
                .setMessage(message)
                .setPositiveButton("OK", null);
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(petBroadcastReceiver);
    }
}
