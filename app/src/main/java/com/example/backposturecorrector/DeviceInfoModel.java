package com.example.backposturecorrector;

public class DeviceInfoModel {

    private final String deviceName, deviceHardwareAddress;

    public DeviceInfoModel(String deviceName, String deviceHardwareAddress) {
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceHardwareAddress() {
        return deviceHardwareAddress;
    }

}
