package com.example.android.speaker_seeker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.android.speaker_seeker.models.HelpMessage;
import com.example.android.speaker_seeker.models.InfoWindowData;
import com.example.android.speaker_seeker.models.User;
import com.example.android.speaker_seeker.models.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeMapFragment extends Fragment {

    private GoogleMap mMap;
    private MapView mMapView;
    private FirebaseMultiQuery mFirebaseMultiQuery;
    private DatabaseReference mDatabaseReference;
    private Map<String,UserLocation> mUserLocationsMap;
    private Map<String,User> mUserInfoMap;
    private Map<String,HelpMessage> mHelpMessagesMap;
    private Map<String,Marker> mMarkersMap;
    private Map<String,InfoWindowData> mInfoWindowDataMap;
    private Map<String, GoogleMap.OnInfoWindowClickListener> mInfoWindowClickMap;


    private ArrayList<String> name = new ArrayList<>();


    private static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final float INITIAL_ZOOM = 12f;

    private static final String TAG = HomeMapFragment.class.getName();

    public HomeMapFragment() {
        // Required empty public constructor
    }

    public static HomeMapFragment newInstance(String info) {
        Log.d(TAG,"Fragment is instantiated");
        Bundle args = new Bundle();
        HomeMapFragment fragment = new HomeMapFragment();
        args.putString("info", info);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_map, null, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mMapView = (MapView) view.findViewById(R.id.home_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mUserLocationsMap = new HashMap<>();
        mUserInfoMap = new HashMap<>();
        mHelpMessagesMap = new HashMap<>();
        mMarkersMap = new HashMap<>();
        mInfoWindowDataMap = new HashMap<>();
        mInfoWindowClickMap = new HashMap<>();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                if(checkPermission()) {
                    mMap.setMyLocationEnabled(true);
                    mShowUsersOnMap();
                }
                else askPermission();
            }
        });
        return view;
    }
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }
    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQUEST_LOCATION_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (checkPermission())
                        mMap.setMyLocationEnabled(true);

                } else {
                    Log.d(TAG, "Permission denied");
                }
                break;
            }
        }
    }

    public void mReadDataOnce(final OnGetDataListener listener) {
        listener.onStart();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });
    }

    private void mShowUsersOnMap() {
        this.mReadDataOnce(new OnGetDataListener() {
            @Override
            public void onStart() {
                //DO SOME THING WHEN START GET DATA HERE
            }

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                DataSnapshot data = dataSnapshot.child("users");
                for(final DataSnapshot user : data.getChildren()){
                    if (user.hasChild("connections")) {
                        Log.d(TAG, "Online user " + user.getKey());
                        DatabaseReference helpMessageRef = mDatabaseReference.child("help_messages").child(user.getKey());
                        DatabaseReference userRef = mDatabaseReference.child("users").child(user.getKey());
                        DatabaseReference locationRef = mDatabaseReference.child("locations").child(user.getKey());

                        mFirebaseMultiQuery = new FirebaseMultiQuery(locationRef,userRef,helpMessageRef);
                        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = mFirebaseMultiQuery.start();
                        allLoad.addOnCompleteListener(getActivity(), new AllOnCompleteListener());
                    }
                    else{
                        // If there was a marker present on the map and user went offline -> remove its marker
                        if (mMarkersMap != null &&  mMarkersMap.containsKey(user.getKey())) {
                            mMarkersMap.get(user.getKey()).remove();
                            mMarkersMap.remove(user.getKey());
                            Log.d(TAG, "Offline user " + user.getKey() + " marker removed");
                        }
                    }
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Log.d(TAG,"No location found for current user");
            }

        });
    }

    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            if (task.isSuccessful()) {
                final Map<DatabaseReference, DataSnapshot> result = task.getResult();
                for (DataSnapshot data : result.values()) {
                    if (data.hasChild("name")) {
                        User user = new User(data.getValue(User.class).getUsername(),
                                data.getValue(User.class).getName(),
                                data.getValue(User.class).getSurname(),
                                data.getValue(User.class).getGender(),
                                data.getValue(User.class).getPhone(),
                                data.getValue(User.class).getNativeLanguages(),
                                data.getValue(User.class).getOtherLanguages(),
                                data.getValue(User.class).getPhoto());
                        mUserInfoMap.put(data.getKey(), user);
                    }
                    if (data.hasChild("date")) {
                        HelpMessage helpMessage = new HelpMessage(data.getValue(HelpMessage.class).getContent(),
                                data.getValue(HelpMessage.class).getLanguages(),
                                data.getValue(HelpMessage.class).getDate());
                        mHelpMessagesMap.put(data.getKey(), helpMessage);
                    }
                    if (data.hasChild("latitude")) {
                        UserLocation userLocation = new UserLocation(data.getValue(UserLocation.class).getLatitude(),
                                data.getValue(UserLocation.class).getLongitude());
                        mUserLocationsMap.put(data.getKey(), userLocation);
                    }
                }

                final String userChild = result.entrySet().iterator().next().getKey().getKey();
                Log.d(TAG, "---------All data was retrieved for user " + userChild+"---------");
                Log.d(TAG, "1) User was retrieved: " + mUserInfoMap.get(userChild ).getUsername());
                Log.d(TAG, "2) Help message was retrieved: " + mHelpMessagesMap.get(userChild ).getContent());
                Log.d(TAG, "3) Location was retrieved: " + mUserLocationsMap.get(userChild ).getLatitude() + " ," + mUserLocationsMap.get(userChild ).getLongitude());

                MarkerOptions markerOptions = new MarkerOptions();
                LatLng lastLocation = new LatLng(mUserLocationsMap.get(userChild).getLatitude(), mUserLocationsMap.get(userChild).getLongitude());

                if(mHelpMessagesMap.get(userChild).getContent().equals("This user currently does not need help")){
                    markerOptions.position(lastLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else{
                    markerOptions.position(lastLocation);
                }

                //If marker for a user has not been added -> add
                if (!mMarkersMap.containsKey(userChild)) {
                    final Marker marker = mMap.addMarker(markerOptions);
                    mMarkersMap.put(userChild, marker);
                //If marker for a user has been already added but the location has been changed -> remove old and add another one
                } else if (!(mMarkersMap.get(userChild).getPosition().equals(lastLocation))) {
                    mMarkersMap.get(userChild).remove();
                    mMarkersMap.remove(userChild);
                    final Marker marker = mMap.addMarker(markerOptions); //Add new marker to the map
                    mMarkersMap.put(userChild, marker);
                //If marker for a user has been already added and the location has not been changed but the user has deleted the help message -> remove old Red marker and add new Green one
                } else if ((mMarkersMap.get(userChild).getPosition().equals(lastLocation) && mHelpMessagesMap.get(userChild).getContent().equals("This user currently does not need help"))){
                    mMarkersMap.get(userChild).remove();
                    mMarkersMap.remove(userChild);
                    markerOptions.position(lastLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    final Marker marker = mMap.addMarker(markerOptions); //Add new marker to the map
                    mMarkersMap.put(userChild, marker);
                //If marker for a user has been already added and the location has not been changed but the user has sent the help message -> remove old Green marker and add new Red one
                } else if ((mMarkersMap.get(userChild).getPosition().equals(lastLocation) && !mHelpMessagesMap.get(userChild).getContent().equals("This user currently does not need help"))){
                    mMarkersMap.get(userChild).remove();
                    mMarkersMap.remove(userChild);
                    markerOptions.position(lastLocation);
                    final Marker marker = mMap.addMarker(markerOptions);
                    mMarkersMap.put(userChild, marker);
                }

                mInfoWindowDataMap.put(userChild, new InfoWindowData(mUserInfoMap.get(userChild), mHelpMessagesMap.get(userChild)));

                if (mMarkersMap.containsKey(userChild)) {
                    mMarkersMap.get(userChild).setTag(mInfoWindowDataMap.get(userChild));
                    InfoWindowCustom infoWindowCustom = new InfoWindowCustom(getActivity());
                    mMap.setInfoWindowAdapter(infoWindowCustom);

                    mInfoWindowClickMap.put(userChild, new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            if (marker.isInfoWindowShown()) {
                                final   InfoWindowData data = (InfoWindowData) marker.getTag();
                                int optionsToContactUser;
                                if(isConnectedToNetwork())
                                    optionsToContactUser = R.array.options_contact_user_connected;
                                else
                                    optionsToContactUser = R.array.options_contact_user_not_connected;
                                AlertDialog dialog = new AlertDialog.Builder(getContext())
                                        .setTitle(R.string.dlg_title_help_way)
                                        .setItems(optionsToContactUser, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int item) {
                                                if (item == 0) {
                                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.getUser().getPhone()));
                                                    Log.d(TAG, "Calling " + data.getUser().getUsername() + " via phone " + data.getUser().getPhone());
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                } else if (item == 1) {
                                                    ChatwindowFragment window = new ChatwindowFragment();
                                                    Bundle args = new Bundle();
                                                    if(!name.contains(data.getUser().getName())){

                                                        name.add(data.getUser().getName());
                                                    }
                                                    args.putStringArrayList("speaker", name);
                                                    window.setArguments(args);

                                                    FragmentTransaction agm_ft = getActivity().getSupportFragmentManager()
                                                            .beginTransaction();
                                                    agm_ft.replace(R.id.speaker, window,
                                                            "agm_frag");
                                                    agm_ft.addToBackStack(null);
                                                    agm_ft.commit();

                                                    ((BottomNavigationView) getActivity().findViewById(R.id.bnv)).setSelectedItemId(R.id.bni_chat);

                                                }
                                            }
                                        })
                                        .create();
                                dialog.show();
                                mMarkersMap.get(userChild).hideInfoWindow();
                            } else {
                                mMarkersMap.get(userChild).showInfoWindow();
                            }
                        }
                    });

                    mMap.setOnInfoWindowClickListener(mInfoWindowClickMap.get(userChild));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(lastLocation).zoom(INITIAL_ZOOM).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
            else {
                Log.d(TAG,"Error has occurred while retrieving information from database");
            }

        }
    }

    public void onStop() {
//        mFirebaseMultiQuery.stop();
        super.onStop();
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
        }

    }

}
