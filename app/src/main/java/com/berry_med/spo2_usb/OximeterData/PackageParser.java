package com.berry_med.spo2_usb.OximeterData;


/**
 * Created by ZXX on 2015/8/31.
 * <p>
 * Add all data from oximeter into a Queue, and then Parsing the data as the protocol manual.
 * If you want more details about the protocol, click the link below.
 * <p>
 * https://github.com/zh2x/BCI_Protocol_Demo/tree/master/protocol_manual
 */
public class PackageParser {

    private OxiParams mOxiParams;
    private OnDataChangeListener mOnDataChangeListener;

    public PackageParser(OnDataChangeListener onDataChangeListener) {
        this.mOnDataChangeListener = onDataChangeListener;

        mOxiParams = new OxiParams();
    }

    public void parse(int[] packageDat) {

        int spo2, pulseRate, pi;

        spo2 = packageDat[4];
        pulseRate = packageDat[3] | ((packageDat[2] & 0x40) << 1);
        pi = packageDat[0] & 0x0f;

        if (spo2 != mOxiParams.spo2 || pulseRate != mOxiParams.pulseRate || pi != mOxiParams.pi) {
            mOxiParams.update(spo2, pulseRate, pi);
            mOnDataChangeListener.onParamsChanged();
        }
        mOnDataChangeListener.onWaveChanged(packageDat[1]);
    }

    /**
     * interface for parameters changed.
     */
    public interface OnDataChangeListener {
        void onParamsChanged();

        void onWaveChanged(int wave);
    }


    /**
     * a small collection of Oximeter parameters.
     * you can add more parameters as the manual.
     * <p>
     * spo2          Pulse Oxygen Saturation
     * pulseRate     pulse rate
     * pi            perfusion index
     */
    public class OxiParams {
        public final int SPO2_INVALID_VALUE = 127;
        public final int PULSE_RATE_INVALID_VALUE = 255;

        private int spo2;
        private int pulseRate;
        private int pi;             //perfusion index

        private void update(int spo2, int pulseRate, int pi) {
            this.spo2 = spo2;
            this.pulseRate = pulseRate;
            this.pi = pi;
        }

        public int getSpo2() {
            return spo2;
        }

        public int getPulseRate() {
            return pulseRate;
        }

        public int getPi() {
            return pi;
        }
    }

    public OxiParams getOxiParams() {
        return mOxiParams;
    }

}
