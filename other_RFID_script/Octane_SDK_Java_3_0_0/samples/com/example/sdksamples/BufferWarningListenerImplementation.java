package com.example.sdksamples;

import com.impinj.octane.BufferWarningEvent;
import com.impinj.octane.BufferWarningListener;
import com.impinj.octane.ImpinjReader;

public class BufferWarningListenerImplementation implements
        BufferWarningListener {

    @Override
    public void onBufferWarning(ImpinjReader reader, BufferWarningEvent e) {
        System.out.println("Buffer_Warning--percent_full: "
                + e.getPercentFull());
    }
}
