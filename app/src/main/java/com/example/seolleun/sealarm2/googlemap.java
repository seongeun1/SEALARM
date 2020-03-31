package com.example.seolleun.sealarm2;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.places.ui.PlacePicker.*;

public class googlemap extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {
    private static final String TAG = googlemap.class.getSimpleName();
    Context mContext;
    TextView title;

    private Marker currentMarker = null;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient; // Deprecated된 FusedLocationApi를 대체
    private LocationRequest locationRequest;
    private Location mCurrentLocatiion;

    private final LatLng mDefaultLocation = new LatLng(37.56, 126.97);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000 * 60 * 15;  // LOG 찍어보니 이걸 주기로 하지 않는듯
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000 * 15; // 5초 단위로 화면 갱신

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int PLACE_PICKER_REQUEST =1;
    GoogleMap mMap;
    SearchView searchView;
    SupportMapFragment mapFragment;
    List<Location> locationList;
    List<Address> addressList=null;
    LatLng previousPosition = null;
    Marker addedMarker = null;
    LatLng currentLatLng;
    LatLng previousLatLng;
    CircleOptions circle1km;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            mCurrentLocatiion = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.googlemaplayout);
        mContext = googlemap.this;

        searchView=findViewById(R.id.sv_location);
        mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.realtimemap);


        Button btn=(Button) findViewById(R.id.buttonclick);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(addressList==null) {
                    Toast.makeText(getBaseContext(), "Set Destination first", Toast.LENGTH_LONG).show();
                    TextView textView =(TextView) findViewById(R.id.distancetext);


                }

                else {
                    Address address = addressList.get(0);
                    LatLng latLng2 = new LatLng(address.getLatitude(), address.getLongitude());
                    double distance = getDistance(currentLatLng, latLng2);

                    if(distance>1000){
                        distance/=1000;
                        distance=Math.round((distance*1000)/1000);
                        Toast.makeText(getBaseContext(), "목적지까지 약 " +distance+ "km가 남았습니다", Toast.LENGTH_LONG).show();
                    }
                    else{
                        distance=Math.ceil(distance);
                        Toast.makeText(getBaseContext(), "목적지까지 약 " +distance+ "m가 남았습니다", Toast.LENGTH_LONG).show();
                    }


                }




            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location2=searchView.getQuery().toString();

                if(location2!=null || !location2.equals("")) {
                    Geocoder geocoder=new Geocoder(googlemap.this);
                    try{
                        addressList=geocoder.getFromLocationName(location2, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address=addressList.get(0);
                    LatLng latLng=new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location2));


                    mMap.addPolyline(new PolylineOptions().add(latLng, currentLatLng).width(5).color(Color.RED));

                circle1km=new CircleOptions().center(latLng).radius(1000).strokeWidth(2)
                            .strokeColor(0x9575cd)
                            .fillColor(0x45c71585);
                    mMap.addCircle(circle1km);

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);

        // title set
   //     title = (TextView) this.findViewById(R.id.navibar_text); // title
     //   title.setText("실시간 위치 표시");

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 정확도를 최우선적으로 고려
                .setInterval(UPDATE_INTERVAL_MS) // 위치가 Update 되는 주기
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS); // 위치 획득후 업데이트되는 주기

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.realtimemap);
        mapFragment.getMapAsync(this);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocatiion);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.e(TAG, "onMapReady :");
        mMap = map;
        setDefaultLocation(); // GPS를 찾지 못하는 장소에 있을 경우 지도의 초기 위치가 필요함.
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mCurrentLocatiion = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //위치정보 없을 때 만드는 마커
    private void setDefaultLocation() {
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mDefaultLocation);
        markerOptions.title("위치정보 가져올 수 없음");
        markerOptions.snippet("위치 퍼미션과 GPS 활성 여부 확인하세요");
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15);
        mMap.moveCamera(cameraUpdate);
    }

    String getCurrentAddress(LatLng latlng) {
            // 위치 정보와 지역으로부터 주소 문자열을 구한다.
            List<Address> addressList = null ;
            Geocoder geocoder = new Geocoder( this, Locale.getDefault());

            // 지오코더를 이용하여 주소 리스트를 구한다.
            try {
                addressList = geocoder.getFromLocation(latlng.latitude,latlng.longitude,1);
            } catch (IOException e) {
                Toast. makeText( this, "위치로부터 주소를 인식할 수 없습니다. 네트워크가 연결되어 있는지 확인해 주세요.", Toast.LENGTH_SHORT ).show();
                e.printStackTrace();
                return "주소 인식 불가" ;
            }

            if (addressList.size() < 1) { // 주소 리스트가 비어있는지 비어 있으면
                return "해당 위치에 주소 없음" ;
            }

        // 주소를 담는 문자열을 생성하고 리턴
        Address address = addressList.get(0);
        StringBuilder addressStringBuilder = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressStringBuilder.append(address.getAddressLine(i));
            if (i < address.getMaxAddressLineIndex())
                addressStringBuilder.append("\n");
        }

        return addressStringBuilder.toString();
    }



//현재 위치 따라다니기
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        Log.e(TAG, "현재위치 갱신 시작~!");
        previousLatLng=currentLatLng;

        if (currentMarker != null & circle1km!=null)  {
            currentMarker.remove();
            circle1km.visible(false);
        }
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.addPolyline(new PolylineOptions().add(previousLatLng, currentLatLng).width(5).color(Color.BLUE));

        if(addressList==null){
            Toast. makeText( this, "현재위치 로그에서 나왔다, 목적지 설정해라", Toast.LENGTH_SHORT ).show();
            Log.e(TAG, "현재위치 갱신 로그 찍히나 ");

        }
        else{
            Log.e(TAG, "목적지 하나 들어옴");
            Address address=addressList.get(0);
            LatLng latLng2 = new LatLng(address.getLatitude(), address.getLongitude());
            double distance = getDistance(currentLatLng, latLng2);
            Log.e(TAG, "목적지 1번 가져오기 성공?");
            if(distance<1000){
                Log.e(TAG, "거의 다 왔음");
                Intent intent=new Intent(googlemap.this, gmapAlarm.class);
                startActivity(intent);

            }
        }
        MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng).alpha(0.5f).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

      circle1km=new CircleOptions().center(currentLatLng).radius(1000).strokeWidth(2)
                .strokeColor(0x9575cd)
                .fillColor(0x40d1c4e9);
        this.mMap.addCircle(circle1km);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);



    }



    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);

                currentLatLng= new LatLng(location.getLatitude(), location.getLongitude());
                LatLng currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());
                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());
                Log.d(TAG, "Time :" + CurrentTime() + " onLocationResult : " + markerSnippet);
                // Update 주기를 확인해보려고 시간을 찍어보았음.
                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;


            }

        }

    };

    private String CurrentTime(){
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        return time.format(today);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLocationPermissionGranted) {
            Log.d(TAG, "onStart : requestLocationUpdates");
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onStop : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onDestroy : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onClick(View v) {

    }



    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);

        return distance;
    }

}



