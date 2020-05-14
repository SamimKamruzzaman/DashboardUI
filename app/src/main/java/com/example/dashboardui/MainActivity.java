package com.example.dashboardui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    Context context = this;
    Button fetch;
    TextView user_location;
    private FusedLocationProviderClient mFusedLocationClient;
    TextView textView;
    private static SensorManager sensorManager;
    private Sensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.resultView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        fetch = findViewById(R.id.fetch_location);
        user_location = findViewById(R.id.user_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fetchLocation();


            }
        });
    }

    private void fetchLocation() {


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(this)
                        .setTitle("Required Location Permission")
                        .setMessage("You have to give this permission to acess this feature")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                Double latittude = location.getLatitude();
                                Double longitude = location.getLongitude();

                                user_location.setText("Latitude = "+latittude + "\nLongitude = " + longitude);

                                LocationHelper helper=new LocationHelper(
                                        location.getLongitude(),
                                        location.getLatitude()
                                );

                                FirebaseDatabase.getInstance().getReference("Current Location")
                                        .setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            Toast.makeText(MainActivity.this,"Location Saved",Toast.LENGTH_LONG).show();
                                        }
                                        else{
                                            Toast.makeText(MainActivity.this,"Location Not Saved",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    });

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //abc
            }else{

            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(sensor != null){
            sensorManager.registerListener(this,sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this,"NOT supported !", Toast.LENGTH_SHORT).show();
            finish();
        }


    }
    @Override

    protected void onPause(){

        super.onPause();

        sensorManager.unregisterListener(this);

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float azimuth = Math.round(sensorEvent.values[0]);
        float pitch = Math.round(sensorEvent.values[1]);
        float roll = Math.round(sensorEvent.values[2]);
        double tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));
        String text = String.format("%.0f",tesla);
        textView.setText(text + " μT");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
