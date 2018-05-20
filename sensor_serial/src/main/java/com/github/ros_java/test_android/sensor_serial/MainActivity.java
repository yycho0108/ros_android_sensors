package com.github.ros_java.test_android.sensor_serial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import android.location.Location;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class MainActivity extends RosActivity {

    private SensorPublisher publisher;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    private boolean gps_permitted;

	public MainActivity(){
        super("sensorSerial","SensorSerial");
	}

    @Override
    public void init(NodeMainExecutor n){
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        publisher = new SensorPublisher(this, n);

        //register listeners - camera and other sensors
        publisher.registerListeners();

        n.execute(publisher, nodeConfiguration);


    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
             if(locationResult == null) return;

             Location loc = locationResult.getLastLocation();
             if(publisher != null){
                 publisher.onLocationChanged(loc);
             }

            }
        };
        // just for calibration ...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {

        super.onStart();

        //request permissions
        //TODO: frankly, these aren't very robust, but the assumption is that I have permissions anyways

        gps_permitted = true;

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            gps_permitted = false;
            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},0);
        }
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            gps_permitted = false;
            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        /*if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        }*/

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        gps_permitted = (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if(gps_permitted){
            // TODO : do something
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO : is this necessary?
        if(publisher != null){
            publisher.unregisterListeners();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        /*
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        */
    }

    private void startLocationUpdates() {
        int ps1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int ps2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(ps1 == PackageManager.PERMISSION_GRANTED && ps2 == PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
