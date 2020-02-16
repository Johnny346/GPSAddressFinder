package com.example.gpsaddressfinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Main App Activity
 */
public class MainActivity extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
    /**
     * GPSTracker class
     * Location Object used here
     * DBHelper sql database object used here
     */
    //
    private GoogleMap mMap;
    GPSTracker gps;
    ImageView image;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Location gps_loc;
    Location network_loc;
    Location final_loc;
    double longitude;
    double latitude;
    double currentLongitude;
    double currentLatitude;
    String userCountry, userAddress;
    DBHelper mydb;
    Marker startMarker;
    Marker nextMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mydb = new DBHelper(this);
        mydb.deleteContact(1);
        final TextView textViewmain = findViewById(R.id.textViewmain);

        textViewmain.setTextSize(19);



        //Button viewReports = findViewById(R.id.ReportButton);
       // viewReports.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, Reports.class);
//                startActivity(intent);
//            }
//        });

        /**
         * Start Activity button here
         * first check if GPPS is enabled and then get user permissions
         * get the current location and update the UI
         * change UI image on main activity to show user that activity has started
         */
        Button startActivityButton = findViewById(R.id.startButton);
        startActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                TextView startAddressTextView = findViewById(R.id.textViewStartAddress);
                startAddressTextView.setTextSize(22);
                startAddressTextView.setText("");
                getLocation(startAddressTextView);
               // ----------------------
                // Create class object
                gps = new GPSTracker(MainActivity.this);

                // Check if GPS enabled
                if(gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    currentLongitude = gps.getLongitude();
                    currentLatitude = gps.getLatitude();

                } else {
                    // Can't get location.
                    // GPS or network is not enabled.
                    // Ask user to enable GPS/network in settings.
                    gps.showSettingsAlert();
                }

                String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                if(!provider.contains("gps")){ //if gps is disabled
                    final Intent poke = new Intent();
                    poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                    poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                    poke.setData(Uri.parse("3"));
                    sendBroadcast(poke);
                }
                float startTime = System.currentTimeMillis();


            }
        });



    }

    /**
     * Gets the location and with more detail such as the house address
     * @param startAddressTextView is the textView in the UI to update
     * */
    private String getLocation(TextView startAddressTextView){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        startAddressTextView = findViewById(R.id.textViewStartAddress);


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            return "";
        }

        try {

            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gps_loc != null) {
            final_loc = gps_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else if (network_loc != null) {
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else {
            latitude = 0.0;
            longitude = 0.0;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        try {

            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                userCountry = addresses.get(0).getCountryName();
                userAddress = addresses.get(0).getAddressLine(0);
                //mydb.insertItem(userAddress);
                startAddressTextView.setTextSize(22);
                startAddressTextView.setTextColor(Color.BLACK);
                startAddressTextView.setText("GPS Address: "+userCountry + ", " + userAddress);
                final LatLng PERTH = new LatLng(latitude, longitude);
                if(startMarker == null){
                    startMarker = mMap.addMarker(new MarkerOptions()
                            .position(PERTH)
                            .draggable(true));
                    startMarker.setTitle(userAddress);
                }

            }
            else {
                userCountry = "Unknown";
                startAddressTextView.setText(userCountry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
            return "Try Again";
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
       
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        mMap.setOnMarkerDragListener(this);
        // Set a listener for marker click.
       mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        //Toast.makeText(this, "Current location:\n" + location., Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
       // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).


        TextView startAddressTextView = findViewById(R.id.textViewStartAddress);
        this.getLocation(startAddressTextView);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        startMarker.setTitle("");

    }

    public void onMarkerDrag(Marker marker){

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng center0 = mMap.getCameraPosition().target;
        LatLng center = marker.getPosition();
        String userCountryNew = "Unknown";
        String userAddressNew = "Unknown";
        //marker.remove();
        TextView startAddressTextView = findViewById(R.id.textViewStartAddress);
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(center.latitude, center.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            userCountryNew = addresses.get(0).getCountryName();
            userAddressNew  = addresses.get(0).getAddressLine(0);


        if(userAddress.contentEquals(addresses.get(0).getAddressLine(0))){
            userAddressNew = "Unknown";
        }
        if(startMarker == null){
            startMarker = mMap.addMarker(new MarkerOptions().position(marker.getPosition()).draggable(true).title(userAddressNew));
        }
        startMarker.setSnippet("Drag & Click");
        //marker.remove();
            Toast.makeText(this, "Marker Address: " + userAddressNew, Toast.LENGTH_SHORT).show();
        startAddressTextView.setTextSize(22);
        startAddressTextView.setTextColor(Color.BLACK);
        startAddressTextView.setText("Marker Address: "+userCountryNew + ", " + userAddressNew);

        }

        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius(5) // In meters
                .strokeColor(Color.GREEN)
                .clickable(true)
                ;

// Get back the mutable Circle
        Circle circle = mMap.addCircle(circleOptions);
        circle.setTag(userAddressNew);
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

            @Override
            public void onCircleClick(Circle circle) {
                // Flip the r, g and b components of the circle's
                // stroke color.

                TextView startAddressTextView = findViewById(R.id.textViewStartAddress);
                startAddressTextView.setTextSize(22);
                startAddressTextView.setTextColor(Color.BLACK);
                startAddressTextView.setText("Circle Address: "+circle.getTag() );
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the data from the marker.
        marker.showInfoWindow();
        LatLng position = marker.getPosition(); //

        marker.getPosition();
        //marker.remove();
        //onMarkerDrag(marker);
        //Toast.makeText(this, "MyLocation", Toast.LENGTH_SHORT).show();
       // Toast.makeText(this, "MyLocation"+ position.toString(), Toast.LENGTH_SHORT).show();
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
}
