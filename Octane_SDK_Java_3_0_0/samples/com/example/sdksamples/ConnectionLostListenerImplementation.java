package com.example.sdksamples;

import com.impinj.octane.ConnectionLostListener;
import com.impinj.octane.ImpinjReader;

public class ConnectionLostListenerImplementation implements
        ConnectionLostListener {

    @Override
    public void onConnectionLost(ImpinjReader reader) {
        System.out.println("Connection Lost: Disconnecting");
        reader.disconnect();
        System.exit(0);
    }
}
