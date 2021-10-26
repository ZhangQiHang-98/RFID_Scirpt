package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;

public class ReaderStopListenerImplementation implements ReaderStopListener {

    @Override
    public void onReaderStop(ImpinjReader reader, ReaderStopEvent e) {
        System.out.println("Reader_Stopped");
    }
}
