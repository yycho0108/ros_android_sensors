package com.github.ros_java.test_android.sensor_serial;

/**
 * Created by jamiecho on 2/5/17.
 */

public class Quaternion {
    // data class
    public float w,x,y,z;
    public Quaternion(float w, float x, float y, float z){
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Quaternion(float[] v){
        this(v[0],v[1],v[2],v[3]);
    }
    public Quaternion(Quaternion q){
        this(q.w,q.x,q.y,q.z);
    }
    static Quaternion fromAxisAngle(float[] axis, float angle){
        final float w = (float)Math.cos(angle/2);
        final float s = (float)Math.sin(angle/2);
        final float qx = axis[0] * s;
        final float qy = axis[1] * s;
        final float qz = axis[2] * s;

        return new Quaternion(w,qx,qy,qz);
    }
    Quaternion mul(Quaternion q){
        final float qx = this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y;
        final float qy = this.w * q.y + this.y * q.w + this.z * q.x - this.x * q.z;
        final float qz = this.w * q.z + this.z * q.w + this.x * q.y - this.y * q.x;
        final float w = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z;

        return new Quaternion(w,qx,qy,qz);
    }
    float norm(){
        return (float)Math.sqrt(x*x+y*y+z*z+w*w);
    }

    void normalize(){
        float s = norm();
        w /= s;
        x /= s;
        y /= s;
        z /= s;
    }
}
