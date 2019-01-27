package com.example.android.speaker_seeker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private Button mResetPasswordButton;
    private Animation mShakeAnimation;
    private FirebaseAuth mAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        setContentView(R.layout.activity_reset_password);

        initViews();
        mAuthentication = FirebaseAuth.getInstance();
        setListeners();
    }

    private void initViews() {

        mEmailEditText = (EditText) findViewById(R.id.et_pw_reset_email);
        mResetPasswordButton = (Button) findViewById(R.id.btn_reset_pw);
        mShakeAnimation = AnimationUtils.loadAnimation(ResetPasswordActivity.this, R.anim.shake);
    }

    private void setListeners() {
        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String getEmail = mEmailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(getEmail)) {
                    Toast.makeText(getApplication(), getString(R.string.error_field_required), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuthentication.sendPasswordResetEmail(getEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.check_email_reset_password), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.error_send_reset_password_instractions), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
