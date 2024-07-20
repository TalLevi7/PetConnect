package com.tallevi.petconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class PetBroadcastReceiver extends BroadcastReceiver {
    private PetAdapter petAdapter;
    private List<Pet> petList;

    public PetBroadcastReceiver(PetAdapter petAdapter, List<Pet> petList) {
        this.petAdapter = petAdapter;
        this.petList = petList;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        List<Pet> pets = intent.getParcelableArrayListExtra("pets");
        if (pets != null) {
            petList.clear();
            petList.addAll(pets);
            petAdapter.notifyDataSetChanged();
        }
    }
}
