package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.ArrayList;
import java.util.Scanner;

public class ReducedPowerFrequencies {

    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            System.out.println("Connecting");
            reader.connect(hostname);

            reader.setTagReportListener(new TagReportListenerImplementation());

            FeatureSet features = reader.queryFeatureSet();
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.getReport().setIncludeChannel(true);
            settings.getReport().setMode(ReportMode.Individual);

            if (features.isHoppingRegion() && features.getReaderModel() != ReaderModel.SpeedwayR120 && features.getReaderModel() != ReaderModel.SpeedwayR220) {
                // setting reduced power frequencies is allowed if it's hopping
                ArrayList<Double> freqList = new ArrayList<Double>();
                freqList.add(902.75);
                freqList.add(903.25);
                freqList.add(903.75);
                freqList.add(904.25);
                freqList.add(904.75);
                freqList.add(905.25);
                freqList.add(905.75);
                freqList.add(906.25);
                freqList.add(906.75);
                freqList.add(907.25);
                freqList.add(907.75);
                freqList.add(908.25);
                freqList.add(908.75);
                freqList.add(909.25);
                freqList.add(909.75);

                settings.setReducedPowerFrequenciesInMhz(freqList);
            }

            System.out.println("Applying Settings");
            reader.applySettings(settings);

            System.out.println("Starting");
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
