package com.tallevi.petconnect;

import android.os.Parcel;
import android.os.Parcelable;

public class Pet implements Parcelable {
    private String name;
    private String imageUrl;
    private String description;
    private String phone;
    private String type;
    private String age;
    private String zone;
    private String gender;
    private String userId;  // Add this field

    public Pet(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    protected Pet(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        phone = in.readString();
        type = in.readString();
        age = in.readString();
        zone = in.readString();
        gender = in.readString();
        userId = in.readString();  // Read userId from parcel
    }

    public static final Creator<Pet> CREATOR = new Creator<Pet>() {
        @Override
        public Pet createFromParcel(Parcel in) {
            return new Pet(in);
        }

        @Override
        public Pet[] newArray(int size) {
            return new Pet[size];
        }
    };

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(imageUrl);
        parcel.writeString(description);
        parcel.writeString(phone);
        parcel.writeString(type);
        parcel.writeString(age);
        parcel.writeString(zone);
        parcel.writeString(gender);
        parcel.writeString(userId);  // Write userId to parcel
    }
}
