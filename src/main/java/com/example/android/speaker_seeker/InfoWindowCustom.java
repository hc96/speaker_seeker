package com.example.android.speaker_seeker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.speaker_seeker.models.InfoWindowData;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowCustom implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private LayoutInflater inflater;

    public InfoWindowCustom(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {

        inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.info_window_custom, null);

        TextView usernameTextView = view.findViewById(R.id.tv_iw_username);
        TextView genderTextView = view.findViewById(R.id.tv_iw_gender);
        ImageView photoImageView = view.findViewById(R.id.iv_iw_user_photo);
        TextView nativeLanguagesTextView = view.findViewById(R.id.tv_iw_languages_native);
        TextView otherLanguagesTextView = view.findViewById(R.id.tv_iw_languages_other);

        TextView helpMessageTextView = view.findViewById(R.id.tv_iw_help_message);
        TextView neededLanguagesTextView = view.findViewById(R.id.tv_iw_languages_needed);
        TextView timestampTextView = view.findViewById(R.id.tv_iw_timestamp);

        Button helpButton = view.findViewById(R.id.btn_iw_help);

        final InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        if (infoWindowData.getUser().getName() != null && infoWindowData.getUser().getSurname() != null) {
            usernameTextView.setText(infoWindowData.getUser().getName() + " " + infoWindowData.getUser().getSurname());
        }
        if (infoWindowData.getUser().getGender() != null) {
            genderTextView.setText(infoWindowData.getUser().getGender());
        }

        if (infoWindowData.getUser().getNativeLanguages() != null) {
            nativeLanguagesTextView.setText(infoWindowData.getUser().getNativeLanguages().toString().replace("[", "").replace("]", ""));
        }
        if (infoWindowData.getUser().getOtherLanguages() != null) {
            otherLanguagesTextView.setText(infoWindowData.getUser().getOtherLanguages().toString().replace("[", "").replace("]", ""));
        }

        if (infoWindowData.getHelpMessage().getContent() != null) {
            helpMessageTextView.setText(infoWindowData.getHelpMessage().getContent());
        }
        if (infoWindowData.getHelpMessage().getLanguages() != null) {
            neededLanguagesTextView.setText(infoWindowData.getHelpMessage().getLanguages().toString().replace("[", "").replace("]", ""));
        }
        if (infoWindowData.getHelpMessage().getDate() != null) {
            timestampTextView.setText(infoWindowData.getHelpMessage().getDate());
        }

        if (infoWindowData.getUser().getPhoto() != null) {
            byte[] decodedByteArray = android.util.Base64.decode(infoWindowData.getUser().getPhoto(), Base64.DEFAULT);
            Bitmap photoBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            photoImageView.setImageBitmap(photoBitmap);
        }

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
