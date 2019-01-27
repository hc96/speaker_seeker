package com.example.android.speaker_seeker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.android.speaker_seeker.models.HelpMessage;
import com.example.android.speaker_seeker.models.LanguageChip;
import com.example.android.speaker_seeker.models.User;
import com.example.android.speaker_seeker.models.UserLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CreateUserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuthentication;
    private DatabaseReference mDatabaseReference;
    private Button mSaveButton;
    private ImageButton mEditUserPhotoImageButton;
    private ImageView mEditUserPhotoImageView;
    private ChipsInput mNativeLanguagesChipsInput;
    private ChipsInput mOtherLanguagesChipsInput;
    private List<LanguageChip> mNativeLanguagesList;
    private List<LanguageChip> mOtherLanguagesList;
    private ScrollView mScrollView;
    private EditText mUsernameEditText;
    private EditText mNameEditText;
    private EditText mSurnameEditText;
    private EditText mPhoneEditText;
    private RadioGroup mGenderRadioGroup;

    private static final int RC_GET_FROM_GALLERY = 9002;
    private static final String TAG = CreateUserProfileActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_create_user_profile);

        mAuthentication = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        initViews();
        setListeners();

        mEditUserPhotoImageView.setImageDrawable(ContextCompat.getDrawable(this,R.mipmap.ic_launcher));
        mEditUserPhotoImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));

        getLanguagesList();

    }

    private void initViews() {
        mSaveButton = (Button) findViewById(R.id.btn_save);
        mEditUserPhotoImageButton = (ImageButton) findViewById(R.id.btn_create_user_photo);
        mEditUserPhotoImageView = (ImageView) findViewById(R.id.iv_create_user_photo);
        mNativeLanguagesChipsInput = (ChipsInput) findViewById(R.id.chips_create_input_languages_native);
        mNativeLanguagesList = new ArrayList<>();
        mOtherLanguagesChipsInput = (ChipsInput) findViewById(R.id.chips_create_input_languages_other);
        mOtherLanguagesList = new ArrayList<>();
        mScrollView = (ScrollView) findViewById(R.id.sv_create_user_profile);
        mUsernameEditText = (EditText) findViewById(R.id.et_create_username);
        mNameEditText = (EditText) findViewById(R.id.et_create_name);
        mSurnameEditText = (EditText) findViewById(R.id.et_create_surname);
        mPhoneEditText = (EditText) findViewById(R.id.et_create_phone);
        mGenderRadioGroup = (RadioGroup) findViewById(R.id.rg_create_gender);
    }

    // Set Listeners
    private void setListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User customUser = createUserProfile();
                writeUserProfileToDatabase(customUser);
                startActivity(new Intent(CreateUserProfileActivity.this, MainActivity.class));
                finish();
            }
        });
        mEditUserPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                //photoPickerIntent.setType("image/*");
                //startActivityForResult(photoPickerIntent, RC_GET_FROM_GALLERY);

                Intent pickImageIntent = new Intent(Intent.ACTION_PICK);

                pickImageIntent.setType("image/*");
                pickImageIntent.putExtra("crop", "true");
                pickImageIntent.putExtra("outputX", 119);
                pickImageIntent.putExtra("outputY", 119);
                pickImageIntent.putExtra("aspectX", 1);
                pickImageIntent.putExtra("aspectY", 1);
                pickImageIntent.putExtra("scale", true);
                startActivityForResult(pickImageIntent, RC_GET_FROM_GALLERY);
            }
        });
        mNativeLanguagesChipsInput.addChipsListener(new ChipsInput.ChipsListener() {
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
                int nY_Pos = mSaveButton.getBottom();
                mScrollView.scrollTo(0,nY_Pos);
                Log.d(TAG, "text changed: " + text.toString());
            }
        });

        mOtherLanguagesChipsInput.addChipsListener(new ChipsInput.ChipsListener() {
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
                int nY_Pos = mSaveButton.getBottom();
                mScrollView.scrollTo(0,nY_Pos);
                Log.d(TAG, "text changed: " + text.toString());
            }
        });
    }

    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuthentication.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(CreateUserProfileActivity.this, MainSignupActivity.class));
            finish();
        }
        if (currentUser != null)
            mDatabaseReference.child("users").child(currentUser.getEmail().replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        startActivity(new Intent(CreateUserProfileActivity.this, MainActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inSampleSize = 1;
                options.inScaled = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap photo = null;
                Uri photoUri = data.getData();
                if (photoUri != null) {
                    photo = BitmapFactory.decodeFile(photoUri.getPath(),options);
                }
                if (photo == null) {
                    Bundle extra = data.getExtras();
                    if (extra != null) {
                        photo = (Bitmap) extra.get("data");
                    }
                }
                mEditUserPhotoImageView.setImageBitmap(photo);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CreateUserProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getLanguagesList() {

        for (String language : getResources().getStringArray(R.array.list_languages)) {
            LanguageChip languageChip = new LanguageChip(language);
            mNativeLanguagesList.add(languageChip);
            mOtherLanguagesList.add(languageChip);
        }

        mNativeLanguagesChipsInput.setFilterableList(mNativeLanguagesList);
        mOtherLanguagesChipsInput.setFilterableList(mOtherLanguagesList);
    }

    private User createUserProfile(){

        String username = mUsernameEditText.getText().toString();
        String name = mNameEditText.getText().toString();
        String surname = mSurnameEditText.getText().toString();

        int selectedGenderID = mGenderRadioGroup.getCheckedRadioButtonId();
        RadioButton genderID=(RadioButton) findViewById(selectedGenderID);
        String gender = genderID.getText().toString();

        String phone = mPhoneEditText.getText().toString();

        List<LanguageChip> nativeLanguagesSelected = (List<LanguageChip>) mNativeLanguagesChipsInput.getSelectedChipList();
        List<LanguageChip> otherLanguagesSelected = (List<LanguageChip>) mOtherLanguagesChipsInput.getSelectedChipList();

        List<String> nativeLanguages = new ArrayList<>();
        List<String> otherLanguages = new ArrayList<>();

        for (LanguageChip nativeLanguage: nativeLanguagesSelected) {
            nativeLanguages.add(nativeLanguage.getLabel());
        }

        for (LanguageChip otherLanguage: otherLanguagesSelected) {
            otherLanguages.add(otherLanguage.getLabel());
        }

        Bitmap photoBitMap =((BitmapDrawable)mEditUserPhotoImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photoBitMap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String photo = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);


        User customUser = new User(username,name,surname,gender,phone,nativeLanguages,otherLanguages,photo);

        return customUser;
    }

    private void writeUserProfileToDatabase(User customUser){
        try {
            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
            mDatabaseReference.child("users").child(firebaseUser.getEmail().replace(".", ",")).setValue(customUser);
            HelpMessage defaultHelpMessage = new HelpMessage(getString(R.string.lbl_no_help_needed),null,"");
            mDatabaseReference.child("help_messages").child(firebaseUser.getEmail().replace(".", ",")).setValue(defaultHelpMessage);
            UserLocation defaultUserLocation = new UserLocation(51.0287364,13.738717);
            mDatabaseReference.child("locations").child(firebaseUser.getEmail().replace(".", ",")).setValue(defaultUserLocation);
            Log.d(TAG, "User with email " + firebaseUser.getEmail() + " is written to database");
        } catch (NullPointerException e){
            Log.d(TAG, "Error while writing user to database");
        }
    }


}
