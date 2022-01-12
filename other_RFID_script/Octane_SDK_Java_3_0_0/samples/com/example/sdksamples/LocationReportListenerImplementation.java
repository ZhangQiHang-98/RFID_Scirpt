package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.LocationReport;
import com.impinj.octane.LocationReportListener;

public class LocationReportListenerImplementation implements
        LocationReportListener {

    @Override
    public void onLocationReported(ImpinjReader reader, LocationReport report) {
        System.out.println("Location: " + " epc: "
                + report.getEpc().toHexString() + " x: "
                + report.getLocationXCm() + " y: " + report.getLocationYCm()
                + " read_count: "
                + report.getConfidenceFactors().getReadCount());
    }
}
