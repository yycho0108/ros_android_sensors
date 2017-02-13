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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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

   private CameraBridgeViewBase mOpenCVCameraView;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mOpenCVCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {
            super.onPackageInstall(operation, callback);
        }
    };
	public MainActivity(){
        super("sensorSerial","SensorSerial");
	}

    @Override
    public void init(NodeMainExecutor n){
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        publisher = new SensorPublisher(this, n);

        //register listeners - camera and other sensors
        mOpenCVCameraView.setCvCameraViewListener(publisher);
        publisher.registerListeners();

        n.execute(publisher, nodeConfiguration);

        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // just for calibration ...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCVCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_view);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setMaxFrameSize(640,480);
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
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        }

        if(gps_permitted){
            buildGoogleAPIClient();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        gps_permitted = (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if(gps_permitted){
            buildGoogleAPIClient();
        }
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
        if (mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCVCameraView != null) {
            // consider removing this?
            mOpenCVCameraView.disableView();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    void startLocationService(){
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(20); // Update location every .1 second
        try{
            if(publisher != null && mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this.publisher);
            }
        }catch(SecurityException e){
            Log.e("RUNTIME-PERMISSIONS", "FAILED TO REQUEST FUSED LOCATION API");
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationService();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
