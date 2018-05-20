package com.github.ros_java.test_android.sensor_serial;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.rosjava_geometry.Quaternion;


import sensor_msgs.Imu;

/**
 * Created by jamiecho on 2/4/17.
 */


public class IMUPublisher extends AbstractNodeMain {
    private final Publisher<Imu> publisher;
    private Imu msg;
    private boolean updated;

    public IMUPublisher(final ConnectedNode connectedNode) {
        this.publisher = connectedNode.newPublisher("android/imu", sensor_msgs.Imu._TYPE);
        this.msg = publisher.newMessage();
        initialize();
    }

    private void initialize(){

        // initialize covariances
        double[] lc = {
                2e-4,0,0,
                0,3e-4,0,
                0,0,3e-4
        };
        double[] ac = {
                1e-6,0,0,
                0,1e-6,0,
                0,0,1e-6
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
        return GraphName.of("android/imu");
    }


    public void update(float[] linAcc, float[] angVel, float[] orientation) {
        updated = true;

        msg.getLinearAcceleration().setX(linAcc[0]);
        msg.getLinearAcceleration().setY(linAcc[1]);
        msg.getLinearAcceleration().setZ(linAcc[2]);

        msg.getAngularVelocity().setX(angVel[0]);
        msg.getAngularVelocity().setY(angVel[1]);
        msg.getAngularVelocity().setZ(angVel[2]);

        //orientation = w,x,y,z
        msg.getOrientation().setW(orientation[0]); // order is x,y,z,w in ROS
        msg.getOrientation().setX(orientation[1]);
        msg.getOrientation().setY(orientation[2]);
        msg.getOrientation().setZ(orientation[3]);
    }

    //TODO : Implement covariance updates
    public void updateCovariance(double[] lc, double[] ac, double[] oc){
        msg.setLinearAccelerationCovariance(lc);
        msg.setAngularVelocityCovariance(ac);
        msg.setOrientationCovariance(oc);
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