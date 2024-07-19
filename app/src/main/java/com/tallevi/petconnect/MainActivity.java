package com.tallevi.petconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    private static MainActivity instance; // Static reference to MainActivity instance

    FirebaseAuth auth;
    FirebaseUser user;

    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private List<Pet> petList;
    private Button uploadPhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize pet list
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList);
        recyclerView.setAdapter(petAdapter);

        // Load pets from Firebase Storage
        loadPetsFromStorage();
    }

    private void setUploadPhotoButtonState() {
        if (isLoggedIn) {
            uploadPhotoButton.setEnabled(true);
            uploadPhotoButton.setBackgroundColor(getResources().getColor(R.color.button_default_color)); // Replace with your default button color
        } else {
            uploadPhotoButton.setEnabled(false);
            uploadPhotoButton.setBackgroundColor(getResources().getColor(R.color.button_disabled_color)); // Replace with your disabled button color
        }
    }

    private void loadPetsFromStorage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images");
        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            String petName = storageMetadata.getCustomMetadata("pet_name");
                            String description = storageMetadata.getCustomMetadata("description");
                            String phone = storageMetadata.getCustomMetadata("phone");
                            String type = storageMetadata.getCustomMetadata("type");
                            String age = storageMetadata.getCustomMetadata("age");
                            String zone = storageMetadata.getCustomMetadata("zone");
                            String gender = storageMetadata.getCustomMetadata("gender");

                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    Log.d("MainActivity", "Image URL: " + imageUrl); // Log the imageUrl

                                    Pet pet = new Pet(petName, imageUrl);
                                    pet.setDescription(description);
                                    pet.setPhone(phone);
                                    pet.setType(type);
                                    pet.setAge(age);
                                    pet.setZone(zone);
                                    pet.setGender(gender);

                                    petList.add(pet);
                                    petAdapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("MainActivity", "Error getting download URL", e);
                                    Toast.makeText(MainActivity.this, "Error getting download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("MainActivity", "Error getting metadata", e);
                            Toast.makeText(MainActivity.this, "Error getting metadata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("MainActivity", "Error listing items", e);
                Toast.makeText(MainActivity.this, "Error listing items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        setIntent(intent); // Update the intent
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
}