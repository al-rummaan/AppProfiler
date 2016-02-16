package com.example.hazem.applicationprofiler;

import android.content.Context;
import android.os.BatteryManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * Created by hazem on 1/17/2016.
 */
public class Energy {
    int samplingRate;
    boolean onOff;
    Vector<Float> readsVolt;
    Vector<Float> readsCurrent;
    Vector<Float> readsEnergy;
    float average;

    Thread BatteryMonitor;
    private Context context;

    public Energy(final int samplingRate, Context context) {
        this.samplingRate = samplingRate;
        this.context = context;
        onOff = false;
        readsVolt = new Vector<>();
        readsCurrent = new Vector<>();
        readsEnergy = new Vector<>();

        this.BatteryMonitor = new Thread(new Runnable() {
            @Override
            public void run() {
                Float value1;
                Float value2;
                while (onOff) {
                    readBatteryStatFile();
                    try {
                        Thread.sleep(samplingRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    /**
     * calculates the average power consumed during the monitoring time.
     */
    public void calculateAveragePower() {
        float sum = 0;
        for (int i = 300; i < readsEnergy.size(); i++) {
            sum = sum + readsEnergy.get(i);
        }

        average = (float) sum / (readsEnergy.size() - 300);

    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public void ReadEnergy() {
        this.onOff = true;
        this.BatteryMonitor.start();
    }

    public void StopReadingEnergy() {
        try {
            this.onOff = false;
            this.BatteryMonitor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads one
     *
     * @return
     */
    public String readBatteryStatFile() {
        String content = "";
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/class/power_supply/battery/uevent", "r");

            String load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine(); //power supply voltage now
            String[] toks = load.split("=");
            float voltageNow = Float.parseFloat(toks[1]);

            readsVolt.add(voltageNow / 1000000);
            Log.i("Battery", load);

            load = reader.readLine(); //power supply temp
            Log.i("Battery", load);
            load = reader.readLine(); //power supply capacity
            Log.i("Battery", load);
            load = reader.readLine(); //power supply current now
            Log.i("Battery", load);
            String[] toks1 = load.split("=");
            float currentNOw = Float.parseFloat(toks1[1]);
            readsCurrent.add(currentNOw / 1000000);

            readsEnergy.add((currentNOw / 1000000) * (voltageNow / 1000000));
            //Log.i("CPU","toks size: "+toks.length);
            //long freq = Long.parseLong(toks[0]);


            return content;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return content;
    }

    public float getBatteryLevel() {

        BatteryManager mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        Long energy =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            energy = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        Log.i("Energy", "Remaining energy = " + energy + "nWh");
        /*Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }*/

        return (float) energy;
        // return ((float)level / (float)scale) * 100.0f;
    }

    public void printValue() {
        String values = "";
        for (int i = 0; i < readsVolt.size(); i++) {
            values = values + readsVolt.get(i) + ";" + readsCurrent.get(i) + ";" + readsEnergy.get(i) + ",";
        }
        //Log.i("Energy","Energy Values : " + values);
        calculateAveragePower();
        values = values + "\r\n average : " + average;
        writeToFile(values);
    }

    private void writeToFile(String data) {
        try {

            File path = context.getExternalFilesDir(null);
            Log.i("Energy", path.getAbsolutePath());
            File file = new File(path, "energy.txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}

