package com.github.ros_java.test_android.sensor_serial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class MainActivity extends RosActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    private SensorPublisher publisher;
    private GoogleApiClient mGoogleApiClient;
    private boolean gps_permitted;

	public MainActivity(){
        super("sensorSerial","SensorSerial");
	}

    @Override
    public void init(NodeMainExecutor n){
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        publisher = new SensorPublisher(this, n);
        publisher.registerListeners();

        n.execute(publisher, nodeConfiguration);
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void buildGoogleAPIClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



    @Override
    protected void onStart() {

        super.onStart();

        //request permissions
        gps_permitted = true;
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            gps_permitted = false;
            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},0);
        }
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            gps_permitted = false;
            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if(gps_permitted){
            mGoogleApiClient.connect();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        gps_permitted = (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO : is this necessary?
        if(publisher != null){
            publisher.unregisterListeners();
        }
        if(mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        // may be more beneficial to never pause
        // but let's see how this goes

        super.onResume();
        /*if(publisher != null){
            publisher.registerListeners();
        }*/

    }

    @Override
    protected void onPause() {
        super.onPause();

        /*if(publisher != null){
            publisher.unregisterListeners();
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every .1 second
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this.publisher);
        }catch(SecurityException e){
            Log.e("RUNTIME-PERMISSIONS", "FAILED TO REQUEST FUSED LOCATION API");
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
