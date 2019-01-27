package com.example.android.speaker_seeker.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class InfoWindowData {
    private User user;
    private HelpMessage helpMessage;

    public InfoWindowData(){
        // Default constructor required for calls to DataSnapshot.getValue(InfoWindowData.class)
    }

    public InfoWindowData(User user, HelpMessage helpMessage) {
        this.user = user;
        this.helpMessage = helpMessage;
    }


    public void setUser(User user) {
        this.user = user;
    }

    public void setHelpMessage(HelpMessage helpMessage) {
        this.helpMessage = helpMessage;
    }

    public User getUser() {

        return user;
    }

    public HelpMessage getHelpMessage() {
        return helpMessage;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("help_message", helpMessage);


        return result;

    }
}
