/*
 * Copyright (C) 2014 Jamie Cho.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.ros_java.test_android.sensor_serial;

import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.ros.concurrent.CancellableLoop;
import org.ros.concurrent.Holder;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;


import java.util.ArrayList;
import java.util.List;

import sensor_msgs.Imu;
import std_msgs.Header;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 */
// TODO : consider filtering barometric altitude and GPS altitude

public class SensorPublisher extends AbstractNodeMain implements
        SensorEventListener,
        LocationListener {

    private Context mContext;
    private SensorManager mSensorManager;



    private List<Sensor> sensors;

    private IMUPublisher imuPublisher;
    private GPSPublisher gpsPublisher;

    private float[] mAcceleration; //linear acceleration
    private float[] mOrientation;
    private float[] mGyroscope; // angular velocity
    private Location location;
    private float[] mGlobal; //lat,long,alt

    private final float mSeaPressure = 1020; // mBar @ Boston Logan Airport

    public SensorPublisher(Context mContext, NodeMainExecutor n) {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        sensors = new ArrayList<>();

        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)); //TODO: use vanilla Accelerometer data?
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
        // hold off on other sensors

        mAcceleration = new float[3];
        mOrientation = new float[4]; //quaternion
        mGyroscope = new float[3];
        mGlobal = new float[3]; //lat,long,alt

        mGlobal[0] = (float) 42.2932; // olin location
        mGlobal[1] = (float) -71.2637;
        mGlobal[2] = (float) 0; // default to 0... :/

        // prepare quaternion for orientation transformation

        //Quaternion q = new Quaternion(decl,0,0,1);

    }

    /* Sensor Related Stuff */
    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean orientationChanged = false;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                // accelerometer minus gravity
                mAcceleration = event.values;
                break;
            case Sensor.TYPE_GYROSCOPE:
                mGyroscope = event.values;
                break;
            case Sensor.TYPE_PRESSURE:
                mGlobal[2] = SensorManager.getAltitude(mSeaPressure, event.values[0]);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                GeomagneticField g = new GeomagneticField(mGlobal[0], mGlobal[1], mGlobal[2], System.currentTimeMillis());
                float decl = g.getDeclination();
                SensorManager.getQuaternionFromVector(mOrientation, event.values);

                Quaternion q = new Quaternion(mOrientation); //w,x,y,z
                Quaternion q1 = Quaternion.fromAxisAngle(new float[]{0,0,1}, -decl); // Correct for Magnetic Declination
                q = q.mul(q1);
                //q.normalize();
                mOrientation[0] = q.w;
                mOrientation[1] = q.x;
                mOrientation[2] = q.y;
                mOrientation[3] = q.z;


                //TODO : use accuracy : event.values[4], assumed variance?
                break;
        }
        if (imuPublisher != null) {
            //sometimes there's another application running to grab sensor data
            //before imuPublisher is instantiated
            imuPublisher.update(mAcceleration, mGyroscope, mOrientation);
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ADJUST COVARIANCE HERE??
    }

    public void registerListeners() {
        // register all listeners
        for (Sensor s : sensors) {
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);//TODO: make this FASTEST?
        }
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this);
    }


    /* GPS Related Stuff */
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if(gpsPublisher != null){
            gpsPublisher.update(location);
        }
    }

    /* ROS Related Stuff */

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_sensors");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {

        imuPublisher = new IMUPublisher(connectedNode);
        gpsPublisher = new GPSPublisher(connectedNode);

        // This CancellableLoop will be canceled automatically when the node shuts
        // down.
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            @Override
            protected void setup() {

            }

            @Override
            protected void loop() throws InterruptedException {
                imuPublisher.publish();
                Thread.sleep(5);
            }
        });
    }
}
