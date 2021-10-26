package com.example.sdksamples;

import com.impinj.octane.ConnectionAttemptEvent;
import com.impinj.octane.ConnectionAttemptListener;
import com.impinj.octane.ImpinjReader;

public class ConnectionAttemptListenerImplementation implements
        ConnectionAttemptListener {

    @Override
    public void onConnectionAttempt(ImpinjReader reader,
                                    ConnectionAttemptEvent e) {
        System.out.println("Connection_Attempt ");
    }
}
