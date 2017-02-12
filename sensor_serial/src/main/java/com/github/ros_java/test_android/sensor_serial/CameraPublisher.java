package com.github.ros_java.test_android.sensor_serial;

import android.graphics.Bitmap;
import android.util.Log;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.ros.internal.message.MessageBuffers;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import sensor_msgs.CameraInfo;
import sensor_msgs.Image;
import sensor_msgs.CompressedImage;


/**
 * Created by jamiecho on 2/4/17.
 */

//There is an issue with frequent publication of large data
//https://github.com/rosjava/rosjava_core/issues/176
//thus the image would probably have to be compressed

public class CameraPublisher extends AbstractNodeMain {
    private final Publisher<Image> publisher;
    private Image msg;
    private CameraInfo msg_info;


    private boolean updated = false;

    private boolean initialized = false;
    private int width,height, nChannels;
    private ChannelBuffer cb;
    private byte[] bb;

    private long last_published = 0;

    public CameraPublisher(final ConnectedNode connectedNode) {
        this.publisher = connectedNode.newPublisher("android/image_raw", Image._TYPE);
        this.msg = publisher.newMessage();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/camera");
    }

    public void init(Mat img){
        Log.i("CV_CAMERA_PUB", "Initializing");
        width = img.width();
        height = img.height();
        nChannels = img.channels();
        bb = new byte[width*height*nChannels];
        cb = ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN, bb);

        msg.setEncoding("rgba8");
        msg.setWidth(width);
        msg.setHeight(height);
        msg.setStep(nChannels*width);

        //msg_info.setWidth(width);
        //msg_info.setHeight(height);
        //TODO : fill calibration params after I figure it out
    }

    public void update(Mat img) {
        if(!initialized){
            init(img);
            initialized = true;
        }

        updated = true;

        img.get(0,0,bb); //bb is modified here
        cb.resetReaderIndex();
        msg.setData(cb);
    }

    public void publish() {
        long now = System.currentTimeMillis();
        float dt = (now - last_published);
        dt /= 1000;
        float hz = 1; // 10 hz publish rate

        //only publish when data got updated
        if(updated && (dt > 1/hz)){
            updated = false;
            last_published = now;
            Utilities.setHeader(msg.getHeader()); // populate header
            publisher.publish(msg);
        }
    }
}