package com.example.hazem.applicationprofiler;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * Measures CPU usage of one process.
 */
public class CPU {
    /**
     *
     */
    int samplingRate;
    boolean keepReading;
    Vector<Double> reads;
    int processID;
    Thread CPUMonitor;
    double uptime;
    private Context context;


    public CPU(final int samplingRate, int processID, Context context, double uptime) {
        this.uptime = uptime;
        this.context = context;
        this.samplingRate = samplingRate;
        this.reads = new Vector<>();
        keepReading = false;
        this.processID = processID;

        this.CPUMonitor = new Thread(new Runnable() {
            @Override
            public void run() {
                while (keepReading) {
                    double read1 = readUsage();
                    try {
                        Thread.sleep(samplingRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reads.add((readUsage() - read1) / ((double) (samplingRate / 1000)));
                }
            }
        });
    }


    /**
     * get the current frequency of the processor.
     *
     * @return long representing the frequency in Hz.
     */
    public long getcurrentFrequency() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r");
            String load = reader.readLine();

            Log.i("CPU", load);
            String[] toks = load.split(" +");  // Split on one or more spaces

            //Log.i("CPU","toks size: "+toks.length);
            long freq = Long.parseLong(toks[0]);


            return freq;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    /**
     * starts the thread for reading cpu usage
     */
    public void startReading() {
        this.keepReading = true;
        this.CPUMonitor.start();

    }


    /**
     * stops the thread that reads cpu usage.
     */
    public void stopReading() {
        this.keepReading = false;
        try {
            this.CPUMonitor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * gets  the process ID which is being monitored.
     *
     * @return process Id as integer.
     */
    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public boolean isKeepReading() {
        return keepReading;
    }

    public void setKeepReading(boolean keepReading) {
        this.keepReading = keepReading;
    }

    /**
     * reads one sample of the process stat file.
     *
     * @return the total time spent in CPU till this time.
     */
    public double readUsage() {

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/" + this.processID + "/stat", "r");
            String load = reader.readLine();

            Log.i("CPU", load);
            String[] toks = load.split(" ");  // Split on one or more spaces

            Log.i("CPU", "toks size: " + toks.length);
            long utime = Long.parseLong(toks[13]);
            long stime = Long.parseLong(toks[14]);
            long cutime = Long.parseLong(toks[15]);
            long cstime = Long.parseLong(toks[16]);
            long starttime = Long.parseLong(toks[22]);

            //long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
            //       + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);


            //long idle2 = Long.parseLong(toks[4]);
            //long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
            //       + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            //return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

            long totaltime = utime + stime + cutime + cstime;


            return totaltime;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    /**
     * write the readings to a file.
     */
    public void printValue() {
        String values = "";
        for (int i = 0; i < reads.size(); i++) {
            values = values + reads.get(i) + ",";
        }
        Log.i("CPU", "CPU Values : " + values);
        writeToFile(values);
    }

    /**
     * writes string to file.
     *
     * @param data
     */
    private void writeToFile(String data) {
        try {

            File path = context.getExternalFilesDir(null);
            Log.i("Energy", path.getAbsolutePath());
            File file = new File(path, "cpu.txt");
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
