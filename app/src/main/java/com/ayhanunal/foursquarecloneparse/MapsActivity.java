package com.ayhanunal.foursquarecloneparse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    //NOT: MAPS ACT NORMALDE FRAGMENT ACTIVITY'E EXTENDS EDİYORDU BIZ ONU APPCOMPATACTİVTY OLARAK DEGİSTİRDİK.(BAR GOZUKSUN DİYE)

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    String latitudeString;
    String longitudeString;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_place,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.save_place){

            //parse upload.
            upload();



        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.ayhanunal.foursquarecloneparse",MODE_PRIVATE);
                boolean firstTimeCheck = sharedPreferences.getBoolean("noFirstTime",false);

                if(!firstTimeCheck){
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                    sharedPreferences.edit().putBoolean("noFirstTime",true).apply();
                }


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


        };

        //izin kontrolu
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }else {
            //get loc.

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            mMap.clear();

            Location lastKonowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(lastKonowLocation != null){
                LatLng lastUserLocation = new LatLng(lastKonowLocation.getLatitude(),lastKonowLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
            }

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //get loc.

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    mMap.clear();

                    Location lastKonowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if(lastKonowLocation != null){
                        LatLng lastUserLocation = new LatLng(lastKonowLocation.getLatitude(),lastKonowLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                    }

                }
            }
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;

        latitudeString = latitude.toString();
        longitudeString = longitude.toString();

        mMap.addMarker(new MarkerOptions().title("New Place").position(latLng));
        Toast.makeText(this,"click on save!!",Toast.LENGTH_LONG).show();

    }

    public void upload(){

        PlacesClass placesClass = PlacesClass.getInstance();

        String placeName = placesClass.getName();
        String placeType = placesClass.getType();
        String placeAtmosphere = placesClass.getAtmosphere();
        Bitmap placeImage = placesClass.getImage();

        //byte dizisi olusturma
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        placeImage.compress(Bitmap.CompressFormat.PNG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();


        ParseFile parseFile = new ParseFile("images.png",bytes);

        ParseObject object = new ParseObject("Places");
        object.put("name",placeName);
        object.put("type",placeType);
        object.put("atmosphere",placeAtmosphere);
        object.put("latitude",latitudeString);
        object.put("longitude",longitudeString);
        object.put("image",parseFile);
        object.put("username", ParseUser.getCurrentUser().getUsername()); //opsiyonel.

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(getApplicationContext(), LocationsActivity.class);
                    startActivity(intent);
                }
            }
        });


    }

}