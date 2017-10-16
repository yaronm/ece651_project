package com.example.setlocation.setlocationtest;

import android.content.Context;
import java.util.List;
import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

//https://stackoverflow.com/questions/17983865/making-a-location-object-in-android-with-latitude-and-longitude-values
//This website teach me how to create a location instance and set the longitude and latitude.

public class MainActivity extends Activity {

    private TextView positionTextView;

    private LocationManager locationManager;

    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        positionTextView = (TextView) findViewById(R.id.position_text_view);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location targetLoation = new Location("");
        targetLoation.setLatitude(39.15);
        targetLoation.setLongitude(45.15);

        showLocation(targetLoation);
    }

    protected void onDestroy(){
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

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

    private void showLocation(Location location){
        String currenPosition = "latitude is "+ location.getLatitude() + "\n" + "longitude is "
                +location.getLongitude();
        positionTextView.setText(currenPosition);

    }

}
