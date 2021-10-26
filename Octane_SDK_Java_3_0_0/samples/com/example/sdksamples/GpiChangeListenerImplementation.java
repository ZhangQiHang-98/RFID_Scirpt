package com.example.sdksamples;

import com.impinj.octane.GpiChangeListener;
import com.impinj.octane.GpiEvent;
import com.impinj.octane.ImpinjReader;

public class GpiChangeListenerImplementation implements GpiChangeListener {

    @Override
    public void onGpiChanged(ImpinjReader reader, GpiEvent e) {
        System.out.println("GPI Change--port: " + e.getPortNumber()
                + " status: " + e.isState());
    }
}
