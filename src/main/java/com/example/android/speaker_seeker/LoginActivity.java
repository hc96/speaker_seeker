package com.example.android.speaker_seeker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mSigninButton;
    private Button mForgotPasswordButton;
    private FirebaseAuth mAuthentication;
    private Animation mShakeAnimation;

    public static final String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        setContentView(R.layout.activity_login);

        initViews();

        mAuthentication = FirebaseAuth.getInstance();

        /* Don't show LogIn page if already logged in before -> redirect to Maps Activity directly
        if (mAuthentication.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
            finish();
        }
        */

        setListeners();
    }

    // Initiate Views
    private void initViews() {

        mEmailEditText = (EditText) findViewById(R.id.et_email);
        mPasswordEditText = (EditText) findViewById(R.id.et_pw);
        mSigninButton = (Button) findViewById(R.id.btn_signin);
        mForgotPasswordButton = (Button) findViewById(R.id.btn_forgot_pw);
        mShakeAnimation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
    }

    // Set Listeners
    private void setListeners() {
        mSigninButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String getEmail = mEmailEditText.getText().toString();
                final String getPassword = mPasswordEditText.getText().toString();

                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(getEmail);

                if (getEmail.equals("") || getEmail.length() == 0
                        || getPassword.equals("") || getPassword.length() == 0) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_field_required), Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (!m.find()) {
                    mEmailEditText.startAnimation(mShakeAnimation);
                    Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
                    return;
                }


                mAuthentication.signInWithEmailAndPassword(getEmail, getPassword)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (getPassword.length() < 6) {
                                        mPasswordEditText.startAnimation(mShakeAnimation);
                                        Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_password),Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.error_auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    startActivity(new Intent(LoginActivity.this, BottomNavigActivity.class));
                                }
                            }
                        });
            }
        });
        
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
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
