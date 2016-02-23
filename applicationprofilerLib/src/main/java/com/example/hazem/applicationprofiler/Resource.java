package com.example.hazem.applicationprofiler;

import android.content.Context;

import java.util.List;

/**
 * Created by hazem on 2/23/2016.
 */
public class Resource {

    int processid = 0;
    String appID = "";
    List<Float> values = null;
    int samplingRate = 0;
    boolean keepReading = false;
    Context context = null;
    String saveTo = "";
    Thread monitor;

    public Resource(Context context) {
        this.context = context;
    }

    public int getProcessid() {
        return processid;
    }

    public void setProcessid(int processid) {
        this.processid = processid;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public List<Float> getValues() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getSaveTo() {
        return saveTo;
    }

    public void setSaveTo(String saveTo) {
        this.saveTo = saveTo;
    }

    public void StartReading() {

    }

    public void stopReading() {

    }

    public String toString() {
        return "";
    }

    public void saveToFile() {

    }


}
