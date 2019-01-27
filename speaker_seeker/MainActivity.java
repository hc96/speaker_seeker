package com.example.android.speaker_seeker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.speaker_seeker.models.HelpMessage;
import com.example.android.speaker_seeker.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkStateReceiver.NetworkStateReceiverListener
        {

    private NetworkStateReceiver networkStateReceiver;

    // Navigation Drawer views
    private TextView mUserNameTextView;
    private ImageView mUserPhotoImageView;
    private Toolbar mToolBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private View mNavigationHeader;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuthentication;

    // Bottom Navigation Menu views
    private BottomNavigationView mBottomNavigationView;
    private ViewPagerCustom mViewPagerCustom;
    private MenuItem mMenuItem;

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setListeners();
        mToolBar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(mToolBar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        mNavigationView.setNavigationItemSelectedListener(this);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mAuthentication = FirebaseAuth.getInstance();

        mViewPagerCustom.setPagingEnabled(false);
        mViewPagerCustom.setOffscreenPageLimit(2);
        setViewPagerCustom(mViewPagerCustom);
        retrieveUserPhotoFromDatabase();

        startService(new Intent(this,LocationTrackingService.class));

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
        String userUid = firebaseUser.getEmail().replace(".", ",");

        final DatabaseReference userConnectionsRef = mDatabaseReference.child("users").child(userUid).child("connections");
        final DatabaseReference lastOnlineRef = mDatabaseReference.child("users").child(userUid).child("lastOnline");

        final DatabaseReference connectedRef = mDatabaseReference.child(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    DatabaseReference con = userConnectionsRef.push();
                    con.onDisconnect().removeValue();
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    con.setValue(Boolean.TRUE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Listener was cancelled at .info/connected");
            }
        });

    }

    private void initViews() {
        //Navigation Drawer views
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationHeader = mNavigationView.getHeaderView(0);
        mUserNameTextView = (TextView) mNavigationHeader.findViewById(R.id.tv_username);
        mUserPhotoImageView = (ImageView) mNavigationHeader.findViewById(R.id.iv_user_photo);

        // Bottom Navigation Menu views
        mViewPagerCustom = (ViewPagerCustom) findViewById(R.id.vp_custom);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bnv);
    }

    // Set Listeners
    private void setListeners() {
        mUserNameTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditUserProfileActivity.class));
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        mUserPhotoImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditUserProfileActivity.class));
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bni_home_map:
                                Log.d(TAG,getString(R.string.title_fragment_home_map)+" is selected");
                                mViewPagerCustom.setCurrentItem(0,false);
                                return true;
                            case R.id.bni_call_for_help:
                                Log.d(TAG,getString(R.string.title_fragment_call_for_help)+" is selected");
                                mViewPagerCustom.setCurrentItem(1,false);
                                return true;
                            case R.id.bni_chat:
                                Log.d(TAG,getString(R.string.title_fragment_chat)+" is selected");
                                mViewPagerCustom.setCurrentItem(2,false);
                                return true;
                        }
                        return false;
                    }
                });
        mViewPagerCustom.addOnPageChangeListener(new ViewPagerCustom.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mMenuItem != null) {
                    mMenuItem.setChecked(false);
                } else {
                    mBottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                mMenuItem = mBottomNavigationView.getMenu().getItem(position);
                mMenuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notifications) {
            return true;
        }
        if (id == R.id.action_delete_help_message) {
            deleteHelpMessage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Log.d(TAG,getString(R.string.title_fragment_home_map)+" is selected");
            mViewPagerCustom.setCurrentItem(0,false);
        } else if (id == R.id.nav_call_for_help) {
            Log.d(TAG,getString(R.string.title_fragment_call_for_help)+" is selected");
            mViewPagerCustom.setCurrentItem(1,false);
        } else if (id == R.id.nav_chat) {
            Log.d(TAG,getString(R.string.title_fragment_chat)+" is selected");
            mViewPagerCustom.setCurrentItem(2,false);
        } else if (id == R.id.nav_notifications) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_send_feedback) {

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setViewPagerCustom(ViewPagerCustom viewPagerCustom) {
        Log.d(TAG,"setViewPagerCustom");
        ViewPagerCustomAdapter adapter = new ViewPagerCustomAdapter(getSupportFragmentManager());
        adapter.addFragment(HomeMapFragment.newInstance("Home"));
        adapter.addFragment(CallForHelpFragment.newInstance("Call for help"));
        adapter.addFragment(ChatwindowFragment.newInstance("Speaker"));
        viewPagerCustom.setAdapter(adapter);
    }

    private void retrieveUserPhotoFromDatabase() {
        try {
            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
            String userUid = firebaseUser.getEmail().replace(".", ",");
            mDatabaseReference.child("users").child(userUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    try {
                        mUserNameTextView.setText(user.getName() + " " + user.getSurname());
                        if (user.getPhoto() != null) {
                            byte[] decodedByteArray = android.util.Base64.decode(user.getPhoto(), Base64.DEFAULT);
                            Bitmap photoBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                            mUserPhotoImageView.setImageBitmap(photoBitmap);
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.error_read_user_profile_from_database), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Failed to read user profile information from database");
                        }
                    }catch (NullPointerException e){
                        Toast.makeText(MainActivity.this,getString(R.string.error_read_user_profile_from_database),Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"Failed to read user profile information from database: " + e.getMessage());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_read_user_profile_from_database), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "The read failed: " + databaseError.getCode());
                }
            });
        } catch (NullPointerException e) {
            Toast.makeText(MainActivity.this, getString(R.string.error_read_user_profile_from_database), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error while retrieving user profile information from database");
        }
    }

    @Override
    public void networkAvailable() {
        Log.d(TAG, "Network is available");
    }

    @Override
    public void networkUnavailable() {
        Log.d(TAG, "Network is not available");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dlg_title_no_network_connection)
                .setMessage(R.string.dlg_msg_no_network_connection)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();
        dialog.show();
    }

    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }
    public void deleteHelpMessage(){
        FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
        String userUid = firebaseUser.getEmail().replace(".", ",");
        HelpMessage defaultHelpMessage = new HelpMessage(getString(R.string.lbl_no_help_needed),null,"");
        mDatabaseReference.child("help_messages").child(userUid).setValue(defaultHelpMessage);
        Toast.makeText(MainActivity.this, getString(R.string.status_help_message_deleted), Toast.LENGTH_SHORT).show();
        Log.d(TAG,"Help message for " + userUid + " was deleted");

    }
}
