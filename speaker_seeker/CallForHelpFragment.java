package com.example.android.speaker_seeker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.android.speaker_seeker.models.HelpMessage;
import com.example.android.speaker_seeker.models.LanguageChip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallForHelpFragment extends Fragment {
    private EditText mHelpMessageEditText;
    private ChipsInput mLanguagesChipsInput;
    private List<LanguageChip> mLanguagesList;
    private Button mSendHelpMessageButton;
    private FirebaseAuth mAuthentication;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private ScrollView mScrollView;

    private static final String TAG = CallForHelpFragment.class.getName();
    private static final String CONTENT_REQUIRED = "Help message cannot be empty";

    public CallForHelpFragment() {
        // Required empty public constructor
    }

    public static CallForHelpFragment newInstance(String info) {
        Log.d(TAG,"Fragment is instantiated");
        Bundle args = new Bundle();
        CallForHelpFragment fragment = new CallForHelpFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_for_help,container,false);

        mHelpMessageEditText = (EditText) view.findViewById(R.id.et_add_help_message);
        mLanguagesChipsInput = (ChipsInput)view.findViewById(R.id.chips_input_help_languages);
        mSendHelpMessageButton = (Button) view.findViewById(R.id.btn_send);
        mLanguagesList = new ArrayList<>();
        mScrollView = (ScrollView) view.findViewById(R.id.sv_add_help_message);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuthentication = FirebaseAuth.getInstance();


        mSendHelpMessageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                HelpMessage helpMessage = createHelpMessage();
                writeHelpMessageToDatabase(helpMessage);
                Toast.makeText(getContext(),R.string.status_help_message_sent, Toast.LENGTH_SHORT).show();
                clearHelpMessageDetails();
            }
        });

        mLanguagesChipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                Log.d(TAG, "chip added, " + newSize);
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                Log.d(TAG, "chip removed, " + newSize);
            }

            @Override
            public void onTextChanged(CharSequence text) {
                int nY_Pos = mSendHelpMessageButton.getBottom();
                mScrollView.scrollTo(0,nY_Pos);
                Log.d(TAG, "text changed: " + text.toString());
            }
        });
        getLanguagesList();

        return view;
    }

    private void getLanguagesList() {

        for (String language : getResources().getStringArray(R.array.list_languages)) {
            LanguageChip languageChip = new LanguageChip(language);
            mLanguagesList.add(languageChip);
        }
        mLanguagesChipsInput.setFilterableList(mLanguagesList);
    }

    private HelpMessage createHelpMessage() {
        mFirebaseUser = mAuthentication.getCurrentUser();
        if (mFirebaseUser != null){
            String content = mHelpMessageEditText.getText().toString();
            List<LanguageChip> languagesSelected = (List<LanguageChip>) mLanguagesChipsInput.getSelectedChipList();
            List<String> languages = new ArrayList<>();
            for (LanguageChip language : languagesSelected) {
                languages.add(language.getLabel());
            }

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date currentDate = new Date(System.currentTimeMillis());
            String date = formatter.format(currentDate);

            if (TextUtils.isEmpty(content)) {
                mHelpMessageEditText.setError(CONTENT_REQUIRED);
                return null;
            }

            HelpMessage helpMessage = new HelpMessage(content, languages, date);

            Log.d(TAG, "Call for help is created");

            return helpMessage;

        } else
            Log.d(TAG, "Error while fetching user from database");
            return null;
    }


    private void writeHelpMessageToDatabase (HelpMessage helpMessage){
        String userID = mFirebaseUser.getEmail().replace(".", ",");
        //mDatabaseReference.child("help_messages").child(userID).push().setValue(helpMessage);
        mDatabaseReference.child("help_messages").child(userID).setValue(helpMessage);
    }

    private void clearHelpMessageDetails(){
        ArrayList<String> removeLanguageLabels = new ArrayList<>();
        mHelpMessageEditText.getText().clear();
        List<LanguageChip> languagesSelected = (List<LanguageChip>) mLanguagesChipsInput.getSelectedChipList();
        for (LanguageChip language : languagesSelected) {
            removeLanguageLabels.add(language.getLabel());
        }
        for (String id : removeLanguageLabels){
            mLanguagesChipsInput.removeChipByLabel(id);
        }
    }

}
