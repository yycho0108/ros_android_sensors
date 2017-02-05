# ROS Android Sensors

Ros Interface to Android Sensors!

Heavily Inspired by [ros\_android\_sensors\_driver](https://github.com/ros-android/android_sensors_driver) during development.

Currently Supporting Android 6.0 (Marshmallow) API 23

Intended for Aiding Robot Navigation and Localization

List of Supported Sensors:

- [x] GPS
- [x] Gyroscope
- [x] Accelerometer
- [x] Magnetometer
- [x] Barometer
- [ ] Camera
- [ ] Microphone

Other Tasks:

- Revise Covariance Matrices dynamically based on professed sensor accuracy
- Consider Odometry Visualization with onboard Kalman filter?
- Consider Serial Communication, or other modes of connection with the robot

## How to Run

### [Optional] Configure USB Tethering

Before you start, the below steps are recommended if you don't want to risk network disconnection. 

1. Edit the network interfaces file:

	```bash
	sudo vim /etc/network/interfaces
	```
2. Add the following lines:

	```bash
	allow-hotplug usb0
	iface usb0 inet dhcp
	```

3. Re-start networking:

	```bash
	sudo service networking restart
	```

4. Configure USB Tethering on the Android Device:

	Settings > Mobile Hotspot and Tethering > USB Tethering **ON**

Now you should be ready to go!

### Running the App

1. Start roscore on the host computer:

	```bash
	roscore
	```


2. Poll the host computer's IP address:

	```bash
	ifconfig [usb0/wlan0]
	```

	Whether or not it is usb0/wlan0, etc. is dependent on how you configured the network settings.

3. Launch the app, and enter the IP address.

4. To test whether or not the topics are being published:

	```bash
	rostopic list
	rostopic echo /android_imu
	rostopic echo /android_gps
	```
