package com.tallevi.petconnect;

import android.app.Service;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

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
                                    Log.d(TAG, "Image URL: " + imageUrl);

                                    Pet pet = new Pet(petName, imageUrl);
                                    pet.setDescription(description);
                                    pet.setPhone(phone);
                                    pet.setType(type);
                                    pet.setAge(age);
                                    pet.setZone(zone);
                                    pet.setGender(gender);

                                    // Apply filters if any
                                    if (matchesFilter(pet)) {
                                        petList.add(pet);
                                    }

                                    sendPetsToMainActivity(petList);
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
        if (filterType != null && !filterType.isEmpty() && !filterType.equals("All") && !filterType.equals(pet.getType())) {
            matches = false;
            Log.d(TAG, "WILL BE FALSE!");
        }

        Log.d(TAG, "Comparing pet age: " + pet.getAge() + " with filter age: " + filterAge);
        if (filterAge != null && !filterAge.isEmpty() && !filterAge.equals("All ages") &&  !filterAge.equals(pet.getAge())) {
            matches = false;
            Log.d(TAG, "WILL BE FALSE!");
        }

        Log.d(TAG, "Comparing pet location: " + pet.getZone() + " with filter location: " + filterLocation);
        if (filterLocation != null && !filterLocation.isEmpty() && !filterLocation.equals("Any location") && !filterLocation.equals(pet.getZone())) {
            matches = false;
            Log.d(TAG, "WILL BE FALSE!");
        }
        if (matches){
            Log.d(TAG, "WILL BE TRUE!");
        }
        return matches;
    }


    private void sendPetsToMainActivity(List<Pet> petList) {
        Intent intent = new Intent("com.tallevi.petconnect.PET_DATA");
        intent.putParcelableArrayListExtra("pets", new ArrayList<>(petList));
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
