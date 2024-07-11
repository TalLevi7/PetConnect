package com.tallevi.petconnect;

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

public class MainActivity extends AppCompatActivity {
    public static boolean isLoggedIn = false;
    private MenuItem loginlogoutBtn;
    private static MainActivity instance; // Static reference to MainActivity instance

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user!= null)
        {
            isLoggedIn = true;
        }
        instance = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateLoginButton();
        Button uploadPhotoButton = findViewById(R.id.button_upload_photo);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadPhoto.class);
                startActivity(intent);
            }
        });
    }
    public static boolean isUserLoggedIn() {
        return isLoggedIn;
    }

    public static void setUserLoggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        if (instance != null)
            instance.updateLoginButton();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the intent
        updateLoginButton();
    }
    private void updateLoginButton() {
        if (loginlogoutBtn != null)
        {
            if (!isLoggedIn)
                loginlogoutBtn.setTitle("Login");
            else
                loginlogoutBtn.setTitle("Logout");
        }
        //showDialog("isLoggedIn:  " + isLoggedIn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        loginlogoutBtn = menu.findItem(R.id.btnLoginLogout);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        // Change the title here if needed
        if (loginlogoutBtn != null) {
            if (!isLoggedIn)
                loginlogoutBtn.setTitle("Login");
            else
                loginlogoutBtn.setTitle("Logout");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
            if (id == R.id.btnLoginLogout)
            {
                if (isLoggedIn) {
                    auth.signOut();
                    setUserLoggedIn(false);
                    updateLoginButton();
                    Toast.makeText(getApplicationContext(), "Logout Successful",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Redirect to login activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                updateLoginButton();
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
        //String appVersion = BuildConfig.VERSION_NAME;
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

    private void showDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Check")
                .setMessage(s)
                .setPositiveButton("OK", null);
        builder.create().show();
    }
}