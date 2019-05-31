package com.example.admin.realtimetracking;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private String email;
    DatabaseReference locations;
    Double lat,lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync( this);

        locations=FirebaseDatabase.getInstance().getReference("Locations");
        if(getIntent()!=null){
            email=getIntent().getStringExtra("email");
            lat=getIntent().getDoubleExtra("lat",0);
            lng=getIntent().getDoubleExtra("lng",0);

        }
        if (!TextUtils.isEmpty(email)){
            loadLocationForThisUser(email);
        }

    }

    private void loadLocationForThisUser(String email) {
        Query user_location =locations.orderByChild("email").equalTo(email);
        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Tracking tracking=postSnapshot.getValue(Tracking.class);

                    LatLng friendLocation=new LatLng(Double.parseDouble(tracking.getLat()),Double.parseDouble(tracking.getLng()));

                    Location currentUser =new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    Location friend =new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLat()));
                    friend.setLongitude(Double.parseDouble(tracking.getLng()));

                    map.clear();
                    map.addMarker(new MarkerOptions()
                                    .position(friendLocation)
                                    .title(tracking.getEmail())
                                    .snippet("Distance"+new DecimalFormat("#.#").format(currentUser.distanceTo(friend)/1000)+" km")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }
                LatLng current=new LatLng(lat,lng);
                map.addMarker(new MarkerOptions().position(current).title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private double distance(Location currentUser, Location friend) {
        double theta=currentUser.getLongitude()-friend.getLongitude();
        double dist=Math.sin(deg2rad(currentUser.getLatitude()))
                *Math.sin(deg2rad(friend.getLatitude()))
                *Math.cos(deg2rad(currentUser.getLatitude()))
                *Math.cos(deg2rad(friend.getLatitude()))
                *Math.cos(deg2rad(theta));

        dist=Math.acos(dist);
        dist=rad2deg(dist);
        dist=dist*60*1.1515;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad*180/Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg*Math.PI/180.0);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
    }
}
