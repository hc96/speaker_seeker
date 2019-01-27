package com.example.android.speaker_seeker.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpMessage {

    private String content;
    private List<String> languages;
    private String date;

    public HelpMessage(){
        // Default constructor required for calls to DataSnapshot.getValue(HelpMessage.class)
    }

    public HelpMessage(String content, List<String> languages, String date){
        this.content = content;
        this.languages = languages;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public String getDate() {
        return date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("content", content);
        result.put("languages", languages);
        result.put("timestamp", date);

        return result;

    }
}
