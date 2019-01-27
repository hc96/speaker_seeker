package com.example.android.speaker_seeker.models;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

public class LanguageChip implements ChipInterface {
    private String languageName;

    public LanguageChip(String languageName) {
        this.languageName = languageName;
    }

    @Override
    public Object getId() {
        return this.languageName;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return languageName;
    }

    @Override
    public String getInfo() {
        return null;
    }

}
