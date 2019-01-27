package com.example.android.speaker_seeker.models;

import com.google.firebase.database.Exclude;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private String username;
    public String name;
    private String surname;
    private String gender;
    private String phone;
    private List<String> nativeLanguages;
    private List<String> otherLanguages;
    private String photo;
    private List<String> connections;
    private Timestamp lastOnline;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String surname, String photo) {
        this.name = name;
        this.surname = surname;
        this.photo = photo;
    }

    public User(String username, String name, String surname, String gender, String phone, List<String> nativeLanguages, List<String> otherLanguages, String photo) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.gender = gender;
        this.phone = phone;
        this.nativeLanguages = nativeLanguages;
        this.otherLanguages = otherLanguages;
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public List<String> getNativeLanguages() {
        return nativeLanguages;
    }

    public List<String> getOtherLanguages() {
        return otherLanguages;
    }

    public String getPhoto() {
        return photo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }



    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("name", name);
        result.put("surname", surname);
        result.put("gender", gender);
        result.put("phone", phone);
        result.put("nativeLanguages", nativeLanguages);
        result.put("otherLanguages", otherLanguages);
        result.put("photo", photo);

        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "photo='" + photo + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
