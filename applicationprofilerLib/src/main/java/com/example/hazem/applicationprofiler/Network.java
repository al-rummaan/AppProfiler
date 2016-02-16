package com.example.hazem.applicationprofiler;

/**
 * Created by hazem on 1/21/2016.
 */

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class Network {

    private static final String TAG = "Network.java";
    public static String pingError = null;
    Vector<Float> reads;
    Vector<Long> RxBytes;
    Vector<Long> TxBytes;
    Context context;
    boolean keepMonitoring;
    Thread monitorNetwork;
    int uid;

    public Network(Context context, int uid) {
        this.reads = new Vector<>();
        this.RxBytes = new Vector<>();
        this.TxBytes = new Vector<>();
        this.context = context;
        this.uid = uid;

        keepMonitoring = false;
        monitorNetwork = new Thread(new Runnable() {
            @Override
            public void run() {
                while (keepMonitoring) {
                    readDataUsage();
                }
            }
        });
    }

    /**
     * Ping a host and return an int value of 0 or 1 or 2 0=success, 1=fail, 2=error
     * <p/>
     * Does not work in Android emulator and also delay by '1' second if host not pingable
     * In the Android emulator only ping to 127.0.0.1 works
     *
     * @param String host in dotted IP address format
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int pingHost(String host) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("ping -c 1 " + host);
        proc.waitFor();
        int exit = proc.exitValue();
        return exit;
    }

    public static String ping(String host, int packetSize) throws IOException, InterruptedException {
        StringBuffer echo = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        Log.v(TAG, "About to ping using runtime.exec");
        Process proc = runtime.exec("ping -c 1 -s " + packetSize + " " + host);
        proc.waitFor();
        int exit = proc.exitValue();
        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line = "";
            while ((line = buffer.readLine()) != null) {
                echo.append(line + "\n");
            }
            return getPingStats(echo.toString());
        } else if (exit == 1) {
            pingError = "failed, exit = 1";
            return null;
        } else {
            pingError = "error, exit = 2";
            return null;
        }
    }

    /**
     * getPingStats interprets the text result of a Linux ping command
     * <p/>
     * Set pingError on error and return null
     * <p/>
     * http://en.wikipedia.org/wiki/Ping
     * <p/>
     * PING 127.0.0.1 (127.0.0.1) 56(84) bytes of data.
     * 64 bytes from 127.0.0.1: icmp_seq=1 ttl=64 time=0.251 ms
     * 64 bytes from 127.0.0.1: icmp_seq=2 ttl=64 time=0.294 ms
     * 64 bytes from 127.0.0.1: icmp_seq=3 ttl=64 time=0.295 ms
     * 64 bytes from 127.0.0.1: icmp_seq=4 ttl=64 time=0.300 ms
     * <p/>
     * --- 127.0.0.1 ping statistics ---
     * 4 packets transmitted, 4 received, 0% packet loss, time 0ms
     * rtt min/avg/max/mdev = 0.251/0.285/0.300/0.019 ms
     * <p/>
     * PING 192.168.0.2 (192.168.0.2) 56(84) bytes of data.
     * <p/>
     * --- 192.168.0.2 ping statistics ---
     * 1 packets transmitted, 0 received, 100% packet loss, time 0ms
     * <p/>
     * # ping 321321.
     * ping: unknown host 321321.
     * <p/>
     * 1. Check if output contains 0% packet loss : Branch to success -> Get stats
     * 2. Check if output contains 100% packet loss : Branch to fail -> No stats
     * 3. Check if output contains 25% packet loss : Branch to partial success -> Get stats
     * 4. Check if output contains "unknown host"
     *
     * @param s
     */
    public static String getPingStats(String s) {
        if (s.contains("0% packet loss")) {
            int start = s.indexOf("/mdev = ");
            int end = s.indexOf(" ms\n", start);
            s = s.substring(start + 8, end);
            String stats[] = s.split("/");
            return stats[2];
        } else if (s.contains("100% packet loss")) {
            pingError = "100% packet loss";
            return null;
        } else if (s.contains("% packet loss")) {
            pingError = "partial packet loss";
            return null;
        } else if (s.contains("unknown host")) {
            pingError = "unknown host";
            return null;
        } else {
            pingError = "unknown error in getPingStats";
            return null;
        }
    }

    public void startReading() {
        keepMonitoring = true;
        monitorNetwork.start();
    }

    public void stopReading() {
        keepMonitoring = false;
        try {
            monitorNetwork.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void readDataUsage() {

        long rx = TrafficStats.getUidRxBytes(uid);
        long tx = TrafficStats.getUidTxBytes(uid);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RxBytes.add(TrafficStats.getUidRxBytes(uid) - rx);

        TxBytes.add(TrafficStats.getUidTxBytes(uid) - tx);

    }

    public void collectRTT(String host) {
        int dataSize = 56;
        for (int i = 0; i < 15; i++) {
            try {
                reads.add(Float.parseFloat(ping(host, dataSize)));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            dataSize += 50;
        }
    }

    public void printValue() {
        String values = "";
        for (int i = 0; i < reads.size(); i++) {
            values = values + reads.get(i) + ",";
        }
        Log.i("Network", "RTT Values : " + values);

        writeToFile(values);
    }

    public void printUsage() {
        String values = "";
        for (int i = 0; i < RxBytes.size(); i++) {
            values = values + (RxBytes.get(i) + TxBytes.get(i)) + ",";
        }

        writeToFile(values);
    }

    private void writeToFile(String data) {
        try {

            File path = this.context.getExternalFilesDir(null);
            Log.i("Network", path.getAbsolutePath());
            File file = new File(path, "Network.txt");
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

