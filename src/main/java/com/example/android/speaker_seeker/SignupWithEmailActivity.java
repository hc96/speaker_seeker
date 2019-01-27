package com.example.android.speaker_seeker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupWithEmailActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mAgreeAndSignupButton;
    private Animation mShakeAnimation;
    private Button mTermsOfServiceButton;
    private FirebaseAuth mAuthentication;

    public static final String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";
    private static final String TAG = SignupWithEmailActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        setContentView(R.layout.activity_signup_with_email);

        mAuthentication = FirebaseAuth.getInstance();
        initViews();
        setListeners();
    }

    // Initiate Views
    private void initViews() {

        mEmailEditText = (EditText) findViewById(R.id.et_signup_email);
        mPasswordEditText = (EditText) findViewById(R.id.et_signup_pw);
        mAgreeAndSignupButton = (Button) findViewById(R.id.btn_signup_with_email);
        mShakeAnimation = AnimationUtils.loadAnimation(SignupWithEmailActivity.this, R.anim.shake);
        mTermsOfServiceButton = (Button) findViewById(R.id.btn_terms_of_service);
    }

    // Set Listeners
    private void setListeners() {
        mAgreeAndSignupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String getEmail = mEmailEditText.getText().toString();
                final String getPassword = mPasswordEditText.getText().toString();

                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(getEmail);

                if (getEmail.equals("") || getEmail.length() == 0
                        || getPassword.equals("") || getPassword.length() == 0) {
                    Toast.makeText(SignupWithEmailActivity.this, getString(R.string.error_email_and_password_required), Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (!m.find()) {
                    mEmailEditText.startAnimation(mShakeAnimation);
                    Toast.makeText(SignupWithEmailActivity.this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (getPassword.length() < 6) {
                    mPasswordEditText.startAnimation(mShakeAnimation);
                    Toast.makeText(SignupWithEmailActivity.this, getString(R.string.error_invalid_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuthentication.createUserWithEmailAndPassword(getEmail, getPassword)
                        .addOnCompleteListener(SignupWithEmailActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signUpWithEmailAndPassword:failed", task.getException());
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(SignupWithEmailActivity.this, getString(R.string.error_account_already_exists), Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(SignupWithEmailActivity.this, getString(R.string.error_auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.d(TAG, "signUpWithEmailAndPassword:successed");
                                    startActivity(new Intent(SignupWithEmailActivity.this, CreateUserProfileActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
        mTermsOfServiceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //show our terms of service
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
