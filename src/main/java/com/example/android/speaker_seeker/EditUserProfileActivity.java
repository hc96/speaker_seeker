package com.example.android.speaker_seeker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.example.android.speaker_seeker.models.LanguageChip;
import com.example.android.speaker_seeker.models.User;
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

public class EditUserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuthentication;
    private DatabaseReference mDatabaseReference;
    private Button mLogoutButton;
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
    private static final String TAG = EditUserProfileActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_edit_user_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        mAuthentication = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        initViews();
        setListeners();

        mEditUserPhotoImageView.setImageDrawable(ContextCompat.getDrawable(this,R.mipmap.ic_launcher));
        mEditUserPhotoImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));

        getLanguagesList();
        readUserProfileFromDatabase();

    }

    private void initViews() {
        mLogoutButton = (Button) findViewById(R.id.btn_log_out);
        mEditUserPhotoImageButton = (ImageButton) findViewById(R.id.btn_edit_user_photo);
        mEditUserPhotoImageView = (ImageView) findViewById(R.id.iv_edit_user_photo);
        mNativeLanguagesChipsInput = (ChipsInput) findViewById(R.id.chips_input_languages_native);
        mNativeLanguagesList = new ArrayList<>();
        mOtherLanguagesChipsInput = (ChipsInput) findViewById(R.id.chips_input_languages_other);
        mOtherLanguagesList = new ArrayList<>();
        mScrollView = (ScrollView) findViewById(R.id.sv_edit_user_profile);
        mUsernameEditText = (EditText) findViewById(R.id.et_username);
        mNameEditText = (EditText) findViewById(R.id.et_name);
        mSurnameEditText = (EditText) findViewById(R.id.et_surname);
        mPhoneEditText = (EditText) findViewById(R.id.et_edit_phone);
        mGenderRadioGroup = (RadioGroup) findViewById(R.id.rg_gender);
    }

    private void readUserProfileFromDatabase(){
        try {
            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
            String userUid = firebaseUser.getEmail().replace(".", ",");
            mDatabaseReference.child("users").child(userUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    try {
                        mUsernameEditText.setText(user.getUsername());
                        mNameEditText.setText(user.getName());
                        mSurnameEditText.setText(user.getSurname());
                        mPhoneEditText.setText(user.getPhone());
                        for (String nativeLanguage : user.getNativeLanguages()) {
                            mNativeLanguagesChipsInput.addChip(new LanguageChip(nativeLanguage));
                        }
                        for (String otherLanguage : user.getOtherLanguages()) {
                            mOtherLanguagesChipsInput.addChip(new LanguageChip(otherLanguage));
                        }

                        String userGender = user.getGender();
                        if (userGender.equals("Male"))
                            mGenderRadioGroup.check(R.id.rbtn_male);
                        else
                            mGenderRadioGroup.check(R.id.rbtn_female);

                        if (user.getPhoto() != null) {
                            byte[] decodedByteArray = android.util.Base64.decode(user.getPhoto(), Base64.DEFAULT);
                            Bitmap photoBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

                            mEditUserPhotoImageView.setImageBitmap(photoBitmap);
                        }
                    } catch (NullPointerException e){
                        Toast.makeText(EditUserProfileActivity.this,getString(R.string.error_read_user_profile_from_database),Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"Failed to read user profile information from database: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(EditUserProfileActivity.this,getString(R.string.error_read_user_profile_from_database),Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"The read failed: " + databaseError.getCode());
                }
            });
        } catch (NullPointerException e){
            Toast.makeText(EditUserProfileActivity.this,getString(R.string.error_read_user_profile_from_database),Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error while retrieving user profile information from database");
        }


    }

    // Set Listeners
    private void setListeners() {
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthentication.signOut();
                startActivity(new Intent(EditUserProfileActivity.this, MainSignupActivity.class));
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
                int nY_Pos = mLogoutButton.getBottom();
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
                int nY_Pos = mLogoutButton.getBottom();
                mScrollView.scrollTo(0,nY_Pos);
                Log.d(TAG, "text changed: " + text.toString());
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
                Toast.makeText(EditUserProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_changes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save_user_profile_changes:
                User customUser = getUserProfileChanges();
                saveUserProfileChangesToDatabase(customUser);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private User getUserProfileChanges(){
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
        photoBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String photo = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        User customUser = new User(username,name,surname,gender,phone,nativeLanguages,otherLanguages,photo);

        return customUser;
    }

    private void saveUserProfileChangesToDatabase(User customUser){
        try {
            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
            String userUid = firebaseUser.getEmail().replace(".", ",");
            mDatabaseReference.child("users").child(userUid).child("username").setValue(customUser.getUsername());
            mDatabaseReference.child("users").child(userUid).child("name").setValue(customUser.getName());
            mDatabaseReference.child("users").child(userUid).child("surname").setValue(customUser.getSurname());
            mDatabaseReference.child("users").child(userUid).child("gender").setValue(customUser.getGender());
            mDatabaseReference.child("users").child(userUid).child("phone").setValue(customUser.getPhone());
            mDatabaseReference.child("users").child(userUid).child("nativeLanguages").setValue(customUser.getNativeLanguages());
            mDatabaseReference.child("users").child(userUid).child("otherLanguages").setValue(customUser.getOtherLanguages());
            mDatabaseReference.child("users").child(userUid).child("photo").setValue(customUser.getPhoto());
            Toast.makeText(EditUserProfileActivity.this, getString(R.string.success_save_user_profile_to_database), Toast.LENGTH_LONG).show();
            Log.d(TAG, "Profile changes are saved");

        } catch (NullPointerException e){
            Toast.makeText(EditUserProfileActivity.this, getString(R.string.error_save_user_profile_to_database), Toast.LENGTH_LONG).show();
            Log.d(TAG, "Error while saving user profile changes to database");
        }

    }

}
