package com.github.ros_java.test_android.sensor_serial;

import android.location.Location;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import sensor_msgs.Imu;
import sensor_msgs.NavSatFix;
import sensor_msgs.NavSatStatus;

/**
 * Created by jamiecho on 2/4/17.
 */


public class GPSPublisher extends AbstractNodeMain {
    private final Publisher<NavSatFix> publisher;
    private NavSatFix msg;
    private boolean updated;

    public GPSPublisher(final ConnectedNode connectedNode) {
        this.publisher = connectedNode.newPublisher("android_gps", NavSatFix._TYPE);
        this.msg = publisher.newMessage();
        initialize();
    }

    private void initialize(){

        // initialize covariances
        double[] lc = {
                0.01,0,0,
                0,0.01,0,
                0,0,0.01
        };
        double[] ac = {
                0.025,0,0,
                0,0.025,0,
                0,0,0.025
        };
        double[] oc = {
                0.001,0,0,
                0,0.001,0,
                0,0,0.001
        };

        updateCovariance(lc,ac,oc);
        // no value yet
        updated = false;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_gps");
    }


    public void update(Location location) {
        updated = true;

        msg.setLatitude(location.getLatitude());
        msg.setLongitude(location.getLongitude());
        msg.setAltitude(location.getAltitude());

        msg.getStatus().setService(NavSatStatus.SERVICE_GPS);
        msg.getStatus().setService(NavSatStatus.STATUS_FIX);
    }

    //TODO : Implement covariance updates
    public void updateCovariance(double[] lc, double[] ac, double[] oc){

    }

    public void publish() {
        //only publish when data got updated
        if(updated){
            updated = false;
            Utilities.setHeader(msg.getHeader()); // populate header
            publisher.publish(msg);
        }
    }
}