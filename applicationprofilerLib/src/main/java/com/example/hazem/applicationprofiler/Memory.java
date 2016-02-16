package com.example.hazem.applicationprofiler;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Created by hazem on 1/20/2016.
 */
public class Memory {

    Context context;
    int sampleRate;
    Vector<Vector<Integer>> reads;
    Thread monitorMemory;
    boolean keepReading;

    public Memory(Context context, int samplingRate) {
        this.context = context;
        this.sampleRate = samplingRate;
        reads = new Vector<>();
        keepReading = false;

        monitorMemory = new Thread(new Runnable() {
            @Override
            public void run() {
                while (keepReading) {

                    try {
                        Thread.sleep(sampleRate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reads.add(readMemory());
                }
            }
        });
    }

    public int getSamplingRate() {

        return sampleRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.sampleRate = samplingRate;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Vector<Integer> readMemory() {
        Vector<Integer> values = new Vector<>();

        ActivityManager mgr = (ActivityManager) this.context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();
        Log.e("DEBUG", "Running processes:");
        for (Iterator i = processes.iterator(); i.hasNext(); ) {
            ActivityManager.RunningAppProcessInfo p = (ActivityManager.RunningAppProcessInfo) i.next();
            if (p.processName.equals("com.example.xmppclient")) {


                Log.e("DEBUG", "  process name: " + p.processName);
                Log.e("DEBUG", "     pid: " + p.pid);
                int[] pids = new int[1];
                pids[0] = p.pid;
                android.os.Debug.MemoryInfo[] MI = mgr.getProcessMemoryInfo(pids);
                Log.e("memory", "     dalvik private: " + MI[0].dalvikPrivateDirty);
                Log.e("memory", "     dalvik shared: " + MI[0].dalvikSharedDirty);
                Log.e("memory", "     dalvik pss: " + MI[0].dalvikPss);
                Log.e("memory", "     native private: " + MI[0].nativePrivateDirty);
                Log.e("memory", "     native shared: " + MI[0].nativeSharedDirty);
                Log.e("memory", "     native pss: " + MI[0].nativePss);
                Log.e("memory", "     other private: " + MI[0].otherPrivateDirty);
                Log.e("memory", "     other shared: " + MI[0].otherSharedDirty);
                Log.e("memory", "     other pss: " + MI[0].otherPss);

                Log.e("memory", "     total private dirty memory (KB): " + MI[0].getTotalPrivateDirty());
                Log.e("memory", "     total shared (KB): " + MI[0].getTotalSharedDirty());
                Log.e("memory", "     total pss: " + MI[0].getTotalPss());

                values.add(MI[0].getTotalPrivateDirty());
                values.add(MI[0].getTotalSharedDirty());
                values.add(MI[0].getTotalPss());

            }
        }
        return values;

    }

    public void startMonitoring() {
        keepReading = true;
        monitorMemory.start();

    }


    public void stopMonitoring() {
        keepReading = false;
        try {
            monitorMemory.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void printValue() {
        String values = "";
        for (int i = 0; i < reads.size(); i++) {
            for (int j = 0; j < 3; j++)
                values = values + reads.get(i).get(j) + ",";

            values = values + " | ";
        }
        Log.i("CPU", "CPU Values : " + values);
        writeToFile(values);
    }

    private void writeToFile(String data) {
        try {

            File path = context.getExternalFilesDir(null);
            Log.i("Memory", path.getAbsolutePath());
            File file = new File(path, "memory.txt");
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
