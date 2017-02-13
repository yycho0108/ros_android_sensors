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

import java.io.ByteArrayOutputStream;
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
    private final Publisher<CompressedImage> publisher;
    private final Publisher<CameraInfo> infoPublisher;
    private CompressedImage msg;
    private CameraInfo infoMsg;

    private boolean updated = false;

    private boolean initialized = false;
    private int width,height, nChannels;
    private Bitmap bmp;
    private ChannelBufferOutputStream stream;

    private long last_published = 0;

    public CameraPublisher(final ConnectedNode connectedNode) {
        this.publisher = connectedNode.newPublisher("android/image_raw/compressed", CompressedImage._TYPE);
        this.infoPublisher = connectedNode.newPublisher("android/camera_info", CameraInfo._TYPE);

        this.msg = publisher.newMessage();
        this.infoMsg = infoPublisher.newMessage();
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

        stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
        //stream = new ChannelBufferOutputStream(ChannelBuffers.directBuffer(ByteOrder.LITTLE_ENDIAN, width*height*nChannels));
        bmp = Bitmap.createBitmap(img.width(),img.height(), Bitmap.Config.ARGB_8888);

        msg.setFormat("jpeg");

        //there was literally NO HOPE with PNG or RAW.
        infoMsg.setHeight(height);
        infoMsg.setWidth(width);

        final double[] K = new double[]{
                635.242267, 0.000000, 310.804609,
                0.000000, 636.478649, 260.775183,
                0.000000, 0.000000, 1.000000
        };
        final double[] D = new double[]{0.212890, -0.504582, 0.001392, -0.005476, 0.000000};

        final double[] R = new double[]{
                1,0,0,
                0,1,0,
                0,0,1
        };
        final double[] P = new double[]{
                647.669006, 0.000000, 307.565991, 0.000000,
                0.000000, 651.221130, 260.785669, 0.000000,
                0.000000, 0.000000, 1.000000, 0.000000
        };

        infoMsg.setDistortionModel("plumb_bob"); //TODO: ?? narrow_stereo?
        infoMsg.setK(K);
        infoMsg.setD(D);
        infoMsg.setR(R);
        infoMsg.setP(P);
        // leave binning_x as default

        //TODO : fill calibration params after I figure it out
    }

    public void update(Mat img) {
        if(!initialized){
            init(img);
            initialized = true;
        }

        try{
            Utils.matToBitmap(img,bmp);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            stream.buffer().clear();
            stream.buffer().writeBytes(baos.toByteArray());
            msg.setData(stream.buffer());
            updated = true;
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void publish() {
        long now = System.currentTimeMillis();
        float dt = (now - last_published);
        dt /= 1000;
        float hz = 30; // publish rate

        //only publish when data got updated
        if(updated && (dt > 1/hz)){
            updated = false;
            last_published = now;

            Utilities.setHeader(msg.getHeader()); // populate header
            Utilities.setHeader(infoMsg.getHeader());

            publisher.publish(msg);
            infoPublisher.publish(infoMsg);
        }
    }
}