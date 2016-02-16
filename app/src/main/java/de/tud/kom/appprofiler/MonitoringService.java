package de.tud.kom.appprofiler;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.example.hazem.applicationprofiler.CPU;
import com.example.hazem.applicationprofiler.Energy;
import com.example.hazem.applicationprofiler.Memory;
import com.example.hazem.applicationprofiler.Network;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hazem on 1/21/2016.
 */
public class MonitoringService extends IntentService {

    public Memory mem;
    public CPU cpu;
    public Energy energy;
    public Network network;

    public MonitoringService() {
        super("MonitoringService");
    }

    public double getSysUptime() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/uptime", "r");
            String load = reader.readLine();

            Log.i("CPU", load);
            String[] toks = load.split(" +");
            return Double.parseDouble(toks[0]);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public int getProcessUID(String applicationID) {
        int uid = 0;
        ActivityManager mgr = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();
        //Log.e("DEBUG", "Running processes:");
        for (Iterator i = processes.iterator(); i.hasNext(); ) {
            ActivityManager.RunningAppProcessInfo p = (ActivityManager.RunningAppProcessInfo) i.next();
            if (p.processName.equals(applicationID)) {

                uid = p.uid;
            }
        }

        return uid;

    }

    public int getProcessID(String applicationId) {
        int pid = 0;
        ActivityManager mgr = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();
        //Log.e("DEBUG", "Running processes:");
        for (Iterator i = processes.iterator(); i.hasNext(); ) {
            ActivityManager.RunningAppProcessInfo p = (ActivityManager.RunningAppProcessInfo) i.next();
            if (p.processName.equals(applicationId)) {


                //Log.e("DEBUG", "  process name: " + p.processName);
                // Log.e("DEBUG", "     pid: " + p.pid);
                pid = p.pid;
            }
        }

        return pid;
    }

    public void showToast(String message) {
        final String msg = message;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        String command = intent.getStringExtra("Action");


        Log.i("Service", "data from intent " + command);
        showToast("Monitoring started");

        switch (command) {
            case "start":
                //network = new Network(getApplicationContext(),this.getProcessUID("com.example.xmppclient"));
                //network.startReading();

                //cpu = new CPU(5000,getProcessID("com.example.xmppclient"),getApplicationContext(),this.getSysUptime());

                //cpu.startReading();
                // Log.i("CPU", "system up time:" + this.getSysUptime());
                //mem = new Memory(getApplicationContext(),500);
                energy = new Energy(50, getApplicationContext());
                energy.ReadEnergy();
                //energy.readBatteryStatFile();
                //mem.startMonitoring();
                long time = SystemClock.elapsedRealtime();
                while (SystemClock.elapsedRealtime() != time + 120000) ;
                //SystemClock.sleep(120000);
                //try {
                //   Thread.sleep(120000);//montior for 3 mins
                //} catch (InterruptedException e) {
                //   e.printStackTrace();
                //}

                //energy.readBatteryStatFile();

                //network.stopReading();
                //network.printUsage();
                //showToast("Monitoring finished");
                //mem.stopMonitoring();
                //mem.printValue();
                energy.StopReadingEnergy();
                energy.printValue();
                //cpu.stopReading();
                //cpu.printValue();
                showToast("monitoring finished");

                //energy.printValue();
                //

                break;
            case "stop":

                break;
        }

    }
}
