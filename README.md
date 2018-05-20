# ROS Android Sensors

Ros Interface to Android Sensors!

This project is compatible with [Android Studio](https://developer.android.com/studio/).

Heavily Inspired by [ros\_android\_sensors\_driver](https://github.com/ros-android/android_sensors_driver) during development.

Currently Supporting Android 7.0 (Nougat) API 24

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

- [ ] Support for Visual Odometry
- [ ] Support for Fused Odometry?

## Dependencies

- [android\_core](http://wiki.ros.org/android_core)

In my case, I installed the library as:

```bash
cd ~/libs
mkdir -p android_core/src
wstool init -j4 src https://raw.github.com/rosjava/rosjava/kinetic/android_core.rosinstall
catkin build
echo 'source ~/libs/android_core/devel/setup.bash' >> ~/.bashrc
```

Remember to add the `--extend` option for any overlaying workspace afterwards.

## Run

### [Optional] Configure USB Tethering

Before you start, consider the below steps if you don't want to bother with connection over wireless network.

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

    Be sure to have the phone connected to the same network as the host computer!

3. Launch the app, and enter the IP address.

4. To test whether or not the topics are being published:

	```bash
	rostopic list
	rostopic echo /android/imu
	rostopic echo /android/gps
	```
