package com.example.android.speaker_seeker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.speaker_seeker.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import java.util.Arrays;
import java.util.HashMap;

public class MainSignupActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private Button mContinueWithFacebookButton;
    private Button mContinueWithGoogleButton;
    private Button mSignupWithEmailButton;
    private Button mLinkToLoginButton;
    private FirebaseAuth mAuthentication;
    private FirebaseAuth.AuthStateListener mAuthenticationListener;

    private String idToken;
    public SharedPrefManager sharedPrefManager;

    private String fetchedUserName;
    private String fetchedUserEmail;
    private String fetchedUserPhoto;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = MainSignupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_MainSignup);
        setContentView(R.layout.activity_main_signup);

        configureContinueWithFacebook();
        configureContinueWithGoogle();

        mAuthentication = FirebaseAuth.getInstance();
        mAuthenticationListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    createUserInFirebaseHelper();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        initViews();
        setListeners();
    }

    private void initViews() {
        mContinueWithFacebookButton = (Button) findViewById(R.id.btn_continue_with_facebook);
        mContinueWithGoogleButton = (Button) findViewById(R.id.btn_continue_with_google);
        mSignupWithEmailButton = (Button) findViewById(R.id.btn_signup_with_email);
        mLinkToLoginButton = (Button) findViewById(R.id.btn_link_to_login);
    }

    private void setListeners() {
        mContinueWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        mContinueWithFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(MainSignupActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
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

    public void configureContinueWithGoogle(){
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

    }
    private void handleGoogleAccessToken(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuthentication.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithGoogleCredential:successed");
                            createUserInFirebaseHelper();
                            FirebaseUser user = mAuthentication.getCurrentUser();
                            startActivity(new Intent(MainSignupActivity.this, CreateUserProfileActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithGoogleCredential:failed", task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(MainSignupActivity.this, getString(R.string.error_account_already_exists), Toast.LENGTH_LONG).show();
                            } else {
                                Log.w(TAG, "signInWithGoogleCredential:failed", task.getException());
                                Toast.makeText(MainSignupActivity.this, getString(R.string.error_continue_with_google), Toast.LENGTH_SHORT).show();
                            }
                        }
                }
                });
    }

    public void configureContinueWithFacebook(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
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
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "firebaseAuthWithFacebook: " + token.getUserId());
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuthentication.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithFacebookCredential:successed");
                            FirebaseUser user = mAuthentication.getCurrentUser();
                            startActivity(new Intent(MainSignupActivity.this, CreateUserProfileActivity.class));
                            finish();
                        } else {
                            Log.w(TAG, "signInWithFacebookCredential:failed", task.getException());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(MainSignupActivity.this, getString(R.string.error_account_already_exists), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainSignupActivity.this, getString(R.string.error_continue_with_facebook),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                idToken = account.getIdToken();
                fetchedUserName = account.getDisplayName();
                fetchedUserEmail = account.getEmail();
                fetchedUserPhoto = account.getPhotoUrl().toString();

                // Save Data to SharedPreference
                sharedPrefManager = new SharedPrefManager(this);
                sharedPrefManager.saveIsLoggedIn(this, true);

                sharedPrefManager.saveEmail(this, fetchedUserEmail);
                sharedPrefManager.saveName(this, fetchedUserName);
                sharedPrefManager.savePhoto(this, fetchedUserPhoto);

                sharedPrefManager.saveToken(this, idToken);
                //sharedPrefManager.saveIsLoggedIn(mContext, true);

                handleGoogleAccessToken(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, getString(R.string.error_continue_with_google));
                Toast.makeText(this, getString(R.string.error_continue_with_google), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuthentication.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainSignupActivity.this, MainActivity.class));
            finish();
        }

    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void createUserInFirebaseHelper() {

        final String encodedEmail = fetchedUserEmail.replace(".", ",").toLowerCase();
        Log.w(TAG, "Email" + encodedEmail );

        //create an object of Firebase database and pass the the Firebase URL
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userLocation = database.getReference();

        //Add a Listerner to that above location
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                    HashMap<String, Object> timestampJoined = new HashMap<>();


                    // Insert into Firebase database
                    //User newUser = new User(fetchedUserName, fetchedUserPhoto, encodedEmail, timestampJoined);
                    //userLocation.setValue(newUser);

                    //Log.w(TAG, "Account created: " + newUser.getEmail());
                    //Toast.makeText(MainSignupActivity.this, "Account created!", Toast.LENGTH_SHORT).show();

                    // After saving data to Firebase, goto next activity
//                    Intent intent = new Intent(MainActivity.this, NavDrawerActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();
                }
            }
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed", error.toException());
            }
        });
    }

}
