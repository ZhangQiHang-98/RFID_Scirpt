package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.ReaderStartEvent;
import com.impinj.octane.ReaderStartListener;

public class ReaderStartListenerImplementation implements ReaderStartListener {

    @Override
    public void onReaderStart(ImpinjReader reader, ReaderStartEvent e) {
        System.out.println("Reader_Started");
    }
}
