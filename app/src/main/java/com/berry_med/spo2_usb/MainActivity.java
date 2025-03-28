package com.berry_med.spo2_usb;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.berry_med.spo2_usb.OximeterData.DataParser;
import com.berry_med.spo2_usb.OximeterData.PackageParser;
import com.berry_med.spo2_usb.USBSerial.USBCommManager;

public class MainActivity extends AppCompatActivity {
    private TextView tvSpO2;
    private TextView tvPulseRate;

    private USBCommManager manager;
    private PackageParser mPackageParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSpO2 = findViewById(R.id.tvSpO2);
        tvPulseRate = findViewById(R.id.tvPulseRate);
        findViewById(R.id.getSource).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zh2x/SpO2-USB-for-Android"))));
        findViewById(R.id.read_data).setOnClickListener(v -> {
            if (manager == null) return;
            manager.usbSerialPort();
        });

        //new A data parser to decode the data.
        DataParser mDataParser = new DataParser(DataParser.Protocol.BCI, dat -> mPackageParser.parse(dat));
        mDataParser.start();

        mPackageParser = new PackageParser(new PackageParser.OnDataChangeListener() {
            @Override
            public void onParamsChanged() {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        PackageParser.OxiParams params = mPackageParser.getOxiParams();
                        if (params.getSpo2() != params.SPO2_INVALID_VALUE) {
                            tvSpO2.setText("" + params.getSpo2());
                        } else {
                            tvSpO2.setText("- -");
                        }

                        if (params.getPulseRate() != params.PULSE_RATE_INVALID_VALUE) {
                            tvPulseRate.setText("" + params.getPulseRate());
                        } else {
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
        manager = new USBCommManager(this, mDataParser);
        manager.usbSerialPort();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}