package com.example.android.speaker_seeker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class MainSignupActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private Button mContinueWithFacebookButton;
    private Button mContinueWithGoogleButton;
    private Button mSignupWithEmailButton;
    private Button mLinkToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_MainSignup);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main_signup);
        initViews();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(MainSignupActivity.this, "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken(), Toast.LENGTH_LONG).show();

                        startActivity(new Intent(MainSignupActivity.this, MapsActivity.class));
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainSignupActivity.this, getString(R.string.cancel_continue_with_facebook), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(MainSignupActivity.this, getString(R.string.error_continue_with_facebook), Toast.LENGTH_SHORT).show();
                    }
                });
        setListeners();

    }

    private void setListeners() {
        mContinueWithFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(MainSignupActivity.this, Arrays.asList("public_profile", "user_friends"));
            }
        });

        mLinkToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSignupActivity.this, LoginActivity.class));
            }
        });
        mSignupWithEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSignupActivity.this, SignupWithEmailActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    // Initiate Views
    private void initViews() {
        mContinueWithFacebookButton = (Button) findViewById(R.id.btn_continue_with_facebook);
        mContinueWithGoogleButton = (Button) findViewById(R.id.btn_continue_with_google);
        mSignupWithEmailButton = (Button) findViewById(R.id.btn_signup_with_email);
        mLinkToLoginButton = (Button) findViewById(R.id.btn_link_to_login);
    }
}
