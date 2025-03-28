package com.berry_med.spo2_usb.USBSerial;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.berry_med.spo2_usb.OximeterData.DataParser;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.List;

/**
 * Manage USB plug-in and unplugging and data receiving
 * Created by ZXX on 2015/12/29.
 */
public class USBCommManager {
    private static final String USB_PERMISSION = "com.android.example.USB_PERMISSION";
    protected UsbManager mUsbManager;
    protected Context context;
    protected DataParser mDataParser;

    public USBCommManager(Context context, DataParser mDataParser) {
        this.context = context;
        this.mDataParser = mDataParser;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public void usbSerialPort() {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) return;
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = mUsbManager.openDevice(driver.getDevice());
        if (connection == null) {
            // requestPermission
            PendingIntent permissionIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            mUsbManager.requestPermission(driver.getDevice(), permissionIntent);
            return;
        }
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (port == null) return;
        SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, new SerialInputOutputManager.Listener() {
            @Override
            public void onNewData(byte[] data) {
                mDataParser.add(data);
            }

            @Override
            public void onRunError(Exception e) {

            }
        });
        usbIoManager.start();
    }
}
