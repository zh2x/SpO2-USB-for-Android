package com.berry_med.spo2_usb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.berry_med.spo2_usb.OximeterData.DataParser;
import com.berry_med.spo2_usb.OximeterData.PackageParser;
import com.berry_med.spo2_usb.USBSerial.USBCommManager;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();
    private final String GITHUB_SITE = "https://github.com/zh2x/SpO2-USB-for-Android";

    private USBCommManager mUSBCommManager;
    private DataParser mDataParser;

    private PackageParser mPackageParser;

    //UI
    private TextView tvSpO2;
    private TextView tvPulseRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //UI control
        tvSpO2 = (TextView) findViewById(R.id.tvSpO2);
        tvPulseRate = (TextView) findViewById(R.id.tvPulseRate);
        mPackageParser = new PackageParser(new PackageParser.OnDataChangeListener() {
            @Override
            public void onParamsChanged() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PackageParser.OxiParams params = mPackageParser.getOxiParams();

                        if(params.getSpo2() != params.SPO2_INVALID_VALUE) {
                            tvSpO2.setText(""+params.getSpo2());
                        }else {
                            tvSpO2.setText("- -");
                        }

                        if(params.getPulseRate() != params.PULSE_RATE_INVALID_VALUE) {
                            tvPulseRate.setText(""+params.getPulseRate());
                        }else {
                            tvPulseRate.setText("- -");
                        }
                    }
                });
            }

            @Override
            public void onWaveChanged(int wave) {
                //draw the waveform of pulse wave
            }
        });


        //new A data parser to decode the data.
        mDataParser = new DataParser(DataParser.Protocol.BCI, new DataParser.onPackageReceivedListener() {
            @Override
            public void onPackageReceived(int[] dat) {
                Log.i(TAG, "onPackageReceived: " + Arrays.toString(dat));
                mPackageParser.parse(dat);
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

    public void onClick(View v)
    {
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(GITHUB_SITE)));
    }


}
