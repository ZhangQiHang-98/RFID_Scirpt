package com.example.sdksamples;

import com.impinj.octane.BufferOverflowEvent;
import com.impinj.octane.BufferOverflowListener;
import com.impinj.octane.ImpinjReader;

public class BufferOverflowListenerImplementation implements
        BufferOverflowListener {

    @Override
    public void onBufferOverflow(ImpinjReader reader, BufferOverflowEvent e) {
        System.out.println("Buffer_Overflow-- ");
    }
}
