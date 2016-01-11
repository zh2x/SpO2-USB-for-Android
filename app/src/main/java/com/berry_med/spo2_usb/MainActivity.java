package com.berry_med.spo2_usb;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.berry_med.spo2_usb.DataParser.DataParser;
import com.berry_med.spo2_usb.USBSerial.USBCommManager;

import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();

    private USBCommManager mUSBCommManager;
    private DataParser     mDataParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //new A data parser to decode the data.
        mDataParser = new DataParser(DataParser.Protocol.BCI, new DataParser.onPackageReceivedListener() {
            @Override
            public void onPackageReceived(int[] dat) {
                Log.i(TAG, "onPackageReceived: " + Arrays.toString(dat));
            }
        });
        mDataParser.start();

        //init USB serial connection
        mUSBCommManager = USBCommManager.getUSBManager(this);
        mUSBCommManager.setListener(new USBCommManager.USBCommListener() {
            @Override
            public void onReceiveData(byte[] dat) {
                mDataParser.add(dat);
            }

            @Override
            public void onUSBStateChanged(boolean isPlugged) {

            }
        });
        mUSBCommManager.initConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataParser.stop();
    }
}
