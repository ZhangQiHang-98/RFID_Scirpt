package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilteredTagReportListenerImplementation implements
        TagReportListener {
    // an internal map to store unique stuff

    Map<String, Tag> map;

    public FilteredTagReportListenerImplementation() {
        map = new HashMap<String, Tag>();
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();

        for (Tag t : tags) {
            String key;

            if (t.isFastIdPresent()) {
                key = t.getTid().toHexString();
            } else {
                key = t.getEpc().toHexString();
            }

            if (map.containsKey(key)) {
                continue;
            } else {
                map.put(key, t);
            }

            System.out.print(" EPC: " + t.getEpc().toString());

            if (t.isAntennaPortNumberPresent()) {
                System.out.print(" antenna: " + t.getAntennaPortNumber());
            }

            if (t.isFirstSeenTimePresent()) {
                System.out.print(" first: " + t.getFirstSeenTime().ToString());
            }

            if (t.isLastSeenTimePresent()) {
                System.out.print(" first: " + t.getLastSeenTime().ToString());
            }

            if (t.isSeenCountPresent()) {
                System.out.print(" count: " + t.getTagSeenCount());
            }

            if (t.isRfDopplerFrequencyPresent()) {
                System.out.print(" doppler: " + t.getRfDopplerFrequency());
            }

            System.out.println("");
        }
    }
}
