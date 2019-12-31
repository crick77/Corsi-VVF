/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.dipvvf.abr.app.corsivvf.bean;

/**
 *
 * @author ospite
 */
public class Device {
    private final String deviceId;
    private final String deviceToken;

    public Device(String deviceId, String deviceToken) {
        this.deviceId = deviceId;
        this.deviceToken = deviceToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }
}
