package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.KeepaliveEvent;
import com.impinj.octane.KeepaliveListener;

public class KeepAliveListenerImplementation implements KeepaliveListener {

    @Override
    public void onKeepalive(ImpinjReader reader, KeepaliveEvent e) {
        System.out.println("!KeepAlive!");
    }
}
