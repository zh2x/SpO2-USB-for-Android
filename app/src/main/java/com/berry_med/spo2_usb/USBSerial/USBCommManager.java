package com.berry_med.spo2_usb.USBSerial;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

/**
 * 管理USB插入拔出和数据接收
 * Created by ZXX on 2015/12/29.
 */
public class USBCommManager {

    //Const
    public String TAG = USBCommManager.class.getSimpleName();

    private static Context            mContext;
    private static USBCommManager     mUSBCommManager;
    private USBCommListener           mListener;

    //定时扫描USB状态
    private boolean               mIsPlugged;
    private List<UsbSerialDriver> availableDrivers;

    private UsbManager         mUsbManager;
    private UsbSerialPort      mSerialPort;


    //构造 单例模式
    private USBCommManager() {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }
    public static  USBCommManager getUSBManager(Context context)
    {
        if(mUSBCommManager == null){
            mContext = context;
            mUSBCommManager = new USBCommManager();
        }

        return mUSBCommManager;
    }

    //Listener
    public void setListener(USBCommListener listener)
    {
        mListener = listener;
    }

    /**
     * 初始化连接
     */
    public void initConnection()
    {
        //创建连接
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if(availableDrivers.size() > 0){
            buildConnection();
            mIsPlugged = true;

            mListener.onUSBStateChanged(mIsPlugged);
            //定时扫描状态
            startScan();
        }


    }

    //定时扫描USB设备
    private void startScan() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsPlugged)
                {
                    availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
                    if(availableDrivers.size() == 0){
                        mIsPlugged = false;
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //Log.i(TAG, " USB scan....--------------------"+ mIsPlugged);
                }

                mListener.onUSBStateChanged(mIsPlugged);
            }
        }).start();



    }

    /**
     * 建立连接 读取数据
     */
    private void buildConnection() {
        UsbSerialDriver     driver     = availableDrivers.get(0);
        UsbDevice           usbDevice  = driver.getDevice();
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);

        mSerialPort = driver.getPorts().get(0);
        try {
            mSerialPort.open(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsPlugged) {
                    try {
                        byte buffer[] = new byte[32];
                        int numBytesRead = mSerialPort.read(buffer, 100);
                        if(numBytesRead > 0)
                        {
                            byte[] dat = new byte[numBytesRead];
                            System.arraycopy(buffer, 0, dat, 0, numBytesRead);
                            //Log.d("USB_SERIAL", "Read " + numBytesRead + " bytes.");
                            mListener.onReceiveData(dat);
                        }

                    } catch (IOException e) {
                        // Deal with error.
                    }
                }
            }
        }).start();
    }

    /**
     * 获取USB插拔状态
     * @return USB状态
     */
    public boolean isPlugged()
    {
        return mIsPlugged;
    }

    //数据传输接口
    public interface USBCommListener
    {
        void onReceiveData(byte[] dat);
        void onUSBStateChanged(boolean isPlugged);
    }
}
