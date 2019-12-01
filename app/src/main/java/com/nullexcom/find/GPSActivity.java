package com.nullexcom.find;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GPSActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BitmapDescriptor onlineBitmapDescriptor;
    private BitmapDescriptor offlineBitmapDescriptor;

    private boolean isActive = true;

    private Map<String, Marker> markerMap = new HashMap<>();
    private Map<String, Client> clientMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        setupMap();
        setupDB();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        mLocationManager.requestLocationUpdates(provider, 1000, 200, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                moveCamera(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

        onlineBitmapDescriptor = fromDrawable(R.drawable.ic_pin_blue);
        offlineBitmapDescriptor = fromDrawable(R.drawable.ic_pin_red);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener(marker -> {
                String uid = (String) marker.getTag();
                Client client = clientMap.get(uid);
                MarkerInfo markerInfo = MarkerInfo.newInstance(client);
                markerInfo.show(getSupportFragmentManager(), null);
                return false;
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private BitmapDescriptor fromDrawable(@DrawableRes int id) {
        int height = (int) Metrics.dp(this, 40f);
        int width = (int) Metrics.dp(this, 40f);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable(id);
        if (bitmapDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }
        Bitmap b = bitmapDrawable.getBitmap();
        Bitmap marker = Bitmap.createScaledBitmap(b, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(marker);
    }

    private void moveCamera(Location location) {
        if (mMap == null) {
            return;
        }
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17));
    }

    private void addMe(Location location) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationsRef = database.getReference("locations");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        Client client = new Client();
        client.setName(user.getDisplayName());
        client.setDeviceName(Build.MANUFACTURER + " " + Build.MODEL);
        client.setLatitude(location.getLatitude());
        client.setLongitude(location.getLongitude());
        client.setActive(isActive);
        client.setLastOnline(Calendar.getInstance().getTimeInMillis());
        locationsRef.child(user.getUid()).setValue(client);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Vui lòng bật GPS để sử dụng dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }
            addMe(location);
        });
    }

    @Override
    protected void onPause() {
        isActive = false;
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Vui lòng bật GPS để sử dụng dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }
            addMe(location);
        });
        super.onPause();
    }

    public void setupDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationsRef = database.getReference("locations");
        locationsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Client c = dataSnapshot.getValue(Client.class);
                if (c == null) {
                    return;
                }
                MarkerOptions options = new MarkerOptions().position(new LatLng(c.getLatitude(), c.getLongitude()));
                options.snippet("snippet");
                if (c.isActive()) {
                    options.icon(onlineBitmapDescriptor);
                } else {
                    options.icon(offlineBitmapDescriptor);
                }
                String key = dataSnapshot.getKey();
                Marker marker = mMap.addMarker(options);
                marker.setTag(key);
                markerMap.put(key, marker);
                clientMap.put(key, c);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Client c = dataSnapshot.getValue(Client.class);
                if (c == null) {
                    return;
                }
                MarkerOptions options = new MarkerOptions().position(new LatLng(c.getLatitude(), c.getLongitude()));
                if (c.isActive()) {
                    options.icon(onlineBitmapDescriptor);
                } else {
                    options.icon(offlineBitmapDescriptor);
                }
                String key = dataSnapshot.getKey();
                Marker marker = markerMap.get(key);
                if (marker == null) {
                    marker = mMap.addMarker(options);
                    markerMap.put(key, marker);
                } else {
                    marker.setIcon(options.getIcon());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
