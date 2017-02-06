#!/usr/bin/env python  
import rospy
import tf
from sensor_msgs.msg import Imu

PI = 3.14159265358979323846264338327950
hist = []

def d2r(d):
    global PI
    return d * PI / 180.

def r2d(r):
    global PI
    return r * 180 / PI

def cb(msg):
    o = msg.orientation
    q = [o.x,o.y,o.z,o.w] # ros-repr.
    e = tf.transformations.euler_from_quaternion(q, axes='sxyz')
    e = list(e)
    e = [r2d(x) for x in e]
    #decl = -14.60 # mag.north pointing a bit westwards
    x,y,z = e[0], e[1], e[2]
    print '{:.2f} {:.2f} {:.2f}'.format(x,y,z)

if __name__ == '__main__':
    rospy.init_node('tf_test_node')
    s = rospy.Subscriber('/android/imu', Imu, cb)
    rospy.spin()
