package com.tallevi.petconnect;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PetRetrievalService extends Service {
    private static final String TAG = "PetRetrievalService";

    private String filterType;
    private String filterAge;
    private String filterLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Started");

        // Get filter preferences from Intent
        filterType = intent.getStringExtra("filter_type");
        filterAge = intent.getStringExtra("filter_age");
        filterLocation = intent.getStringExtra("filter_location");

        new Thread(new Runnable() {
            @Override
            public void run() {
                retrievePetsFromFirebase();
            }
        }).start();
        return START_STICKY;
    }

    private void retrievePetsFromFirebase() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images");
        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                List<Pet> petList = new ArrayList<>();
                Set<String> uniqueLocations = new HashSet<>();
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
                            String userId = storageMetadata.getCustomMetadata("user_id");

                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    Log.d(TAG, "Image URL: " + imageUrl);

                                    Pet pet = new Pet(petName, imageUrl);
                                    pet.setDescription(description);
                                    pet.setPhone(phone);
                                    pet.setType(type);
                                    pet.setAge(age);
                                    pet.setZone(zone);
                                    pet.setGender(gender);
                                    pet.setUserId(userId);

                                    // Add unique location
                                    if (zone != null && !zone.isEmpty()) {
                                        uniqueLocations.add(zone);
                                        Log.d(TAG, "Added location: " + zone);
                                    }

                                    // Apply filters if any
                                    if (matchesFilter(pet)) {
                                        petList.add(pet);
                                    }
                                    sendPetsToMainActivity(petList);
                                    sendUniqueLocationsToSettings(new ArrayList<>(uniqueLocations));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error getting download URL", e);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error getting metadata", e);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error listing items", e);
            }
        });
    }

    private boolean matchesFilter(Pet pet) {
        boolean matches = true;
        Log.d(TAG, "Comparing pet type: " + pet.getType() + " with filter type: " + filterType);
        if (filterType != null && pet.getType() != null) {
            if (!filterType.isEmpty() && !filterType.equals("All") && !filterType.toLowerCase().contains(pet.getType().toLowerCase())) {
                matches = false;
            }
        }

        Log.d(TAG, "Comparing pet age: " + pet.getAge() + " with filter age: " + filterAge);
        if (filterAge != null && pet.getAge() != null && !filterAge.isEmpty() && !filterAge.equals("All ages")) {
            double petAge = Double.parseDouble(pet.getAge());
            if (filterAge.equals("puppy (0–1 years)")) {
                if (petAge >= 1)
                    matches = false;
            }
            if (filterAge.equals("adult (1–7 years)"))
            {
                if ((petAge < 1) || (petAge >= 7))
                    matches = false;
            }
            if (filterAge.equals("senior (7+ years)"))
            {
                if (petAge < 7)
                    matches = false;
            }
        }

        Log.d(TAG, "Comparing pet location: " + pet.getZone() + " with filter location: " + filterLocation);
        if (filterLocation != null && !filterLocation.isEmpty() && !filterLocation.equals("Any location") && !filterLocation.equalsIgnoreCase(pet.getZone())) {
            matches = false;
        }
        return matches;
    }

    private void sendPetsToMainActivity(List<Pet> petList) {
        Intent intent = new Intent("com.tallevi.petconnect.PET_DATA");
        intent.putParcelableArrayListExtra("pets", new ArrayList<>(petList));
        sendBroadcast(intent);
    }

    private void sendUniqueLocationsToSettings(List<String> uniqueLocations) {
        Log.d(TAG, "Broadcasting locations: " + uniqueLocations);

        // Remove duplicates
        Set<String> uniqueLocationsSet = new HashSet<>(uniqueLocations);
        List<String> uniqueLocationsList = new ArrayList<>(uniqueLocationsSet);

        // Save locations in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PetConnectPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray(uniqueLocationsList);
        editor.putString("unique_locations", jsonArray.toString());
        editor.apply();

        // Also send broadcast for any active receivers
        Intent intent = new Intent("com.tallevi.petconnect.LOCATION_DATA");
        intent.putStringArrayListExtra("locations", new ArrayList<>(uniqueLocationsList));
        sendBroadcast(intent);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}